package com.tokilearn.app.ui

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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Subject
import kotlinx.coroutines.launch

/** 错题本页：按学科分；错题列表、可重做、单条移除、全部清除。 */
@Composable
fun WrongBookScreen(
    subject: Subject,
    store: LearnStore,
    onRedoAll: (Set<String>) -> Unit,
    onRedoOne: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val wrong by store.wrongIds(subject.id).collectAsState(initial = emptySet())
    val wrongQuestions = subject.questions.filter { it.id in wrong }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = "错题本 · ${subject.name}", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "共 ${wrongQuestions.size} 道错题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        if (wrongQuestions.isEmpty()) {
            Text(text = "还没有错题，做错的题会自动记在这里。", style = MaterialTheme.typography.bodyLarge)
        } else {
            Row {
                Button(onClick = { onRedoAll(wrong) }) { Text("重做全部") }
                Spacer(Modifier.height(0.dp))
                OutlinedButton(
                    onClick = { scope.launch { store.clearWrong(subject.id) } },
                    modifier = Modifier.padding(start = 12.dp),
                ) { Text("全部清除") }
            }
            Spacer(Modifier.height(16.dp))
            GroupContainer {
                wrongQuestions.forEachIndexed { index, question ->
                    if (index > 0) GroupDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(text = question.stem, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                            Text(
                                text = "${question.chapter} · ${question.source}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedButton(onClick = { onRedoOne(question.id) }) { Text("重做") }
                        IconButton(onClick = { scope.launch { store.removeWrong(subject.id, question.id) } }) {
                            Icon(Icons.Outlined.Close, contentDescription = "移出错题本")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}
