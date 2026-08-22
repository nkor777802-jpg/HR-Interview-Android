package com.hrinterview.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.domain.ThemeMode
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.CompetenceChip
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.components.SectionTitle
import com.hrinterview.app.ui.vm.CompetencesViewModel
import com.hrinterview.app.ui.vm.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBank: () -> Unit,
    onCompetences: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader(title = "Настройки", subtitle = "Оформление, данные и документы")
        AppCard {
            SectionTitle("Оформление")
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    CompetenceChip(
                        text = mode.title,
                        selected = theme == mode,
                        accent = theme == mode,
                        onClick = { vm.setTheme(mode) }
                    )
                }
            }
        }
        AppCard {
            SectionTitle("Интервью")
            Spacer(Modifier.height(8.dp))
            SettingsRow("Банк вопросов", onBank)
            SettingsRow("Пользовательские компетенции", onCompetences)
        }
        AppCard {
            SectionTitle("Данные")
            Spacer(Modifier.height(8.dp))
            Text(
                "История интервью хранится только на этом устройстве.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            PrimaryAction("Очистить историю интервью", onClick = { confirmClear = true }, modifier = Modifier.fillMaxWidth())
        }
        AppCard {
            SectionTitle("О приложении")
            Spacer(Modifier.height(8.dp))
            SettingsRow("Пользовательское соглашение", onTerms)
            SettingsRow("Политика конфиденциальности", onPrivacy)
            Spacer(Modifier.height(8.dp))
            Text("Версия 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Удалить историю?") },
            text = { Text("Все сохранённые интервью, оценки и комментарии будут удалены с устройства. Банк вопросов останется.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearHistory { confirmClear = false }
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CompetencesScreen(vm: CompetencesViewModel = viewModel()) {
    val items by vm.items.collectAsStateWithLifecycle()
    val name by vm.newName.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PageHeader(title = "Компетенции")
        OutlinedTextField(
            value = name,
            onValueChange = { vm.newName.value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Новая компетенция") },
            singleLine = true
        )
        PrimaryAction("Добавить", onClick = vm::add, modifier = Modifier.fillMaxWidth())
        items.forEach { item ->
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (item.isBuiltIn) "Встроенная" else "Пользовательская",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (!item.isBuiltIn) {
                        TextButton(onClick = { vm.delete(item.id) }) { Text("Удалить") }
                    }
                }
            }
        }
    }
}
