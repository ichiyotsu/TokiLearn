package com.tokilearn.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.GeminiClient
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Subject
import com.tokilearn.app.data.Subjects
import com.tokilearn.app.data.ThemeMode
import kotlinx.coroutines.launch

/**
 * 我的 / 设置页：主页底栏与学科底栏共用。
 * [subject] 为空时是主页级（显示全部学科的考试信息）；进入学科后显示该学科。
 * Gemini Key 只存本机 DataStore，请求直连 Google Gemini API。
 */
@Composable
fun SettingsScreen(subject: Subject?, store: LearnStore) {
    val scope = rememberCoroutineScope()
    val mode by store.themeMode.collectAsState(initial = ThemeMode.DARK)
    val savedKey by store.geminiKey.collectAsState(initial = "")
    val savedModel by store.geminiModel.collectAsState(initial = "gemini-2.0-flash")
    var keyInput by remember(savedKey) { mutableStateOf(savedKey) }
    var modelInput by remember(savedModel) { mutableStateOf(savedModel) }
    var savedNote by remember { mutableStateOf("") }
    var askInput by remember { mutableStateOf("") }
    var askResult by remember { mutableStateOf("") }
    var asking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = "我的", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Text(text = "外观", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        GroupContainer {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip("深色", mode == ThemeMode.DARK) { scope.launch { store.setThemeMode(ThemeMode.DARK) } }
                ThemeChip("浅色", mode == ThemeMode.LIGHT) { scope.launch { store.setThemeMode(ThemeMode.LIGHT) } }
                ThemeChip("跟随系统", mode == ThemeMode.SYSTEM) { scope.launch { store.setThemeMode(ThemeMode.SYSTEM) } }
            }
        }
        GroupSpacer()

        Text(text = "考试信息", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        val subjects = if (subject != null) listOf(subject) else Subjects.all
        subjects.forEach { s ->
            if (subject == null) {
                Text(text = s.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            GroupContainer {
                s.examInfo.forEachIndexed { i, (label, value) ->
                    if (i > 0) GroupDivider()
                    GroupRow(icon = Icons.Outlined.School, title = label, description = value)
                }
            }
            GroupSpacer()
        }

        Text(text = "Gemini 问答", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        GroupContainer {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Key 只存在本机，请求直连 Google，不经过任何服务器。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Gemini Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("模型名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    scope.launch {
                        store.setGeminiKey(keyInput)
                        store.setGeminiModel(modelInput)
                        savedNote = "已保存到本机。"
                    }
                }) { Text("保存") }
                if (savedNote.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = savedNote, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        GroupSpacer()

        Text(text = "问一问", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        GroupContainer {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = askInput,
                    onValueChange = { askInput = it },
                    label = { Text("输入问题${if (subject != null) "（${subject.name}）" else ""}") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        asking = true
                        askResult = ""
                        scope.launch {
                            val context = subject?.geminiContext ?: "你是学习助手。只依据教材与笔记内容回答，不确定的地方明说不知道，不要编造。"
                            val result = GeminiClient.ask(savedKey, savedModel, context, askInput)
                            askResult = result.getOrElse { "出错了：${it.message}" }
                            asking = false
                        }
                    },
                    enabled = !asking && askInput.isNotBlank(),
                ) { Text(if (asking) "问答中…" else "发送") }
                if (askResult.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(text = askResult, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        GroupSpacer()

        Text(text = "关于", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        GroupContainer {
            GroupRow(icon = Icons.Outlined.Info, title = "TokiLearn 0.1.0", description = "题目来源：杨可桢《机械设计基础》（第 7 版）笔记和课后习题详解（名校考研真题详解）与 TaskPro 笔记；其余题目待导入，绝不编造。")
            GroupDivider()
            GroupRow(icon = Icons.Outlined.Key, title = "本地存储", description = "做题进度、错题本、Gemini Key 全部只存本机。")
            GroupDivider()
            GroupRow(icon = Icons.Outlined.Palette, title = "设计", description = "TokiUI 0.2.0-b1 语义规范 + Material 3 Expressive。")
            GroupDivider()
            GroupRow(icon = Icons.Outlined.SmartToy, title = "AI", description = "Gemini 直连 Google API，不经过服务器。")
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
