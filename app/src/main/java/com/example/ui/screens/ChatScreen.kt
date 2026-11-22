package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPhone
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.MediaAttachmentHelper
import com.example.data.model.AppStrings
import com.example.data.model.ChatMessage
import com.example.data.model.ConsultationSession
import com.example.data.model.MediaAttachment
import com.example.data.model.QuickPrompt
import com.example.ui.ConsultationState
import com.example.ui.HamshiraUiState
import com.example.ui.components.StructuredMedicalText
import com.example.ui.theme.LocalHamshiraStatusColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    uiState: HamshiraUiState,
    strings: AppStrings,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    onQuickPromptClick: (QuickPrompt) -> Unit,
    onSelectSession: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onRetryClick: () -> Unit,
    onDismissError: () -> Unit,
    onToggleDisclaimer: (Boolean) -> Unit,
    onAttachMedia: (Uri, Boolean?) -> Unit,
    onAttachmentError: (String) -> Unit,
    onRemoveAttachment: () -> Unit,
    onDismissAttachmentNotice: () -> Unit,
    onOpenEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val isGenerating = uiState.consultationState is ConsultationState.Loading ||
        uiState.consultationState is ConsultationState.Streaming

    var showAttachSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraIsVideo by rememberSaveable { mutableStateOf(false) }
    var previewMediaMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAttachMedia(uri, null)
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uriStr = pendingCameraUriString
        pendingCameraUriString = null
        if (success && !uriStr.isNullOrBlank()) {
            onAttachMedia(Uri.parse(uriStr), false)
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val uriStr = pendingCameraUriString
        pendingCameraUriString = null
        if (success && !uriStr.isNullOrBlank()) {
            onAttachMedia(Uri.parse(uriStr), true)
        }
    }

    fun launchCamera(isVideo: Boolean) {
        val result = runCatching {
            val (uri, _) = MediaAttachmentHelper.createCameraOutputUri(context, isVideo)
            pendingCameraUriString = uri.toString()
            if (isVideo) {
                captureVideoLauncher.launch(uri)
            } else {
                takePhotoLauncher.launch(uri)
            }
        }
        if (result.isFailure) {
            pendingCameraUriString = null
            onAttachmentError(strings.attachmentFailed)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(pendingCameraIsVideo)
        } else {
            onAttachmentError(strings.cameraPermissionDenied)
        }
    }

    fun requestCameraAndCapture(isVideo: Boolean) {
        pendingCameraIsVideo = isVideo
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCamera(isVideo)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.consultationState) {
        val totalItems = uiState.messages.size +
            (if (isGenerating || uiState.consultationState is ConsultationState.Error) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            MedicalDisclaimerBanner(
                strings = strings,
                minimized = uiState.settings.disclaimerMinimized,
                onToggle = { onToggleDisclaimer(!uiState.settings.disclaimerMinimized) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.messages.isEmpty() && uiState.consultationState is ConsultationState.Idle) {
                    item(key = "welcome_hero") {
                        WelcomeHeroAndQuickPrompts(
                            strings = strings,
                            quickPrompts = uiState.quickPrompts,
                            recentSessions = uiState.sessions.take(3),
                            onQuickPromptClick = onQuickPromptClick,
                            onSelectSession = onSelectSession,
                            onOpenHistory = onOpenHistory,
                            onOpenEmergency = onOpenEmergency
                        )
                    }
                }

                items(
                    items = uiState.messages,
                    key = { msg -> "${msg.id}_${msg.timestamp}" }
                ) { message ->
                    if (message.isUser) {
                        UserMessageBubble(
                            message = message,
                            strings = strings,
                            onInspectAttachment = { previewMediaMessage = message }
                        )
                    } else {
                        AssistantMessageCard(
                            content = message.content,
                            strings = strings,
                            isStreaming = false,
                            onCopy = {
                                copyToClipboard(context, message.content, strings.copySuccess)
                            }
                        )
                    }
                }

                when (val state = uiState.consultationState) {
                    is ConsultationState.Loading -> {
                        item(key = "loading_indicator") {
                            AssistantThinkingCard(strings = strings)
                        }
                    }

                    is ConsultationState.Streaming -> {
                        item(key = "streaming_card") {
                            AssistantMessageCard(
                                content = state.partialText,
                                strings = strings,
                                isStreaming = true,
                                onCopy = {}
                            )
                        }
                    }

                    is ConsultationState.Error -> {
                        item(key = "error_card") {
                            ErrorBannerCard(
                                errorMessage = state.message,
                                strings = strings,
                                onRetry = onRetryClick,
                                onDismiss = onDismissError
                            )
                        }
                    }

                    ConsultationState.Idle -> Unit
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (uiState.attachmentNotice != null) {
                AttachmentNoticeBanner(
                    message = uiState.attachmentNotice,
                    onDismiss = onDismissAttachmentNotice,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            if (uiState.isImportingAttachment) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.pendingAttachment != null) {
                PendingAttachmentBar(
                    attachment = uiState.pendingAttachment,
                    strings = strings,
                    onRemove = onRemoveAttachment,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            ChatComposerBar(
                inputText = uiState.inputText,
                hasPendingAttachment = uiState.pendingAttachment != null,
                isGenerating = isGenerating,
                strings = strings,
                onInputChange = onInputChange,
                onAttachClick = { showAttachSheet = true },
                onSendClick = onSendClick,
                onStopClick = onStopClick
            )
        }
    }

    if (showAttachSheet) {
        MediaSourcePickerSheet(
            strings = strings,
            onDismiss = { showAttachSheet = false },
            onPickGallery = {
                showAttachSheet = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onTakePhoto = {
                showAttachSheet = false
                requestCameraAndCapture(isVideo = false)
            },
            onRecordVideo = {
                showAttachSheet = false
                requestCameraAndCapture(isVideo = true)
            }
        )
    }

    previewMediaMessage?.let { msg ->
        MediaPreviewDialog(
            message = msg,
            strings = strings,
            onDismiss = { previewMediaMessage = null }
        )
    }
}

@Composable
private fun MedicalDisclaimerBanner(
    strings: AppStrings,
    minimized: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColors = LocalHamshiraStatusColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .testTag("disclaimer_banner"),
        color = statusColors.warningContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, statusColors.warning.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HealthAndSafety,
                        contentDescription = null,
                        tint = statusColors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (minimized) strings.disclaimerShort else strings.disclaimerTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (minimized) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                    contentDescription = null,
                    tint = statusColors.warning,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = !minimized,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = strings.disclaimerBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeroAndQuickPrompts(
    strings: AppStrings,
    quickPrompts: List<QuickPrompt>,
    recentSessions: List<ConsultationSession>,
    onQuickPromptClick: (QuickPrompt) -> Unit,
    onSelectSession: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenEmergency: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nurse_avatar),
                    contentDescription = strings.welcomeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.welcomeTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.welcomeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        EmergencyQuickCard(
            strings = strings,
            onClick = onOpenEmergency
        )

        Text(
            text = strings.quickPromptsHeader,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quickPrompts.chunked(2).forEach { rowPrompts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPrompts.forEach { prompt ->
                        QuickPromptCard(
                            prompt = prompt,
                            onClick = { onQuickPromptClick(prompt) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowPrompts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (recentSessions.isNotEmpty()) {
            RecentConsultationsSection(
                strings = strings,
                sessions = recentSessions,
                onSelectSession = onSelectSession,
                onOpenHistory = onOpenHistory
            )
        }
    }
}

@Composable
private fun EmergencyQuickCard(
    strings: AppStrings,
    onClick: () -> Unit
) {
    val statusColors = LocalHamshiraStatusColors.current
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColors.urgentContainer.copy(alpha = 0.72f)
        ),
        border = BorderStroke(1.dp, statusColors.urgent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("emergency_quick_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColors.urgent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalHospital,
                    contentDescription = null,
                    tint = statusColors.urgent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.emergencyCardTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColors.urgent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.emergencyCardSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                color = statusColors.urgent,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalPhone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = strings.emergencyButtonShort,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentConsultationsSection(
    strings: AppStrings,
    sessions: List<ConsultationSession>,
    onSelectSession: (String) -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.historyRecentHeader,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("recent_view_all_history_button")
            ) {
                Text(
                    text = strings.historyViewAll,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        sessions.forEach { session ->
            val dateText = remember(session.updatedAt) {
                SimpleDateFormat("dd.MM • HH:mm", Locale.getDefault())
                    .format(Date(session.updatedAt))
            }
            Card(
                onClick = { onSelectSession(session.sessionId) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("recent_session_${session.sessionId}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$dateText • ${strings.historyMessagesCount(session.messageCount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPromptCard(
    prompt: QuickPrompt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("quick_prompt_${prompt.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = prompt.categoryBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    strings: AppStrings,
    onInspectAttachment: () -> Unit
) {
    val context = LocalContext.current
    val attachmentUri = message.attachmentUri
    val thumbnail by produceState<Bitmap?>(initialValue = null, attachmentUri, message.attachmentIsVideo) {
        value = if (attachmentUri != null) {
            withContext(Dispatchers.IO) {
                MediaAttachmentHelper.loadThumbnailBitmap(
                    context = context,
                    uriString = attachmentUri,
                    isVideo = message.attachmentIsVideo
                )
            }
        } else {
            null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 6.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.youLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                if (attachmentUri != null) {
                    Surface(
                        onClick = onInspectAttachment,
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 190.dp)
                            .testTag("message_attachment_preview")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail!!.asImageBitmap(),
                                    contentDescription = if (message.attachmentIsVideo) {
                                        strings.attachmentVideoBadge
                                    } else {
                                        strings.attachmentPhotoBadge
                                    },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (message.attachmentIsVideo) {
                                        Icons.Outlined.Videocam
                                    } else {
                                        Icons.Outlined.PhotoLibrary
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            if (message.attachmentIsVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayCircleFilled,
                                        contentDescription = strings.attachmentVideoBadge,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (message.attachmentIsVideo) {
                                        strings.attachmentVideoBadge
                                    } else {
                                        strings.attachmentPhotoBadge
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingAttachmentBar(
    attachment: MediaAttachment,
    strings: AppStrings,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, attachment.uriString, attachment.isVideo) {
        value = withContext(Dispatchers.IO) {
            MediaAttachmentHelper.loadThumbnailBitmap(
                context = context,
                uriString = attachment.uriString,
                isVideo = attachment.isVideo
            )
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pending_attachment_preview")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (attachment.isVideo) Icons.Outlined.Videocam else Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (attachment.isVideo) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircleFilled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (attachment.isVideo) strings.attachmentVideoBadge else strings.attachmentPhotoBadge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = listOfNotNull(
                        attachment.displayName.takeIf { it.isNotBlank() },
                        MediaAttachmentHelper.formatFileSize(attachment.sizeBytes)
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("remove_attachment_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.cancelLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttachmentNoticeBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColors = LocalHamshiraStatusColors.current
    Surface(
        color = statusColors.warningContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, statusColors.warning.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("attachment_notice_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = statusColors.warning,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaSourcePickerSheet(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("media_source_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = strings.attachSheetTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            MediaSourceOptionCard(
                icon = Icons.Outlined.PhotoLibrary,
                title = strings.attachFromGallery,
                subtitle = strings.attachFromGallerySub,
                onClick = onPickGallery,
                testTag = "attach_gallery_option"
            )
            MediaSourceOptionCard(
                icon = Icons.Outlined.PhotoCamera,
                title = strings.attachTakePhoto,
                subtitle = strings.attachTakePhotoSub,
                onClick = onTakePhoto,
                testTag = "attach_camera_photo_option"
            )
            MediaSourceOptionCard(
                icon = Icons.Outlined.Videocam,
                title = strings.attachRecordVideo,
                subtitle = strings.attachRecordVideoSub,
                onClick = onRecordVideo,
                testTag = "attach_camera_video_option"
            )
        }
    }
}

@Composable
private fun MediaSourceOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MediaPreviewDialog(
    message: ChatMessage,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriString = message.attachmentUri ?: return
    val bitmap by produceState<Bitmap?>(initialValue = null, uriString, message.attachmentIsVideo) {
        value = withContext(Dispatchers.IO) {
            if (message.attachmentIsVideo) {
                MediaAttachmentHelper.loadThumbnailBitmap(context, uriString, isVideo = true)
            } else {
                MediaAttachmentHelper.loadFullBitmap(context, uriString)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (message.attachmentIsVideo) {
                            strings.attachmentVideoBadge
                        } else {
                            strings.attachmentPhotoBadge
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.cancelLabel
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 380.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantMessageCard(
    content: String,
    strings: AppStrings,
    isStreaming: Boolean,
    onCopy: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.nurseLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!isStreaming) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("copy_answer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = strings.copyLabel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            StructuredMedicalText(markdown = content)
        }
    }
}

@Composable
private fun AssistantThinkingCard(strings: AppStrings) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = strings.statusThinking,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorBannerCard(
    errorMessage: String,
    strings: AppStrings,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val statusColors = LocalHamshiraStatusColors.current
    Surface(
        color = statusColors.urgentContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, statusColors.urgent.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("error_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = statusColors.urgent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = statusColors.urgent
                ),
                border = BorderStroke(1.dp, statusColors.urgent.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("retry_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = strings.retryLabel)
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    inputText: String,
    hasPendingAttachment: Boolean,
    isGenerating: Boolean,
    strings: AppStrings,
    onInputChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val canSend = inputText.isNotBlank() || hasPendingAttachment

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onAttachClick,
                enabled = !isGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("attach_media_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = strings.attachMediaDesc,
                    tint = if (!isGenerating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = strings.inputPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(22.dp),
                minLines = 1,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (!isGenerating && canSend) onSendClick()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                trailingIcon = {
                    if (inputText.isNotEmpty() && !isGenerating) {
                        TextButton(
                            onClick = { onInputChange("") },
                            modifier = Modifier.testTag("clear_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )

            Surface(
                onClick = {
                    if (isGenerating) onStopClick() else if (canSend) onSendClick()
                },
                enabled = isGenerating || canSend,
                shape = CircleShape,
                color = when {
                    isGenerating -> MaterialTheme.colorScheme.errorContainer
                    canSend -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .size(50.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("send_message_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.Send,
                        contentDescription = strings.sendContentDesc,
                        tint = when {
                            isGenerating -> MaterialTheme.colorScheme.error
                            canSend -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Hamshira AI", text))
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
