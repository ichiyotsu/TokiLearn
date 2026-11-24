package com.tokilearn.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Subjects

/** 首页：学科列表 + 继续学习入口 + 跨学科错题汇总（有错题时才显示）。 */
@Composable
fun HomeScreen(
    store: LearnStore,
    onOpenSubject: (subjectId: String, openQuiz: Boolean) -> Unit,
    onOpenSubjectWrongBook: (subjectId: String) -> Unit,
) {
    val lastSubjectId by store.lastSubjectId.collectAsState(initial = null)
    val lastSubject = lastSubjectId?.let { Subjects.byId(it) } ?: Subjects.all.firstOrNull()
    // 固定顺序订阅全部学科的进度（Subjects.all 是静态表，调用次数不变）。
    val answeredBySubject = Subjects.all.map { subject ->
        store.answeredIds(subject.id).collectAsState(initial = emptySet()).value
    }
    val lastAnswered = lastSubject?.let { subject ->
        answeredBySubject.getOrNull(Subjects.all.indexOf(subject)) ?: emptySet()
    } ?: emptySet()
    val wrongCounts = Subjects.all.map { subject ->
        subject to store.wrongIds(subject.id).collectAsState(initial = emptySet()).value.size
    }
    val totalWrong = wrongCounts.sumOf { it.second }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = "TokiLearn", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "多学科学起来。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        if (lastSubject != null) {
            Text(text = "继续学习", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            GroupContainer {
                GroupRow(
                    icon = Icons.Outlined.PlayArrow,
                    title = lastSubject.name,
                    description = if (lastAnswered.isEmpty()) "还没开始做题，点这里开始刷题。" else "已答 ${lastAnswered.size}/${lastSubject.questions.size} 道，点这里继续刷题。",
                    onClick = { onOpenSubject(lastSubject.id, true) },
                )
            }
            GroupSpacer()
        }

        Text(text = "学科", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        GroupContainer {
            Subjects.all.forEachIndexed { index, subject ->
                if (index > 0) GroupDivider()
                GroupRow(
                    icon = Icons.Outlined.School,
                    title = subject.name,
                    description = "${subject.subtitle} · ${subject.questions.size} 道题 · ${subject.chapters.size} 章考点",
                    onClick = { onOpenSubject(subject.id, false) },
                )
            }
        }
        GroupSpacer()

        if (totalWrong > 0) {
            Text(text = "错题汇总", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            GroupContainer {
                wrongCounts.filter { it.second > 0 }.forEachIndexed { index, (subject, count) ->
                    if (index > 0) GroupDivider()
                    GroupRow(
                        icon = Icons.Outlined.ErrorOutline,
                        title = subject.name,
                        description = "共 $count 道错题，点这里去重做。",
                        onClick = { onOpenSubjectWrongBook(subject.id) },
                    )
                }
            }
            GroupSpacer()
        }
        Spacer(Modifier.height(120.dp))
    }
}
