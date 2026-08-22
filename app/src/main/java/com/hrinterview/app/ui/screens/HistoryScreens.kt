package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.EmptyState
import com.hrinterview.app.ui.vm.DetailViewModel
import com.hrinterview.app.ui.vm.HistoryViewModel

@Composable
fun HistoryScreen(
    onOpen: (String) -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PageHeader(title = "История", subtitle = "Сохранённые интервью на устройстве")
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по кандидату или вакансии") },
                singleLine = true
            )
        }
        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = "История пока пуста",
                subtitle = "Сохранённые интервью появятся здесь."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    InterviewMiniCard(item) { onOpen(item.id) }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(interviewId: String, vm: DetailViewModel = viewModel()) {
    val item by vm.interview.collectAsStateWithLifecycle()
    LaunchedEffect(interviewId) { vm.load(interviewId) }
    item?.let {
        ResultContent(summary = it, editable = false)
    }
}
