package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.domain.InterviewPlanner
import com.hrinterview.app.domain.InterviewSummary
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.BadgeTone
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.CommentField
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.components.SecondaryAction
import com.hrinterview.app.ui.components.SectionTitle
import com.hrinterview.app.ui.components.StatusBadge
import com.hrinterview.app.ui.components.formatScore
import com.hrinterview.app.ui.components.scoreTone
import com.hrinterview.app.ui.vm.ResultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    onSaved: () -> Unit,
    onNew: () -> Unit,
    onHome: () -> Unit,
    vm: ResultViewModel = viewModel()
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()
    val summary = session.toSummary(session.savedId ?: "draft")
    ResultContent(
        summary = summary,
        editable = true,
        saved = saved,
        onFinalComment = vm::setFinalComment,
        onSave = { vm.save(onSaved) },
        onNew = onNew,
        onHome = onHome
    )
}

@Composable
fun ResultContent(
    summary: InterviewSummary,
    editable: Boolean,
    saved: Boolean = true,
    onFinalComment: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onNew: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val scores = InterviewPlanner.competenceScores(summary.answers)
    val strengths = scores.take(2)
    val weak = scores.sortedBy { it.average }.take(2)
    val date = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(summary.createdAt))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader(title = "Итог интервью", subtitle = date)
        AppCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(summary.candidateName, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(summary.vacancy, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(summary.positionType.title, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge("${formatScore(summary.overallScore)} / 5", scoreTone(summary.overallScore))
            }
            Spacer(Modifier.height(16.dp))
            Text("Общая оценка", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${formatScore(summary.overallScore)} / 5",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        AppCard {
            SectionTitle("Оценки по компетенциям")
            Spacer(Modifier.height(12.dp))
            scores.forEach { item ->
                Column(Modifier.padding(bottom = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        StatusBadge(formatScore(item.average), scoreTone(item.average))
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.average / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
        AppCard {
            SectionTitle("Сильные стороны")
            Spacer(Modifier.height(8.dp))
            if (strengths.isEmpty()) Text("Недостаточно оценок")
            else strengths.forEach { Text("• ${it.name} — ${formatScore(it.average)}") }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Требуют дополнительной проверки")
            }
            Spacer(Modifier.height(8.dp))
            StatusBadge("Решение принимает HR", BadgeTone.Neutral)
            Spacer(Modifier.height(8.dp))
            Text(
                "Приложение не принимает решение о найме. Низкие оценки — повод уточнить факты на следующем этапе.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            if (weak.isEmpty()) Text("Недостаточно оценок")
            else weak.forEach { Text("• ${it.name} — ${formatScore(it.average)}") }
        }
        AppCard {
            SectionTitle("Итоговый комментарий HR")
            Spacer(Modifier.height(8.dp))
            if (editable) {
                CommentField(
                    value = summary.finalComment,
                    onValueChange = onFinalComment,
                    placeholder = "Ключевые выводы для себя и коллег..."
                )
            } else {
                Text(
                    summary.finalComment.ifBlank { "Комментарий не добавлен" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!editable) {
            AppCard {
                SectionTitle("Вопросы и оценки")
                Spacer(Modifier.height(12.dp))
                summary.answers.forEach { answer ->
                    Text("${answer.orderIndex + 1}. ${answer.questionText}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${answer.competenceName} · ${answer.questionType.title} · оценка ${answer.score}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (answer.comment.isNotBlank()) {
                        Text(answer.comment, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
        if (editable) {
            if (!saved) PrimaryAction("Сохранить результат", onSave, modifier = Modifier.fillMaxWidth())
            else Text("Результат сохранён на устройстве", color = MaterialTheme.colorScheme.primary)
            SecondaryAction("Новое интервью", onNew, modifier = Modifier.fillMaxWidth())
            SecondaryAction("На главную", onHome, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))
    }
}
