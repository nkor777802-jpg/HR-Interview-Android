package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.domain.InterviewSummary
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.BrandHero
import com.hrinterview.app.ui.components.EmptyState
import com.hrinterview.app.ui.components.StatusBadge
import com.hrinterview.app.ui.components.formatScore
import com.hrinterview.app.ui.components.scoreTone
import com.hrinterview.app.ui.vm.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onStart: () -> Unit,
    onHistory: () -> Unit,
    onBank: () -> Unit,
    onSettings: () -> Unit,
    onOpen: (String) -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val interviews by vm.interviews.collectAsStateWithLifecycle()
    val count by vm.count.collectAsStateWithLifecycle()
    val recent = interviews.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BrandHero(
                title = "HR Интервью",
                subtitle = "Структурированное интервью без лишних записей",
                action = "Начать интервью",
                onAction = onStart
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AppCard(modifier = Modifier.weight(1f), onClick = onHistory) {
                    Text("Проведено", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("$count", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                AppCard(modifier = Modifier.weight(1f), onClick = onBank) {
                    Icon(Icons.Outlined.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Банк вопросов", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Компетенции", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Последние интервью", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                if (interviews.isNotEmpty()) {
                    TextButton(onClick = onHistory) { Text("Все") }
                }
            }
        }
        if (recent.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Forum,
                    title = "Здесь появятся результаты ваших интервью",
                    subtitle = "Начните с первого структурированного разговора — оценки и заметки сохранятся на устройстве.",
                    action = "Провести первое интервью",
                    onAction = onStart
                )
            }
        } else {
            items(recent, key = { it.id }) { item ->
                InterviewMiniCard(item, onClick = { onOpen(item.id) })
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun InterviewMiniCard(item: InterviewSummary, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(item.createdAt))
    AppCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(item.candidateName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(item.vacancy, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge("${formatScore(item.overallScore)} / 5", scoreTone(item.overallScore))
        }
        Spacer(Modifier.height(12.dp))
        Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
