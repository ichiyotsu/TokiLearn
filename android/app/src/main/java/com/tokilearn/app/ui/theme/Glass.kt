package com.tokilearn.app.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeFeatureFlags
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * 磨砂玻璃（TokiStudio 0.1.10 / Tokinote 同款配方）。
 * 内容源玻璃：底栏宽度变化时窗口背景采样会黑边，内容源采样不会。
 */
@OptIn(ExperimentalHazeApi::class)
object TokiBlurCapability {
    init {
        HazeFeatureFlags.isPlatformBackdropEnabled = true
    }

    val canSampleInWindow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

object TokiGlassParams {
    const val strongVeilAlpha: Float = 0.45f

    fun strongTints(dark: Boolean): List<HazeColorEffect> = if (dark) {
        listOf(HazeColorEffect.tint(Color.White.copy(alpha = 0.30f)))
    } else {
        listOf(
            HazeColorEffect.tint(Color(0xFF2A2622).copy(alpha = 0.16f)),
            HazeColorEffect.tint(Color.White.copy(alpha = 0.46f)),
        )
    }
}

@Composable
fun Modifier.tokiContentGlassStrong(state: HazeState): Modifier {
    val dark = LocalDarkTheme.current
    return hazeBlur(
        input = HazeInput.Sources(state),
        style = HazeBlurStyle {
            blurEnabled(true)
            blurRadius(32.dp)
            noiseFactor(0.02f)
            backgroundColor(Color.Transparent)
            colorEffects(TokiGlassParams.strongTints(dark))
            fallbackColorEffect(HazeColorEffect.tint(Color.Black.copy(alpha = if (dark) 0.45f else 0.30f)))
        },
        performanceMode = HazePerformanceMode.Quality,
    )
}

data class BottomTab(val label: String, val icon: ImageVector)

/**
 * Tokinote 同款 50% 磨砂玻璃胶囊悬浮底栏：内容源强磨砂（32dp 模糊 +
 * 45% surface veil）+ 圆角胶囊；选中项是实心 pill。
 * 主页底栏（首页 / 我的）与学科底栏（考点 / 刷题 / 错题 / 我的）共用。
 */
@Composable
fun GlassBottomBar(
    hazeState: HazeState,
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50)
    val frosted = TokiBlurCapability.canSampleInWindow
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .then(
                if (frosted) {
                    Modifier.tokiContentGlassStrong(hazeState)
                        .background(scheme.surfaceContainer.copy(alpha = TokiGlassParams.strongVeilAlpha))
                } else {
                    Modifier.background(scheme.surfaceContainer.copy(alpha = 0.5f))
                },
            )
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.45f), shape),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .clip(shape)
                        .background(if (selected) scheme.primary else Color.Transparent)
                        .clickable { onSelect(index) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) scheme.onPrimary else scheme.onSurface,
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) scheme.onPrimary else scheme.onSurface,
                    )
                }
            }
        }
    }
}
