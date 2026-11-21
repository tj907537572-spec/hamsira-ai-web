package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.NetworkMonitor
import com.example.data.ai.HamshiraAiRepository
import com.example.data.ai.NurseAiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRepository
import com.example.data.local.SettingsDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.CareGuide
import com.example.data.model.CareGuidesCatalog
import com.example.data.model.ChatMessage
import com.example.data.model.ConsultationSession
import com.example.data.model.GuideCategory
import com.example.data.model.QuickPrompt
import com.example.data.model.ResponseStyle
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "HamshiraViewModel"

sealed interface ConsultationState {
    data object Idle : ConsultationState
    data object Loading : ConsultationState
    data class Streaming(val partialText: String) : ConsultationState
    data class Error(val message: String, val failedPrompt: String) : ConsultationState
}

data class HamshiraUiState(
    val sessionId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ConsultationSession> = emptyList(),
    val filteredSessions: List<ConsultationSession> = emptyList(),
    val totalMessageCount: Int = 0,
    val historySearchQuery: String = "",
    val isHistorySheetOpen: Boolean = false,
    val consultationState: ConsultationState = ConsultationState.Idle,
    val settings: UserSettings = UserSettings(),
    val isOnline: Boolean = true,
    val inputText: String = "",
    val quickPrompts: List<QuickPrompt> = emptyList(),
    val guides: List<CareGuide> = emptyList(),
    val selectedCategory: GuideCategory = GuideCategory.ALL,
    val guideSearchQuery: String = "",
    val selectedGuide: CareGuide? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HamshiraViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val aiClient: NurseAiClient
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        chatRepository = ChatRepository(AppDatabase.getInstance(application)),
        aiClient = HamshiraAiRepository()
    )

    private val settingsStore = SettingsDataStore(application)
    private val networkMonitor = NetworkMonitor(application)

    private val _sessionId = MutableStateFlow(UUID.randomUUID().toString())
    private val _consultationState = MutableStateFlow<ConsultationState>(ConsultationState.Idle)
    private val _inputText = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(GuideCategory.ALL)
    private val _guideSearchQuery = MutableStateFlow("")
    private val _selectedGuide = MutableStateFlow<CareGuide?>(null)
    private val _historySearchQuery = MutableStateFlow("")
    private val _isHistorySheetOpen = MutableStateFlow(false)

    private var activeGenerationJob: Job? = null

    private val sessionMessagesFlow = _sessionId.flatMapLatest { id ->
        chatRepository.observeSessionMessages(id)
    }

    private val allSessionsFlow = chatRepository.observeAllSessions()

    private val filteredSessionsFlow = _historySearchQuery.flatMapLatest { query ->
        chatRepository.observeAllSessions(query)
    }

    private val historyBundleFlow = combine(
        allSessionsFlow,
        filteredSessionsFlow,
        chatRepository.observeTotalMessageCount(),
        _historySearchQuery,
        _isHistorySheetOpen
    ) { allSessions, filteredSessions, totalCount, query, isOpen ->
        HistoryUiBundle(
            allSessions = allSessions,
            filteredSessions = filteredSessions,
            totalMessageCount = totalCount,
            searchQuery = query,
            isSheetOpen = isOpen
        )
    }

    val uiState: StateFlow<HamshiraUiState> = combine(
        _sessionId,
        sessionMessagesFlow,
        historyBundleFlow,
        _consultationState,
        settingsStore.settingsFlow,
        networkMonitor.isOnline,
        _inputText,
        _selectedCategory,
        _guideSearchQuery,
        _selectedGuide
    ) { values ->
        val sessionId = values[0] as String
        @Suppress("UNCHECKED_CAST")
        val messages = values[1] as List<ChatMessage>
        val historyBundle = values[2] as HistoryUiBundle
        val consultationState = values[3] as ConsultationState
        val settings = values[4] as UserSettings
        val isOnline = values[5] as Boolean
        val inputText = values[6] as String
        val selectedCategory = values[7] as GuideCategory
        val searchQuery = values[8] as String
        val selectedGuide = values[9] as CareGuide?

        val allGuides = CareGuidesCatalog.careGuides(settings.language)
        val filteredGuides = allGuides.filter { guide ->
            val matchesCategory = selectedCategory == GuideCategory.ALL || guide.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                guide.title.contains(q, ignoreCase = true) ||
                guide.summary.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        HamshiraUiState(
            sessionId = sessionId,
            messages = messages,
            sessions = historyBundle.allSessions,
            filteredSessions = historyBundle.filteredSessions,
            totalMessageCount = historyBundle.totalMessageCount,
            historySearchQuery = historyBundle.searchQuery,
            isHistorySheetOpen = historyBundle.isSheetOpen,
            consultationState = consultationState,
            settings = settings,
            isOnline = isOnline,
            inputText = inputText,
            quickPrompts = CareGuidesCatalog.quickPrompts(settings.language),
            guides = filteredGuides,
            selectedCategory = selectedCategory,
            guideSearchQuery = searchQuery,
            selectedGuide = selectedGuide
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HamshiraUiState()
    )

    init {
        viewModelScope.launch {
            val latestSession = chatRepository.getLatestSessionId()
            if (!latestSession.isNullOrBlank()) {
                _sessionId.value = latestSession
            }
        }
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendCurrentInput() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        _inputText.value = ""
        sendPrompt(text)
    }

    fun sendQuickPrompt(promptText: String) {
        if (promptText.isBlank()) return
        sendPrompt(promptText.trim())
    }

    fun retryFailedPrompt() {
        val errorState = _consultationState.value as? ConsultationState.Error ?: return
        sendPromptInternal(errorState.failedPrompt, recordUserMessage = false)
    }

    fun dismissError() {
        if (_consultationState.value is ConsultationState.Error) {
            _consultationState.value = ConsultationState.Idle
        }
    }

    fun stopGenerating() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        val partial = (_consultationState.value as? ConsultationState.Streaming)?.partialText
        if (!partial.isNullOrBlank()) {
            val currentSession = _sessionId.value
            viewModelScope.launch {
                chatRepository.addMessage(currentSession, isUser = false, content = partial)
            }
        }
        _consultationState.value = ConsultationState.Idle
    }

    fun startNewConsultation() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _consultationState.value = ConsultationState.Idle
        _sessionId.value = UUID.randomUUID().toString()
    }

    fun openHistorySheet() {
        _isHistorySheetOpen.value = true
    }

    fun closeHistorySheet() {
        _isHistorySheetOpen.value = false
    }

    fun onHistorySearchChanged(query: String) {
        _historySearchQuery.value = query
    }

    fun selectSession(sessionId: String) {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _consultationState.value = ConsultationState.Idle
        _sessionId.value = sessionId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_sessionId.value == sessionId) {
                val nextLatest = chatRepository.getLatestSessionId()
                _sessionId.value = nextLatest ?: UUID.randomUUID().toString()
            }
        }
    }

    fun clearAllHistory() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _consultationState.value = ConsultationState.Idle
        viewModelScope.launch {
            chatRepository.clearAllHistory()
            _sessionId.value = UUID.randomUUID().toString()
        }
    }

    private fun sendPrompt(prompt: String) {
        sendPromptInternal(prompt, recordUserMessage = true)
    }

    private fun sendPromptInternal(prompt: String, recordUserMessage: Boolean) {
        if (activeGenerationJob?.isActive == true) return

        val currentSession = _sessionId.value
        val currentSettings = uiState.value.settings

        activeGenerationJob = viewModelScope.launch {
            try {
                val historySnapshot = chatRepository.getSessionMessages(currentSession)
                if (recordUserMessage) {
                    chatRepository.addMessage(
                        sessionId = currentSession,
                        isUser = true,
                        content = prompt
                    )
                }

                _consultationState.value = ConsultationState.Loading

                var latestAccumulated = ""
                aiClient.streamConsultation(
                    history = historySnapshot,
                    userPrompt = prompt,
                    language = currentSettings.language,
                    responseStyle = currentSettings.responseStyle
                ).collect { partial ->
                    latestAccumulated = partial
                    _consultationState.value = ConsultationState.Streaming(partial)
                }

                if (latestAccumulated.isNotBlank()) {
                    chatRepository.addMessage(
                        sessionId = currentSession,
                        isUser = false,
                        content = latestAccumulated
                    )
                }
                _consultationState.value = ConsultationState.Idle
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout while consulting Hamshira AI", e)
                _consultationState.value = ConsultationState.Error(
                    message = timeoutErrorMessage(currentSettings.language),
                    failedPrompt = prompt
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error while consulting Hamshira AI", e)
                _consultationState.value = ConsultationState.Error(
                    message = e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: genericErrorMessage(currentSettings.language),
                    failedPrompt = prompt
                )
            }
        }
    }

    fun selectCategory(category: GuideCategory) {
        _selectedCategory.value = category
    }

    fun onGuideSearchChanged(query: String) {
        _guideSearchQuery.value = query
    }

    fun openGuide(guide: CareGuide) {
        _selectedGuide.value = guide
    }

    fun closeGuide() {
        _selectedGuide.value = null
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsStore.setLanguage(language) }
    }

    fun updateResponseStyle(style: ResponseStyle) {
        viewModelScope.launch { settingsStore.setResponseStyle(style) }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun toggleDisclaimerMinimized(minimized: Boolean) {
        viewModelScope.launch { settingsStore.setDisclaimerMinimized(minimized) }
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }

    private fun timeoutErrorMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Вақти интизории ҷавоб ба охир расид (60 сония). Пайвастро санҷед ва аз нав кӯшиш кунед."
        AppLanguage.RU -> "Превышено время ожидания ответа (60 сек). Проверьте соединение и повторите попытку."
        AppLanguage.UZ_LATN -> "Javob kutish vaqti tugadi (60 soniya). Internet aloqasini tekshirib, qayta urinib ko'ring."
    }

    private fun genericErrorMessage(language: AppLanguage): String = when (language) {
        AppLanguage.TG -> "Аз модули AI ҷавоб гирифта нашуд. Пайвасти интернетро санҷед ва аз нав кӯшиш кунед."
        AppLanguage.RU -> "Не удалось получить ответ от AI-модуля. Проверьте подключение и повторите попытку."
        AppLanguage.UZ_LATN -> "AI modulidan javob olib bo'lmadi. Aloqani tekshirib, qayta urinib ko'ring."
    }
}

private data class HistoryUiBundle(
    val allSessions: List<ConsultationSession>,
    val filteredSessions: List<ConsultationSession>,
    val totalMessageCount: Int,
    val searchQuery: String,
    val isSheetOpen: Boolean
)
