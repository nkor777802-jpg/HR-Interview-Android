package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.domain.QuestionType
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.BadgeTone
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.CompetenceChip
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.components.SectionTitle
import com.hrinterview.app.ui.components.StatusBadge
import com.hrinterview.app.ui.vm.BankViewModel
import com.hrinterview.app.ui.vm.QuestionEditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankScreen(
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    vm: BankViewModel = viewModel()
) {
    val questions by vm.questions.collectAsStateWithLifecycle()
    val competences by vm.competences.collectAsStateWithLifecycle()
    val competenceFilter by vm.competenceFilter.collectAsStateWithLifecycle()
    val positionFilter by vm.positionFilter.collectAsStateWithLifecycle()
    val grouped = questions.groupBy { it.competenceName }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Добавить вопрос")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PageHeader(
                    title = "Банк вопросов",
                    subtitle = "Встроенные вопросы нельзя удалить пакетом. Их можно только отключить."
                )
            }
            item {
                SectionTitle("Тип позиции")
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompetenceChip("Все", selected = positionFilter == null, onClick = { vm.positionFilter.value = null })
                    PositionType.entries.forEach { type ->
                        CompetenceChip(
                            text = type.title,
                            selected = positionFilter == type,
                            accent = positionFilter == type,
                            onClick = { vm.positionFilter.value = type }
                        )
                    }
                }
            }
            item {
                SectionTitle("Компетенция")
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompetenceChip("Все", selected = competenceFilter == null, onClick = { vm.competenceFilter.value = null })
                    competences.forEach { item ->
                        CompetenceChip(
                            text = item.name,
                            selected = competenceFilter == item.id,
                            onClick = { vm.competenceFilter.value = item.id }
                        )
                    }
                }
            }
            grouped.forEach { (competence, list) ->
                item {
                    SectionTitle(competence)
                }
                items(list, key = { it.id }) { q ->
                    AppCard(onClick = if (!q.isBuiltIn) ({ onEdit(q.id) }) else null) {
                        Text(q.text, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${q.questionType.title} · ${q.positionTypes.joinToString { it.title }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                if (q.isBuiltIn) "Встроенный" else "Пользовательский",
                                if (q.isBuiltIn) BadgeTone.Neutral else BadgeTone.Accent
                            )
                            Spacer(Modifier.weight(1f))
                            Text("Активен", style = MaterialTheme.typography.labelMedium)
                            Switch(checked = q.isEnabled, onCheckedChange = { vm.toggle(q) })
                        }
                        if (!q.isBuiltIn) {
                            TextButton(onClick = { vm.deleteUser(q.id) }) { Text("Удалить") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionEditorScreen(
    questionId: String?,
    onDone: () -> Unit,
    vm: QuestionEditorViewModel = viewModel()
) {
    val text by vm.text.collectAsStateWithLifecycle()
    val competenceId by vm.competenceId.collectAsStateWithLifecycle()
    val type by vm.type.collectAsStateWithLifecycle()
    val positions by vm.positions.collectAsStateWithLifecycle()
    val enabled by vm.enabled.collectAsStateWithLifecycle()
    val builtIn by vm.isBuiltIn.collectAsStateWithLifecycle()
    val competences by vm.competences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(questionId) { vm.load(questionId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader(
            title = if (questionId == null) "Новый вопрос" else "Редактирование"
        )
        OutlinedTextField(
            value = text,
            onValueChange = { if (!builtIn || questionId != null) vm.text.value = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !builtIn,
            minLines = 4,
            label = { Text("Текст вопроса") }
        )
        SectionTitle("Компетенция")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            competences.forEach { item ->
                CompetenceChip(
                    text = item.name,
                    selected = competenceId == item.id,
                    onClick = { if (!builtIn) vm.competenceId.value = item.id }
                )
            }
        }
        SectionTitle("Тип вопроса")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestionType.entries.forEach { item ->
                CompetenceChip(
                    text = item.title,
                    selected = type == item,
                    accent = type == item,
                    onClick = { if (!builtIn) vm.type.value = item }
                )
            }
        }
        SectionTitle("Типы позиций")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PositionType.entries.forEach { item ->
                CompetenceChip(
                    text = item.title,
                    selected = item in positions,
                    onClick = {
                        if (!builtIn) {
                            vm.positions.value = if (item in positions) positions - item else positions + item
                        }
                    }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Активен", modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { vm.enabled.value = it })
        }
        PrimaryAction("Сохранить", onClick = {
            scope.launch { if (vm.save()) onDone() }
        }, modifier = Modifier.fillMaxWidth())
    }
}
