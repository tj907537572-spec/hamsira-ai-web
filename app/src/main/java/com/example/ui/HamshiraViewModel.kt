package com.example.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AttachmentImportResult
import com.example.core.MediaAttachmentHelper
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
import com.example.data.model.FirstAidCatalog
import com.example.data.model.FirstAidMediaFilter
import com.example.data.model.FirstAidTopic
import com.example.data.model.GuideCategory
import com.example.data.model.MediaAttachment
import com.example.data.model.QuickPrompt
import com.example.data.model.ResponseStyle
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import com.example.data.model.stringsFor
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
    data class Error(
        val message: String,
        val failedPrompt: String,
        val failedAttachment: MediaAttachment? = null
    ) : ConsultationState
}

data class HamshiraUiState(
    val sessionId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ConsultationSession> = emptyList(),
    val filteredSessions: List<ConsultationSession> = emptyList(),
    val totalMessageCount: Int = 0,
    val historySearchQuery: String = "",
    val isHistorySheetOpen: Boolean = false,
    val isEmergencySheetOpen: Boolean = false,
    val consultationState: ConsultationState = ConsultationState.Idle,
    val settings: UserSettings = UserSettings(),
    val isOnline: Boolean = true,
    val inputText: String = "",
    val pendingAttachment: MediaAttachment? = null,
    val isImportingAttachment: Boolean = false,
    val attachmentNotice: String? = null,
    val quickPrompts: List<QuickPrompt> = emptyList(),
    val guides: List<CareGuide> = emptyList(),
    val selectedCategory: GuideCategory = GuideCategory.ALL,
    val guideSearchQuery: String = "",
    val selectedGuide: CareGuide? = null,
    val firstAidTopics: List<FirstAidTopic> = emptyList(),
    val firstAidFilter: FirstAidMediaFilter = FirstAidMediaFilter.ALL,
    val firstAidSearchQuery: String = "",
    val selectedFirstAidTopic: FirstAidTopic? = null,
    val firstAidStartInVideoMode: Boolean = true
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
    private val _isEmergencySheetOpen = MutableStateFlow(false)
    private val _pendingAttachment = MutableStateFlow<MediaAttachment?>(null)
    private val _isImportingAttachment = MutableStateFlow(false)
    private val _attachmentNotice = MutableStateFlow<String?>(null)
    private val _firstAidFilter = MutableStateFlow(FirstAidMediaFilter.ALL)
    private val _firstAidSearchQuery = MutableStateFlow("")
    private val _selectedFirstAidTopic = MutableStateFlow<FirstAidTopic?>(null)
    private val _firstAidStartInVideoMode = MutableStateFlow(true)

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

    private val mediaAndEmergencyBundleFlow = combine(
        _pendingAttachment,
        _isImportingAttachment,
        _attachmentNotice,
        _isEmergencySheetOpen
    ) { pendingAttachment, isImporting, notice, isEmergencyOpen ->
        MediaAndEmergencyBundle(
            pendingAttachment = pendingAttachment,
            isImportingAttachment = isImporting,
            attachmentNotice = notice,
            isEmergencySheetOpen = isEmergencyOpen
        )
    }

    private val firstAidBundleFlow = combine(
        _firstAidFilter,
        _firstAidSearchQuery,
        _selectedFirstAidTopic,
        _firstAidStartInVideoMode
    ) { filter, query, selectedTopic, startInVideo ->
        FirstAidUiBundle(
            filter = filter,
            searchQuery = query,
            selectedTopic = selectedTopic,
            startInVideoMode = startInVideo
        )
    }

    private val guidesBundleFlow = combine(
        _selectedCategory,
        _guideSearchQuery,
        _selectedGuide
    ) { category, query, selectedGuide ->
        GuidesUiBundle(
            selectedCategory = category,
            searchQuery = query,
            selectedGuide = selectedGuide
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
        guidesBundleFlow,
        mediaAndEmergencyBundleFlow,
        firstAidBundleFlow
    ) { values ->
        val sessionId = values[0] as String
        @Suppress("UNCHECKED_CAST")
        val messages = values[1] as List<ChatMessage>
        val historyBundle = values[2] as HistoryUiBundle
        val consultationState = values[3] as ConsultationState
        val settings = values[4] as UserSettings
        val isOnline = values[5] as Boolean
        val inputText = values[6] as String
        val guidesBundle = values[7] as GuidesUiBundle
        val mediaAndEmergency = values[8] as MediaAndEmergencyBundle
        val firstAidBundle = values[9] as FirstAidUiBundle

        val selectedCategory = guidesBundle.selectedCategory
        val searchQuery = guidesBundle.searchQuery
        val selectedGuide = guidesBundle.selectedGuide

        val allGuides = CareGuidesCatalog.careGuides(settings.language)
        val filteredGuides = allGuides.filter { guide ->
            val matchesCategory = selectedCategory == GuideCategory.ALL || guide.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                guide.title.contains(q, ignoreCase = true) ||
                guide.summary.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val allFirstAid = FirstAidCatalog.topics(settings.language)
        val faQuery = firstAidBundle.searchQuery.trim()
        val filteredFirstAid = allFirstAid.filter { topic ->
            faQuery.isEmpty() ||
                topic.title.contains(faQuery, ignoreCase = true) ||
                topic.shortBadge.contains(faQuery, ignoreCase = true) ||
                topic.summary.contains(faQuery, ignoreCase = true)
        }

        HamshiraUiState(
            sessionId = sessionId,
            messages = messages,
            sessions = historyBundle.allSessions,
            filteredSessions = historyBundle.filteredSessions,
            totalMessageCount = historyBundle.totalMessageCount,
            historySearchQuery = historyBundle.searchQuery,
            isHistorySheetOpen = historyBundle.isSheetOpen,
            isEmergencySheetOpen = mediaAndEmergency.isEmergencySheetOpen,
            consultationState = consultationState,
            settings = settings,
            isOnline = isOnline,
            inputText = inputText,
            pendingAttachment = mediaAndEmergency.pendingAttachment,
            isImportingAttachment = mediaAndEmergency.isImportingAttachment,
            attachmentNotice = mediaAndEmergency.attachmentNotice,
            quickPrompts = CareGuidesCatalog.quickPrompts(settings.language),
            guides = filteredGuides,
            selectedCategory = selectedCategory,
            guideSearchQuery = searchQuery,
            selectedGuide = selectedGuide,
            firstAidTopics = filteredFirstAid,
            firstAidFilter = firstAidBundle.filter,
            firstAidSearchQuery = firstAidBundle.searchQuery,
            selectedFirstAidTopic = firstAidBundle.selectedTopic,
            firstAidStartInVideoMode = firstAidBundle.startInVideoMode
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

    fun attachMediaFromUri(uri: Uri, forcedIsVideo: Boolean? = null) {
        viewModelScope.launch {
            _isImportingAttachment.value = true
            _attachmentNotice.value = null
            val strings = stringsFor(uiState.value.settings.language)
            when (val result = MediaAttachmentHelper.importFromUri(getApplication(), uri, forcedIsVideo)) {
                is AttachmentImportResult.Success -> {
                    _pendingAttachment.value = result.attachment
                }
                AttachmentImportResult.FileTooLarge -> {
                    _attachmentNotice.value = strings.attachmentTooLarge
                }
                AttachmentImportResult.UnsupportedOrFailed -> {
                    _attachmentNotice.value = strings.attachmentFailed
                }
            }
            _isImportingAttachment.value = false
        }
    }

    fun attachResolvedMedia(attachment: MediaAttachment) {
        _attachmentNotice.value = null
        _pendingAttachment.value = attachment
    }

    fun removePendingAttachment() {
        _pendingAttachment.value = null
    }

    fun clearAttachmentNotice() {
        _attachmentNotice.value = null
    }

    fun setAttachmentNotice(message: String) {
        _attachmentNotice.value = message
    }

    fun openEmergencySheet() {
        _isEmergencySheetOpen.value = true
    }

    fun closeEmergencySheet() {
        _isEmergencySheetOpen.value = false
    }

    fun sendCurrentInput() {
        if (activeGenerationJob?.isActive == true) return
        val rawText = _inputText.value.trim()
        val attachment = _pendingAttachment.value
        if (rawText.isEmpty() && attachment == null) return

        val prompt = rawText.ifEmpty { stringsFor(uiState.value.settings.language).defaultMediaPrompt }
        _inputText.value = ""
        _pendingAttachment.value = null
        _attachmentNotice.value = null
        sendPromptInternal(prompt = prompt, attachment = attachment, recordUserMessage = true)
    }

    fun sendQuickPrompt(promptText: String) {
        if (promptText.isBlank()) return
        sendPromptInternal(prompt = promptText.trim(), attachment = null, recordUserMessage = true)
    }

    fun retryFailedPrompt() {
        val errorState = _consultationState.value as? ConsultationState.Error ?: return
        sendPromptInternal(
            prompt = errorState.failedPrompt,
            attachment = errorState.failedAttachment,
            recordUserMessage = false
        )
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
        _pendingAttachment.value = null
        _attachmentNotice.value = null
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
        _pendingAttachment.value = null
        _attachmentNotice.value = null
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
        _pendingAttachment.value = null
        _attachmentNotice.value = null
        viewModelScope.launch {
            chatRepository.clearAllHistory()
            _sessionId.value = UUID.randomUUID().toString()
        }
    }

    private fun sendPromptInternal(
        prompt: String,
        attachment: MediaAttachment?,
        recordUserMessage: Boolean
    ) {
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
                        content = prompt,
                        attachment = attachment
                    )
                }

                _consultationState.value = ConsultationState.Loading

                var latestAccumulated = ""
                aiClient.streamConsultation(
                    history = historySnapshot,
                    userPrompt = prompt,
                    language = currentSettings.language,
                    responseStyle = currentSettings.responseStyle,
                    attachment = attachment
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
                    failedPrompt = prompt,
                    failedAttachment = attachment
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error while consulting Hamshira AI", e)
                _consultationState.value = ConsultationState.Error(
                    message = e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: genericErrorMessage(currentSettings.language),
                    failedPrompt = prompt,
                    failedAttachment = attachment
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

    fun selectFirstAidFilter(filter: FirstAidMediaFilter) {
        _firstAidFilter.value = filter
    }

    fun onFirstAidSearchChanged(query: String) {
        _firstAidSearchQuery.value = query
    }

    fun openFirstAidTopic(topic: FirstAidTopic, startInVideoMode: Boolean) {
        _firstAidStartInVideoMode.value = startInVideoMode
        _selectedFirstAidTopic.value = topic
    }

    fun closeFirstAidTopic() {
        _selectedFirstAidTopic.value = null
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
        AppLanguage.TG -> "Вақти интизории ҷавоб ба охир расид. Пайвастро санҷед ва аз нав кӯшиш кунед."
        AppLanguage.RU -> "Превышено время ожидания ответа. Проверьте соединение и повторите попытку."
        AppLanguage.UZ_LATN -> "Javob kutish vaqti tugadi. Internet aloqasini tekshirib, qayta urinib ko'ring."
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

private data class MediaAndEmergencyBundle(
    val pendingAttachment: MediaAttachment?,
    val isImportingAttachment: Boolean,
    val attachmentNotice: String?,
    val isEmergencySheetOpen: Boolean
)

private data class GuidesUiBundle(
    val selectedCategory: GuideCategory,
    val searchQuery: String,
    val selectedGuide: CareGuide?
)

private data class FirstAidUiBundle(
    val filter: FirstAidMediaFilter,
    val searchQuery: String,
    val selectedTopic: FirstAidTopic?,
    val startInVideoMode: Boolean
)
