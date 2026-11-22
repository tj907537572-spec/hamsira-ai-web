package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.data.model.FirstAidAnimationType
import com.example.data.model.FirstAidTopic
import com.example.data.model.FirstAidVideoScene
import com.example.ui.theme.LocalHamshiraStatusColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun FirstAidVideoPlayer(
    topic: FirstAidTopic,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val scenes = topic.videoScenes
    if (scenes.isEmpty()) return

    val statusColors = LocalHamshiraStatusColors.current
    val totalDurationMs = remember(topic.id, scenes) {
        scenes.sumOf { it.durationSeconds.toLong() * 1000L }.coerceAtLeast(1000L)
    }

    var isPlaying by remember(topic.id) { mutableStateOf(true) }
    var elapsedMs by remember(topic.id) { mutableLongStateOf(0L) }

    LaunchedEffect(topic.id, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        val tickMs = 50L
        while (isPlaying) {
            delay(tickMs)
            val next = elapsedMs + tickMs
            if (next >= totalDurationMs) {
                elapsedMs = 0L
            } else {
                elapsedMs = next
            }
        }
    }

    val (activeSceneIndex, activeScene) = remember(elapsedMs, scenes) {
        var acc = 0L
        var resolvedIdx = scenes.lastIndex
        for (i in scenes.indices) {
            acc += scenes[i].durationSeconds * 1000L
            if (elapsedMs < acc) {
                resolvedIdx = i
                break
            }
        }
        resolvedIdx to scenes[resolvedIdx]
    }

    fun jumpToScene(targetIndex: Int) {
        val clamped = targetIndex.coerceIn(0, scenes.lastIndex)
        var offsetMs = 0L
        for (i in 0 until clamped) {
            offsetMs += scenes[i].durationSeconds * 1000L
        }
        elapsedMs = offsetMs
    }

    val progress = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    val currentSec = (elapsedMs / 1000L).toInt()
    val totalSec = (totalDurationMs / 1000L).toInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                if (activeScene.isWarningScene) {
                    statusColors.urgent.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            ),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("first_aid_video_player")
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .clickable { isPlaying = !isPlaying }
                ) {
                    Image(
                        painter = painterResource(id = topic.imageRes),
                        contentDescription = topic.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (isPlaying) 0.88f else 0.72f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f),
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.14f),
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f)
                                    )
                                )
                            )
                    )

                    FirstAidMotionOverlay(
                        animationType = topic.animationType,
                        isPlaying = isPlaying,
                        isWarning = activeScene.isWarningScene,
                        primaryTint = MaterialTheme.colorScheme.primary,
                        urgentTint = statusColors.urgent,
                        onDarkTint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (activeScene.isWarningScene) {
                                statusColors.urgent
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = Color.White,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (activeScene.isWarningScene) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = strings.firstAidStepCounter(activeSceneIndex + 1, scenes.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${formatTimer(currentSec)} / ${formatTimer(totalSec)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = strings.firstAidPlayLabel,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = activeScene,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        label = "video_caption_anim"
                    ) { scene: FirstAidVideoScene ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = scene.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = scene.caption,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.95f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    color = if (activeScene.isWarningScene) statusColors.urgent else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("video_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) strings.firstAidPauseLabel else strings.firstAidPlayLabel,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                elapsedMs = 0L
                                isPlaying = true
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("video_replay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Replay,
                                contentDescription = strings.firstAidReplayLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { jumpToScene(activeSceneIndex - 1) },
                            enabled = activeSceneIndex > 0,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = { jumpToScene(activeSceneIndex + 1) },
                            enabled = activeSceneIndex < scenes.lastIndex,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = null
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        scenes.forEachIndexed { idx, scene ->
                            val isSelected = idx == activeSceneIndex
                            Surface(
                                onClick = { jumpToScene(idx) },
                                shape = CircleShape,
                                color = when {
                                    isSelected && scene.isWarningScene -> statusColors.urgent
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("video_step_pill_${idx + 1}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = strings.firstAidVideoPlayerHint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun FirstAidMotionOverlay(
    animationType: FirstAidAnimationType,
    isPlaying: Boolean,
    isWarning: Boolean,
    primaryTint: Color,
    urgentTint: Color,
    onDarkTint: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "first_aid_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val effectivePhase = if (isPlaying) phase else 0.35f
    val accent = if (isWarning) urgentTint else primaryTint

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.84f, h * 0.36f)
        val pulse = 0.5f + 0.5f * sin(effectivePhase * 2f * PI.toFloat())

        when (animationType) {
            FirstAidAnimationType.BURNS_COOL_WATER,
            FirstAidAnimationType.WOUNDS_CLEANSE -> {
                for (i in 0..3) {
                    val dropProgress = (effectivePhase + i * 0.25f) % 1f
                    val x = w * (0.76f + i * 0.055f)
                    val y = h * (0.16f + dropProgress * 0.38f)
                    drawLine(
                        color = onDarkTint.copy(alpha = 0.85f * (1f - dropProgress * 0.5f)),
                        start = Offset(x, y),
                        end = Offset(x, y + 14.dp.toPx()),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            FirstAidAnimationType.BLEEDING_PRESSURE -> {
                val r1 = 16.dp.toPx() + pulse * 14.dp.toPx()
                drawCircle(
                    color = accent.copy(alpha = 0.65f * (1f - pulse * 0.4f)),
                    radius = r1,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = onDarkTint.copy(alpha = 0.85f),
                    radius = 7.dp.toPx(),
                    center = center
                )
            }

            FirstAidAnimationType.CHOKING_HEIMLICH -> {
                val lift = effectivePhase * 22.dp.toPx()
                val startY = center.y + 18.dp.toPx() - lift
                val tipY = startY - 22.dp.toPx()
                drawLine(
                    color = onDarkTint.copy(alpha = 0.9f),
                    start = Offset(center.x, startY),
                    end = Offset(center.x, tipY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = onDarkTint.copy(alpha = 0.9f),
                    start = Offset(center.x - 8.dp.toPx(), tipY + 8.dp.toPx()),
                    end = Offset(center.x, tipY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = onDarkTint.copy(alpha = 0.9f),
                    start = Offset(center.x + 8.dp.toPx(), tipY + 8.dp.toPx()),
                    end = Offset(center.x, tipY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            FirstAidAnimationType.FAINTING_RECOVERY,
            FirstAidAnimationType.FEVER_CARE -> {
                val waveRadius = 14.dp.toPx() + effectivePhase * 22.dp.toPx()
                drawCircle(
                    color = onDarkTint.copy(alpha = 0.7f * (1f - effectivePhase)),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = accent.copy(alpha = 0.85f),
                    radius = 8.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

private fun formatTimer(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
