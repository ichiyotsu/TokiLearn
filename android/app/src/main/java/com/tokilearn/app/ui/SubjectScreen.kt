package com.tokilearn.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Subject
import com.tokilearn.app.ui.theme.BottomTab
import com.tokilearn.app.ui.theme.GlassBottomBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

enum class SubjectTab { KNOWLEDGE, QUIZ, WRONG, MINE }

/**
 * 学科页（第二级导航）：底栏 考点 / 刷题 / 错题 / 我的。
 * [initialTab] / [quizFilter] 由首页“继续学习”“错题汇总”“重做”带入。
 */
@Composable
fun SubjectScreen(
    subject: Subject,
    store: LearnStore,
    initialTab: SubjectTab,
    quizFilter: Set<String>?,
    onQuizFilterConsumed: () -> Unit,
    onBackHome: () -> Unit,
) {
    val hazeState = remember { HazeState() }
    var tab by remember(subject.id) { mutableStateOf(initialTab) }
    var workbenchOpen by remember { mutableStateOf(false) }
    var filter by remember(subject.id) { mutableStateOf(quizFilter) }

    BackHandler(enabled = !workbenchOpen) { onBackHome() }

    if (workbenchOpen) {
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
            WorkbenchScreen(onBack = { workbenchOpen = false })
        }
        return
    }

    Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackHome) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "回首页")
                }
                Text(text = subject.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    text = subject.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(0.dp))
            }
            Box(Modifier.weight(1f)) {
                when (tab) {
                    SubjectTab.KNOWLEDGE -> KnowledgeScreen(subject)
                    SubjectTab.QUIZ -> QuizScreen(
                        subject = subject,
                        store = store,
                        onlyIds = filter,
                        onOpenWorkbench = { workbenchOpen = true },
                    )
                    SubjectTab.WRONG -> WrongBookScreen(
                        subject = subject,
                        store = store,
                        onRedoAll = { ids ->
                            filter = ids
                            onQuizFilterConsumed()
                            tab = SubjectTab.QUIZ
                        },
                        onRedoOne = { id ->
                            filter = setOf(id)
                            tab = SubjectTab.QUIZ
                        },
                    )
                    SubjectTab.MINE -> SettingsScreen(subject = subject, store = store)
                }
            }
            Spacer(Modifier.height(8.dp))
            GlassBottomBar(
                hazeState = hazeState,
                tabs = listOf(
                    BottomTab("考点", Icons.Outlined.Book),
                    BottomTab("刷题", Icons.Outlined.Quiz),
                    BottomTab("错题", Icons.Outlined.ErrorOutline),
                    BottomTab("我的", Icons.Outlined.Person),
                ),
                selectedIndex = tab.ordinal,
                onSelect = {
                    tab = SubjectTab.entries[it]
                    if (SubjectTab.entries[it] != SubjectTab.QUIZ) {
                        filter = null
                        onQuizFilterConsumed()
                    }
                },
                modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp),
            )
        }
    }
}
