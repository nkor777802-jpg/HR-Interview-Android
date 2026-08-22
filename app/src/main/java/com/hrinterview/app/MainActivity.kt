package com.hrinterview.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hrinterview.app.domain.ThemeMode
import com.hrinterview.app.ui.navigation.HrNavHost
import com.hrinterview.app.ui.screens.AgreementScreen
import com.hrinterview.app.ui.theme.HrInterviewTheme
import com.hrinterview.app.ui.vm.LegalViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = application.container.settings
        setContent {
            var theme by remember { mutableStateOf(ThemeMode.LIGHT) }
            var accepted by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                settings.themeMode.collect { theme = it }
            }
            LaunchedEffect(Unit) {
                settings.agreementAccepted.collect { accepted = it }
            }
            HrInterviewTheme(themeMode = theme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (accepted) {
                        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        false -> {
                            val legalVm: LegalViewModel = viewModel()
                            AgreementScreen { legalVm.accept { accepted = true } }
                        }
                        true -> HrNavHost()
                    }
                }
            }
        }
    }
}
