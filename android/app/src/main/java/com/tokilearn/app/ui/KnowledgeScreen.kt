package com.tokilearn.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tokilearn.app.data.Subject

/** 考点页：按学科的章节分组列表；内容只用笔记与教材原文，绝不编造。 */
@Composable
fun KnowledgeScreen(subject: Subject) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = subject.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subject.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        subject.chapters.forEach { chapter ->
            Text(text = chapter.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = chapter.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            GroupContainer {
                chapter.items.forEachIndexed { index, item ->
                    var expanded by remember { mutableStateOf(false) }
                    if (index > 0) GroupDivider()
                    GroupRow(
                        icon = Icons.AutoMirrored.Outlined.Article,
                        title = item.title,
                        description = item.detail,
                        onClick = { expanded = !expanded },
                        expanded = expanded,
                    )
                }
            }
            chapter.pendingNote?.let { note ->
                Spacer(Modifier.height(8.dp))
                GroupContainer {
                    GroupRow(icon = Icons.Outlined.Warning, title = "未完待续", description = note)
                }
            }
            GroupSpacer()
        }
    }
}
