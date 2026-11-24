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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Question
import com.tokilearn.app.data.QuestionType
import com.tokilearn.app.data.Subject
import com.tokilearn.app.data.isChoiceCorrect
import com.tokilearn.app.data.isNumericCorrect
import kotlinx.coroutines.launch

/**
 * 刷题页：按学科取题；选择题 / 判断题 / 计算题（填最终结果按容差自动判）/
 * 画图题（看参考答案后自判）。做题进度与错题按学科存 DataStore。
 */
@Composable
fun QuizScreen(
    subject: Subject,
    store: LearnStore,
    onlyIds: Set<String>? = null,
    onOpenWorkbench: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val questions = remember(subject, onlyIds) {
        if (onlyIds == null) subject.questions else subject.questions.filter { it.id in onlyIds }
    }
    var index by remember(subject, onlyIds) { mutableIntStateOf(0) }
    var selected by remember(subject, onlyIds) { mutableIntStateOf(-1) }
    var input by remember(subject, onlyIds) { mutableStateOf("") }
    var graded by remember(subject, onlyIds) { mutableStateOf(false) }
    var correct by remember(subject, onlyIds) { mutableStateOf(false) }
    val answered by store.answeredIds(subject.id).collectAsState(initial = emptySet())
    val correctIds by store.correctIds(subject.id).collectAsState(initial = emptySet())

    val question = questions.getOrNull(index)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "刷题 · ${subject.name}", style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = onOpenWorkbench) { Text("工作台") }
        }
        Text(
            text = if (questions.isEmpty()) "暂无题目" else "已答 ${answered.size}/${subject.questions.size} · 答对 ${correctIds.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        if (question == null) {
            Text(
                text = if (onlyIds != null) "错题已全部重做完。" else if (answered.isNotEmpty()) "本轮做完了，去错题本看看做错的题。" else "本学科暂无题目。",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            QuestionCard(
                question = question,
                position = "${index + 1}/${questions.size}",
                selected = selected,
                onSelect = { if (!graded) selected = it },
                input = input,
                onInput = { if (!graded) input = it },
                graded = graded,
                correct = correct,
            )
            Spacer(Modifier.height(16.dp))
            if (!graded) {
                when (question.type) {
                    QuestionType.DRAWING -> {
                        Text(
                            text = "画完后点“看参考答案”，对照自判。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { graded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("看参考答案")
                        }
                    }
                    else -> {
                        val canSubmit = when (question.type) {
                            QuestionType.CALCULATION -> input.toDoubleOrNull() != null
                            else -> selected >= 0
                        }
                        Button(
                            onClick = {
                                correct = when (question.type) {
                                    QuestionType.CALCULATION -> isNumericCorrect(
                                        input.toDouble(),
                                        question.numericAnswer ?: 0.0,
                                        question.tolerance,
                                    )
                                    else -> isChoiceCorrect(selected, question.answerIndex)
                                }
                                graded = true
                                scope.launch { store.recordAnswer(subject.id, question.id, correct) }
                            },
                            enabled = canSubmit,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("提交") }
                    }
                }
            } else {
                if (question.type == QuestionType.DRAWING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                scope.launch { store.recordAnswer(subject.id, question.id, true) }
                                if (index + 1 < questions.size) { index++; selected = -1; input = ""; graded = false } else { index++ }
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("我答对了") }
                        OutlinedButton(
                            onClick = {
                                scope.launch { store.recordAnswer(subject.id, question.id, false) }
                                if (index + 1 < questions.size) { index++; selected = -1; input = ""; graded = false } else { index++ }
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("我答错了") }
                    }
                } else {
                    Button(
                        onClick = {
                            if (index + 1 < questions.size) { index++; selected = -1; input = ""; graded = false } else { index++ }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (index + 1 < questions.size) "下一题" else "完成") }
                }
            }
        }
        if (subject.pendingTopics.isNotEmpty()) {
            GroupSpacer()
            Text(text = "待导入", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            GroupContainer {
                subject.pendingTopics.forEachIndexed { i, topic ->
                    if (i > 0) GroupDivider()
                    GroupRow(icon = Icons.Outlined.Edit, title = topic, description = "题目尚未导入，不显示假题。")
                }
            }
            GroupSpacer()
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun QuestionCard(
    question: Question,
    position: String,
    selected: Int,
    onSelect: (Int) -> Unit,
    input: String,
    onInput: (String) -> Unit,
    graded: Boolean,
    correct: Boolean,
) {
    Text(text = "$position · ${question.chapter}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    Text(text = question.stem, style = MaterialTheme.typography.titleLarge)
    Text(
        text = "来源：${question.source}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    when (question.type) {
        QuestionType.SINGLE_CHOICE, QuestionType.TRUE_FALSE -> {
            question.options.forEachIndexed { i, option ->
                val isAnswer = graded && i == question.answerIndex
                val isWrongPick = graded && i == selected && i != question.answerIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected == i, onClick = { onSelect(i) }, role = Role.RadioButton)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (isAnswer) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = when {
                            isAnswer -> MaterialTheme.colorScheme.primary
                            isWrongPick -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(Modifier.height(0.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp),
                        color = if (isWrongPick) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        QuestionType.CALCULATION -> {
            OutlinedTextField(
                value = input,
                onValueChange = onInput,
                label = { Text("最终结果${if (question.unit.isNotEmpty()) "（${question.unit}）" else ""}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !graded,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        QuestionType.DRAWING -> {
            Text(
                text = "在工作台上传题目图、画图作答；也可以直接纸上作图。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (graded) {
        Spacer(Modifier.height(16.dp))
        if (question.type != QuestionType.DRAWING) {
            Text(
                text = if (correct) "答对了" else "答错了",
                style = MaterialTheme.typography.titleMedium,
                color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
        if (question.type == QuestionType.DRAWING) {
            Text(text = "参考答案", style = MaterialTheme.typography.titleMedium)
            Text(text = question.referenceAnswer, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
        }
        if (question.explanation.isNotEmpty()) {
            Text(text = "解析", style = MaterialTheme.typography.titleMedium)
            Text(text = question.explanation, style = MaterialTheme.typography.bodyLarge)
        }
        if (question.type == QuestionType.CALCULATION && question.numericAnswer != null) {
            Text(
                text = "参考答案：${question.numericAnswer}${question.unit}（容差 ±${question.tolerance}）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
