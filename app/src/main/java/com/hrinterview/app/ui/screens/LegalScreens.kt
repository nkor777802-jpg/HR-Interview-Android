package com.hrinterview.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hrinterview.app.ui.components.AppCard
import com.hrinterview.app.ui.components.PageHeader
import com.hrinterview.app.ui.components.PrimaryAction
import com.hrinterview.app.ui.legal.LegalTexts

@Composable
fun LegalDocumentScreen(
    title: String,
    body: String,
    primary: String? = null,
    onPrimary: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PageHeader(title = title)
        AppCard {
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (primary != null && onPrimary != null) {
            PrimaryAction(primary, onPrimary, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AgreementScreen(onAccept: () -> Unit) {
    LegalDocumentScreen(
        title = LegalTexts.AGREEMENT_TITLE,
        body = LegalTexts.AGREEMENT_BODY,
        primary = "Принимаю и продолжить",
        onPrimary = onAccept
    )
}
