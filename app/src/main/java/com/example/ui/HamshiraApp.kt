package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ThemeMode
import com.example.data.model.stringsFor
import com.example.ui.components.ConsultationHistorySheet
import com.example.ui.components.EmergencyAmbulanceSheet
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FirstAidScreen
import com.example.ui.screens.GuidesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HamshiraAiTheme

const val ROUTE_CHAT = "chat"
const val ROUTE_FIRST_AID = "first_aid"
const val ROUTE_GUIDES = "guides"
const val ROUTE_SETTINGS = "settings"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun HamshiraApp(
    viewModel: HamshiraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = stringsFor(uiState.settings.language)

    val useDarkTheme = when (uiState.settings.themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    HamshiraAiTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val isChatRoute = currentDestination?.route == ROUTE_CHAT

        val navItems = listOf(
            NavDestinationItem(
                route = ROUTE_CHAT,
                label = strings.navChat,
                selectedIcon = Icons.AutoMirrored.Filled.Chat,
                unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
                testTag = "nav_chat"
            ),
            NavDestinationItem(
                route = ROUTE_FIRST_AID,
                label = strings.navFirstAid,
                selectedIcon = Icons.Filled.MedicalServices,
                unselectedIcon = Icons.Outlined.MedicalServices,
                testTag = "nav_first_aid"
            ),
            NavDestinationItem(
                route = ROUTE_GUIDES,
                label = strings.navGuides,
                selectedIcon = Icons.Filled.MenuBook,
                unselectedIcon = Icons.Outlined.MenuBook,
                testTag = "nav_guides"
            ),
            NavDestinationItem(
                route = ROUTE_SETTINGS,
                label = strings.navSettings,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                testTag = "nav_settings"
            )
        )

        val isThinking = uiState.consultationState is ConsultationState.Loading ||
            uiState.consultationState is ConsultationState.Streaming

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.navigationBars,
                topBar = {
                    TopHeaderBar(
                        strings = strings,
                        isOnline = uiState.isOnline,
                        isThinking = isThinking,
                        showChatActions = isChatRoute,
                        savedSessionsCount = uiState.sessions.size,
                        onHistoryClick = viewModel::openHistorySheet,
                        onNewDialogClick = viewModel::startNewConsultation,
                        onEmergencyClick = viewModel::openEmergencySheet
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_CHAT,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(ROUTE_CHAT) {
                                ChatScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onInputChange = viewModel::onInputChanged,
                                    onSendClick = viewModel::sendCurrentInput,
                                    onStopClick = viewModel::stopGenerating,
                                    onQuickPromptClick = { prompt ->
                                        viewModel.sendQuickPrompt(prompt.prompt)
                                    },
                                    onSelectSession = viewModel::selectSession,
                                    onOpenHistory = viewModel::openHistorySheet,
                                    onRetryClick = viewModel::retryFailedPrompt,
                                    onDismissError = viewModel::dismissError,
                                    onToggleDisclaimer = viewModel::toggleDisclaimerMinimized,
                                    onAttachMedia = viewModel::attachMediaFromUri,
                                    onAttachmentError = viewModel::setAttachmentNotice,
                                    onRemoveAttachment = viewModel::removePendingAttachment,
                                    onDismissAttachmentNotice = viewModel::clearAttachmentNotice,
                                    onOpenEmergency = viewModel::openEmergencySheet
                                )
                            }

                            composable(ROUTE_FIRST_AID) {
                                FirstAidScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onSearchChange = viewModel::onFirstAidSearchChanged,
                                    onSelectFilter = viewModel::selectFirstAidFilter,
                                    onOpenTopic = viewModel::openFirstAidTopic,
                                    onCloseTopic = viewModel::closeFirstAidTopic,
                                    onOpenEmergency = viewModel::openEmergencySheet,
                                    onAskNurseAboutTopic = { topic ->
                                        viewModel.startNewConsultation()
                                        viewModel.sendQuickPrompt(topic.consultPrompt)
                                        navigateToTab(ROUTE_CHAT)
                                    }
                                )
                            }

                            composable(ROUTE_GUIDES) {
                                GuidesScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onSearchChange = viewModel::onGuideSearchChanged,
                                    onSelectCategory = viewModel::selectCategory,
                                    onOpenGuide = viewModel::openGuide,
                                    onCloseGuide = viewModel::closeGuide,
                                    onAskNurseAboutGuide = { guide ->
                                        viewModel.startNewConsultation()
                                        viewModel.sendQuickPrompt(guide.consultPrompt)
                                        navigateToTab(ROUTE_CHAT)
                                    }
                                )
                            }

                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onLanguageChange = viewModel::updateLanguage,
                                    onResponseStyleChange = viewModel::updateResponseStyle,
                                    onThemeModeChange = viewModel::updateThemeMode,
                                    onOpenHistory = viewModel::openHistorySheet,
                                    onClearHistory = viewModel::clearAllHistory
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isHistorySheetOpen) {
                ConsultationHistorySheet(
                    sessions = uiState.filteredSessions,
                    activeSessionId = uiState.sessionId,
                    searchQuery = uiState.historySearchQuery,
                    strings = strings,
                    onSearchChange = viewModel::onHistorySearchChanged,
                    onSelectSession = { sessionId ->
                        viewModel.selectSession(sessionId)
                        navigateToTab(ROUTE_CHAT)
                    },
                    onDeleteSession = viewModel::deleteSession,
                    onNewConsultation = {
                        viewModel.startNewConsultation()
                        navigateToTab(ROUTE_CHAT)
                    },
                    onDismiss = viewModel::closeHistorySheet
                )
            }

            if (uiState.isEmergencySheetOpen) {
                EmergencyAmbulanceSheet(
                    strings = strings,
                    onDismiss = viewModel::closeEmergencySheet
                )
            }
        }
    }
}
