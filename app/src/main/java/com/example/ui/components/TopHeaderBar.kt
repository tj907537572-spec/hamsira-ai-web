package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalHamshiraStatusColors

@Composable
fun TopHeaderBar(
    strings: AppStrings,
    isOnline: Boolean,
    isThinking: Boolean,
    showChatActions: Boolean,
    savedSessionsCount: Int,
    onHistoryClick: () -> Unit,
    onNewDialogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColors = LocalHamshiraStatusColors.current

    val dotColor by animateColorAsState(
        targetValue = when {
            !isOnline -> statusColors.warning
            isThinking -> MaterialTheme.colorScheme.primary
            else -> statusColors.success
        },
        label = "status_dot"
    )
    val badgeBg by animateColorAsState(
        targetValue = when {
            !isOnline -> statusColors.warningContainer
            isThinking -> MaterialTheme.colorScheme.primaryContainer
            else -> statusColors.successContainer
        },
        label = "status_bg"
    )
    val badgeTextColor by animateColorAsState(
        targetValue = when {
            !isOnline -> statusColors.warning
            isThinking -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> statusColors.success
        },
        label = "status_text"
    )

    val statusText = when {
        !isOnline -> strings.statusOffline
        isThinking -> strings.statusThinking
        else -> strings.statusOnline
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_nurse_avatar),
                        contentDescription = strings.appTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.appSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("connection_status_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTextColor,
                                maxLines = 1
                            )
                        }
                    }

                    if (showChatActions) {
                        IconButton(
                            onClick = onHistoryClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("open_history_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (savedSessionsCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(text = savedSessionsCount.coerceAtMost(99).toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = strings.historyOpenDesc,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onNewDialogClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("new_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddComment,
                                contentDescription = strings.newDialog,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )
        }
    }
}
