package com.hrinterview.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpeakerNotes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.hrinterview.app.ui.theme.BrandNavy
import com.hrinterview.app.ui.theme.BrandNavySoft
import com.hrinterview.app.ui.theme.BrandRed
import com.hrinterview.app.ui.theme.TextSecondary
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hrinterview.app.ui.legal.LegalTexts
import com.hrinterview.app.ui.screens.BankScreen
import com.hrinterview.app.ui.screens.CompetencesScreen
import com.hrinterview.app.ui.screens.DetailScreen
import com.hrinterview.app.ui.screens.HistoryScreen
import com.hrinterview.app.ui.screens.HomeScreen
import com.hrinterview.app.ui.screens.LegalDocumentScreen
import com.hrinterview.app.ui.screens.QuestionEditorScreen
import com.hrinterview.app.ui.screens.ResultScreen
import com.hrinterview.app.ui.screens.SessionScreen
import com.hrinterview.app.ui.screens.SettingsScreen
import com.hrinterview.app.ui.screens.SetupScreen

object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val SESSION = "session"
    const val RESULT = "result"
    const val HISTORY = "history"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
    const val BANK = "bank"
    const val BANK_NEW = "bank_new"
    const val BANK_EDIT = "bank_edit/{id}"
    const val COMPETENCES = "competences"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Главная", Icons.Outlined.Home),
    Tab(Routes.SETUP, "Интервью", Icons.Outlined.SpeakerNotes),
    Tab(Routes.HISTORY, "История", Icons.Outlined.History),
    Tab(Routes.SETTINGS, "Настройки", Icons.Outlined.Settings)
)

@Composable
fun HrNavHost() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = current in tabs.map { it.route }
    val start = Routes.HOME

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selected) {
                                        Box(
                                            Modifier
                                                .padding(bottom = 2.dp)
                                                .width(16.dp)
                                                .height(2.dp)
                                                .background(BrandRed, RoundedCornerShape(2.dp))
                                        )
                                    }
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandNavy,
                                selectedTextColor = BrandNavy,
                                indicatorColor = BrandNavySoft,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStart = { nav.navigate(Routes.SETUP) },
                    onHistory = { nav.navigate(Routes.HISTORY) },
                    onBank = { nav.navigate(Routes.BANK) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpen = { nav.navigate("detail/$it") }
                )
            }
            composable(Routes.SETUP) {
                SetupScreen(onStarted = { nav.navigate(Routes.SESSION) })
            }
            composable(Routes.SESSION) {
                SessionScreen(onFinished = {
                    nav.navigate(Routes.RESULT) {
                        popUpTo(Routes.SETUP)
                    }
                })
            }
            composable(Routes.RESULT) {
                ResultScreen(
                    onSaved = {},
                    onNew = {
                        nav.navigate(Routes.SETUP) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onHome = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onOpen = { nav.navigate("detail/$it") })
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                DetailScreen(interviewId = entry.arguments?.getString("id").orEmpty())
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBank = { nav.navigate(Routes.BANK) },
                    onCompetences = { nav.navigate(Routes.COMPETENCES) },
                    onTerms = { nav.navigate(Routes.TERMS) },
                    onPrivacy = { nav.navigate(Routes.PRIVACY) }
                )
            }
            composable(Routes.BANK) {
                BankScreen(
                    onAdd = { nav.navigate(Routes.BANK_NEW) },
                    onEdit = { nav.navigate("bank_edit/$it") }
                )
            }
            composable(Routes.BANK_NEW) {
                QuestionEditorScreen(questionId = null, onDone = { nav.popBackStack() })
            }
            composable(
                Routes.BANK_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                QuestionEditorScreen(
                    questionId = entry.arguments?.getString("id"),
                    onDone = { nav.popBackStack() }
                )
            }
            composable(Routes.COMPETENCES) { CompetencesScreen() }
            composable(Routes.TERMS) {
                LegalDocumentScreen(LegalTexts.AGREEMENT_TITLE, LegalTexts.AGREEMENT_BODY)
            }
            composable(Routes.PRIVACY) {
                LegalDocumentScreen(LegalTexts.PRIVACY_TITLE, LegalTexts.PRIVACY_BODY)
            }
        }
    }
}
