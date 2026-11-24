package com.tokilearn.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.LearnStore
import com.tokilearn.app.data.Subjects
import com.tokilearn.app.data.ThemeMode
import com.tokilearn.app.ui.HomeScreen
import com.tokilearn.app.ui.SettingsScreen
import com.tokilearn.app.ui.SubjectScreen
import com.tokilearn.app.ui.SubjectTab
import com.tokilearn.app.ui.theme.BottomTab
import com.tokilearn.app.ui.theme.GlassBottomBar
import com.tokilearn.app.ui.theme.TokiLearnTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

private enum class HomeTab { HOME, MINE }

/**
 * 两级导航：Home（学科 hub）→ Subject(subjectId)。
 * - 主页底栏：首页 / 我的。
 * - 学科底栏：考点 / 刷题 / 错题 / 我的（见 SubjectScreen）。
 * 加学科只需在 [Subjects] 里加一份数据，不用改导航和页面。
 */
@Composable
fun TokiLearnApp() {
    val context = LocalContext.current
    val store = remember { LearnStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val mode by store.themeMode.collectAsState(initial = ThemeMode.DARK)

    var currentSubjectId by remember { mutableStateOf<String?>(null) }
    var homeTab by remember { mutableStateOf(HomeTab.HOME) }
    var subjectInitialTab by remember { mutableStateOf(SubjectTab.KNOWLEDGE) }
    var quizFilter by remember { mutableStateOf<Set<String>?>(null) }

    TokiLearnTheme(mode = mode) {
        val subject = currentSubjectId?.let { Subjects.byId(it) }
        if (subject == null) {
            HomeLevel(
                store = store,
                homeTab = homeTab,
                onHomeTab = { homeTab = it },
                onOpenSubject = { id, openQuiz ->
                    scope.launch { store.setLastSubject(id) }
                    currentSubjectId = id
                    quizFilter = null
                    subjectInitialTab = if (openQuiz) SubjectTab.QUIZ else SubjectTab.KNOWLEDGE
                },
                onOpenSubjectWrongBook = { id ->
                    scope.launch { store.setLastSubject(id) }
                    currentSubjectId = id
                    quizFilter = null
                    subjectInitialTab = SubjectTab.WRONG
                },
            )
        } else {
            SubjectScreen(
                subject = subject,
                store = store,
                initialTab = subjectInitialTab,
                quizFilter = quizFilter,
                onQuizFilterConsumed = { quizFilter = null },
                onBackHome = { currentSubjectId = null },
            )
        }
    }
}

@Composable
private fun HomeLevel(
    store: LearnStore,
    homeTab: HomeTab,
    onHomeTab: (HomeTab) -> Unit,
    onOpenSubject: (String, Boolean) -> Unit,
    onOpenSubjectWrongBook: (String) -> Unit,
) {
    val hazeState = remember { HazeState() }
    Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (homeTab) {
                    HomeTab.HOME -> HomeScreen(
                        store = store,
                        onOpenSubject = onOpenSubject,
                        onOpenSubjectWrongBook = onOpenSubjectWrongBook,
                    )
                    HomeTab.MINE -> SettingsScreen(subject = null, store = store)
                }
            }
            Spacer(Modifier.height(8.dp))
            GlassBottomBar(
                hazeState = hazeState,
                tabs = listOf(
                    BottomTab("首页", Icons.Outlined.Home),
                    BottomTab("我的", Icons.Outlined.Person),
                ),
                selectedIndex = homeTab.ordinal,
                onSelect = { onHomeTab(HomeTab.entries[it]) },
                modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp),
            )
        }
    }
}
