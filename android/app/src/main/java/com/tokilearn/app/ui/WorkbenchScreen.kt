package com.tokilearn.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private enum class Tool { PEN, LINE, RECT, CIRCLE, ERASER }

private data class StrokeData(val tool: Tool, val points: List<Offset>, val color: Color, val width: Float)

/**
 * 刷题工作台：上传题目图（系统图片选择器 + Coil 显示）、双指放大查看、
 * 画笔 / 直线 / 矩形 / 圆 / 橡皮批注、可撤销清空、旁边写计算过程。
 * 批注仅保留在当前会话，不上传任何服务器。
 */
@Composable
fun WorkbenchScreen(onBack: () -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tool by remember { mutableStateOf(Tool.PEN) }
    var process by remember { mutableStateOf("") }
    val strokes = remember { mutableStateListOf<StrokeData>() }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") }
            Text(text = "刷题工作台", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { pickImage.launch("image/*") }) { Text("上传题目图") }
            OutlinedButton(onClick = { imageUri = null; strokes.clear() }) { Text("清除图片") }
        }
        Spacer(Modifier.height(12.dp))
        if (imageUri == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.large),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(text = "还没有题目图，点“上传题目图”从相册选一张。", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(420.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.large)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset += pan
                        }
                    },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageUri).crossfade(true).build(),
                        contentDescription = "题目图",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    AnnotationCanvas(
                        strokes = strokes,
                        tool = tool,
                        onStroke = { strokes.add(it) },
                        onErasePoint = { point ->
                            // 橡皮按整笔擦除：删掉触点 48px 内有点的笔画。
                            val hit = strokes.filter { s -> s.points.any { (it - point).getDistance() < 48f } }
                            strokes.removeAll(hit)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "双指缩放查看；批注画在图上。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Text(text = "批注工具", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ToolChip("画笔", Icons.Outlined.Edit, tool == Tool.PEN) { tool = Tool.PEN }
            ToolChip("直线", Icons.Outlined.Remove, tool == Tool.LINE) { tool = Tool.LINE }
            ToolChip("矩形", Icons.Outlined.CropSquare, tool == Tool.RECT) { tool = Tool.RECT }
            ToolChip("圆", Icons.Outlined.Circle, tool == Tool.CIRCLE) { tool = Tool.CIRCLE }
            ToolChip("橡皮", Icons.Outlined.Clear, tool == Tool.ERASER) { tool = Tool.ERASER }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }) { Text("撤销") }
            OutlinedButton(onClick = { strokes.clear() }) { Text("清空批注") }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = process,
            onValueChange = { process = it },
            label = { Text("计算过程（写在这里，方便对照）") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            minLines = 5,
        )
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ToolChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Composable
private fun AnnotationCanvas(
    strokes: List<StrokeData>,
    tool: Tool,
    onStroke: (StrokeData) -> Unit,
    onErasePoint: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }
    Canvas(
        modifier = modifier.pointerInput(tool) {
            detectDragGestures(
                onDragStart = {
                    if (tool == Tool.ERASER) onErasePoint(it) else current = listOf(it)
                },
                onDrag = { change, _ ->
                    if (tool == Tool.ERASER) onErasePoint(change.position)
                    else current = current + change.position
                },
                onDragEnd = {
                    if (tool != Tool.ERASER && current.size >= 2) {
                        onStroke(StrokeData(tool, current, Color.Red, 5f))
                    }
                    current = emptyList()
                },
                onDragCancel = { current = emptyList() },
            )
        },
    ) {
        fun drawStroke(s: StrokeData) {
            when (s.tool) {
                Tool.PEN -> {
                    val path = Path().apply {
                        moveTo(s.points.first().x, s.points.first().y)
                        s.points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color = s.color, style = Stroke(width = s.width))
                }
                Tool.LINE -> drawLine(s.color, s.points.first(), s.points.last(), strokeWidth = s.width)
                Tool.RECT -> {
                    val a = s.points.first(); val b = s.points.last()
                    drawRect(s.color, topLeft = Offset(minOf(a.x, b.x), minOf(a.y, b.y)), size = androidx.compose.ui.geometry.Size(kotlin.math.abs(b.x - a.x), kotlin.math.abs(b.y - a.y)), style = Stroke(width = s.width))
                }
                Tool.CIRCLE -> {
                    val a = s.points.first(); val b = s.points.last()
                    val r = kotlin.math.hypot(b.x - a.x, b.y - a.y)
                    drawCircle(s.color, radius = r, center = a, style = Stroke(width = s.width))
                }
                Tool.ERASER -> Unit // 橡皮按整笔擦除，见 onErasePoint
            }
        }
        strokes.forEach { drawStroke(it) }
        if (current.size >= 2 && tool != Tool.ERASER) {
            drawStroke(StrokeData(tool, current, Color.Red, 5f))
        }
    }
}
