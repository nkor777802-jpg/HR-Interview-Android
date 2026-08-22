package com.hrinterview.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.CommentField
import com.hrinterview.app.ui.components.CompetenceChip
import com.hrinterview.app.ui.components.InterviewProgress
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.components.ScoreScale
import com.hrinterview.app.ui.components.SecondaryAction
import com.hrinterview.app.ui.components.SectionTitle
import com.hrinterview.app.ui.vm.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun SessionScreen(
    onFinished: () -> Unit,
    vm: SessionViewModel = viewModel()
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val commentBringIntoView = remember { BringIntoViewRequester() }
    val total = session.drafts.size.coerceAtLeast(1)
    val index = session.currentIndex.coerceIn(0, session.drafts.lastIndex.coerceAtLeast(0))
    val draft = session.drafts.getOrNull(index)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (index > 0) {
                        SecondaryAction("Назад", onClick = vm::back, modifier = Modifier.weight(1f))
                    }
                    PrimaryAction(
                        text = if (index >= total - 1) "Завершить интервью" else "Далее",
                        onClick = {
                            val finished = vm.nextOrFinish()
                            if (finished) onFinished()
                            else if (draft?.score !in 1..5) {
                                scope.launch { snack.showSnackbar("Сначала поставьте оценку от 1 до 5") }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        pulse = draft?.score in 1..5
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PageHeader(title = "Интервью", subtitle = session.candidateName.ifBlank { session.vacancy })
            InterviewProgress(current = index + 1, total = total)
            if (draft != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompetenceChip(draft.question.competenceName, selected = true, accent = false)
                    CompetenceChip(draft.question.questionType.title, selected = true, accent = true)
                }
                AppCard {
                    Text(draft.question.text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                AppCard {
                    SectionTitle("Оцените ответ")
                    Spacer(Modifier.height(12.dp))
                    ScoreScale(value = draft.score, onChange = vm::setScore)
                }
                AppCard {
                    SectionTitle("Комментарий HR")
                    Spacer(Modifier.height(8.dp))
                    CommentField(
                        value = draft.comment,
                        onValueChange = vm::setComment,
                        placeholder = "Зафиксируйте важные моменты ответа...",
                        modifier = Modifier
                            .bringIntoViewRequester(commentBringIntoView)
                            .onFocusChanged { focus ->
                                if (focus.isFocused) {
                                    scope.launch {
                                        commentBringIntoView.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
