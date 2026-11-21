package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalHamshiraStatusColors

private sealed interface MedicalBlock {
    data class Heading(val text: String) : MedicalBlock
    data class Bullet(val text: String) : MedicalBlock
    data class Callout(val kind: CalloutKind, val text: String) : MedicalBlock
    data class Paragraph(val text: String) : MedicalBlock
}

private enum class CalloutKind {
    ADVICE, WARNING, URGENT
}

@Composable
fun StructuredMedicalText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMedicalBlocks(markdown) }
    val statusColors = LocalHamshiraStatusColors.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MedicalBlock.Heading -> {
                    Text(
                        text = formatInlineBold(block.text),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                is MedicalBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formatInlineBold(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MedicalBlock.Callout -> {
                    val containerColor: Color
                    val accentColor: Color
                    val icon: ImageVector
                    when (block.kind) {
                        CalloutKind.ADVICE -> {
                            containerColor = statusColors.successContainer.copy(alpha = 0.65f)
                            accentColor = statusColors.success
                            icon = Icons.Rounded.CheckCircle
                        }
                        CalloutKind.WARNING -> {
                            containerColor = statusColors.warningContainer.copy(alpha = 0.7f)
                            accentColor = statusColors.warning
                            icon = Icons.Rounded.WarningAmber
                        }
                        CalloutKind.URGENT -> {
                            containerColor = statusColors.urgentContainer.copy(alpha = 0.75f)
                            accentColor = statusColors.urgent
                            icon = Icons.Rounded.ErrorOutline
                        }
                    }

                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = accentColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formatInlineBold(block.text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                is MedicalBlock.Paragraph -> {
                    Text(
                        text = formatInlineBold(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun parseMedicalBlocks(raw: String): List<MedicalBlock> {
    val lines = raw.lines()
    val result = mutableListOf<MedicalBlock>()

    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue

        when {
            line.startsWith("###") || line.startsWith("##") || line.startsWith("# ") -> {
                val clean = line.trimStart('#').trim()
                if (clean.isNotEmpty()) result.add(MedicalBlock.Heading(clean))
            }

            line.startsWith("[СРОЧНО К ВРАЧУ]") || line.startsWith("[ФАВРАН БА ДУХТУР]") || line.startsWith("🚨") -> {
                val clean = line
                    .removePrefix("[СРОЧНО К ВРАЧУ]")
                    .removePrefix("[ФАВРАН БА ДУХТУР]")
                    .removePrefix("🚨")
                    .trim().trimStart(':').trim()
                if (clean.isNotEmpty()) result.add(MedicalBlock.Callout(CalloutKind.URGENT, clean))
            }

            line.startsWith("[ВНИМАНИЕ]") || line.startsWith("[ДИҚҚАТ]") || line.startsWith("⚠️") -> {
                val clean = line
                    .removePrefix("[ВНИМАНИЕ]")
                    .removePrefix("[ДИҚҚАТ]")
                    .removePrefix("⚠️")
                    .trim().trimStart(':').trim()
                if (clean.isNotEmpty()) result.add(MedicalBlock.Callout(CalloutKind.WARNING, clean))
            }

            line.startsWith("[СОВЕТ]") || line.startsWith("[МАСЛИҲАТ]") || line.startsWith("✅") -> {
                val clean = line
                    .removePrefix("[СОВЕТ]")
                    .removePrefix("[МАСЛИҲАТ]")
                    .removePrefix("✅")
                    .trim().trimStart(':').trim()
                if (clean.isNotEmpty()) result.add(MedicalBlock.Callout(CalloutKind.ADVICE, clean))
            }

            line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                val clean = line.drop(2).trim()
                if (clean.isNotEmpty()) result.add(MedicalBlock.Bullet(clean))
            }

            else -> {
                result.add(MedicalBlock.Paragraph(line))
            }
        }
    }
    return result
}

private fun formatInlineBold(text: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            val start = text.indexOf("**", index)
            if (start == -1) {
                append(text.substring(index))
                break
            }
            if (start > index) {
                append(text.substring(index, start))
            }
            val end = text.indexOf("**", start + 2)
            if (end == -1) {
                append(text.substring(start))
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(text.substring(start + 2, end))
            }
            index = end + 2
        }
    }
}
