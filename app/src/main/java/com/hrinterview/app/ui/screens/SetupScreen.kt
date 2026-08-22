package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.CompetenceChip
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.components.SectionTitle
import com.hrinterview.app.ui.vm.SetupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    onStarted: () -> Unit,
    vm: SetupViewModel = viewModel()
) {
    val candidate by vm.candidate.collectAsStateWithLifecycle()
    val vacancy by vm.vacancy.collectAsStateWithLifecycle()
    val position by vm.positionType.collectAsStateWithLifecycle()
    val selected by vm.selectedIds.collectAsStateWithLifecycle()
    val competences by vm.competences.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let { snack.showSnackbar(it) }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader(
                title = "Новое интервью",
                subtitle = "Одинаковый набор компетенций даст сопоставимый набор вопросов для разных кандидатов на одну вакансию."
            )
            AppCard {
                SectionTitle("Кандидат")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = candidate,
                    onValueChange = { vm.candidate.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя или ФИО") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                SectionTitle("Вакансия")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vacancy,
                    onValueChange = { vm.vacancy.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Например: Мастер производства") },
                    singleLine = true
                )
            }
            AppCard {
                SectionTitle("Тип позиции")
                Spacer(Modifier.height(4.dp))
                Text(
                    "От типа зависят рекомендуемые компетенции и набор вопросов.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PositionType.entries.forEach { type ->
                        CompetenceChip(
                            text = type.title,
                            selected = position == type,
                            accent = position == type,
                            onClick = { vm.onPositionChange(type) }
                        )
                    }
                }
            }
            AppCard {
                SectionTitle("Компетенции")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Отметьте, что нужно оценить. Рекомендации зависят от типа позиции.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    competences.filter { it.isEnabled }.forEach { item ->
                        CompetenceChip(
                            text = item.name,
                            selected = item.id in selected,
                            onClick = { vm.toggleCompetence(item.id) }
                        )
                    }
                }
            }
            PrimaryAction("Начать интервью", onClick = {
                scope.launch {
                    if (vm.startInterview()) onStarted()
                }
            }, modifier = Modifier.fillMaxWidth())
        }
        SnackbarHost(snack)
    }
}
