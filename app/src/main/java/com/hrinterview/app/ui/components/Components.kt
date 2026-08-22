package com.hrinterview.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hrinterview.app.ui.theme.BrandNavy
import com.hrinterview.app.ui.theme.BrandRed
import com.hrinterview.app.ui.theme.Success
import com.hrinterview.app.ui.theme.SuccessSoft
import com.hrinterview.app.ui.theme.Warning
import com.hrinterview.app.ui.theme.WarningSoft

val CardShape = RoundedCornerShape(20.dp)
val ButtonShape = RoundedCornerShape(14.dp)
val ChipShape = RoundedCornerShape(10.dp)

@Composable
fun CablePattern(
    modifier: Modifier = Modifier,
    navy: Color = BrandNavy,
    red: Color = BrandRed
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun strand(yRatio: Float, amplitude: Float, phase: Float, color: Color, stroke: Float) {
            val path = Path()
            val y0 = h * yRatio
            path.moveTo(-8f, y0)
            var x = 0f
            while (x <= w + 16f) {
                val y = y0 + kotlin.math.sin((x / w) * 6.28f + phase) * (h * amplitude)
                path.lineTo(x, y)
                x += 8f
            }
            drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        strand(0.22f, 0.10f, 0.2f, navy.copy(alpha = 0.16f), 2.2f)
        strand(0.38f, 0.14f, 1.1f, navy.copy(alpha = 0.22f), 2.6f)
        strand(0.52f, 0.12f, 2.4f, red.copy(alpha = 0.28f), 2.8f)
        strand(0.66f, 0.10f, 0.7f, navy.copy(alpha = 0.18f), 2.2f)
        strand(0.80f, 0.08f, 1.8f, navy.copy(alpha = 0.12f), 1.8f)
        drawCircle(navy.copy(alpha = 0.14f), radius = h * 0.18f, center = Offset(w * 0.12f, h * 0.50f), style = Stroke(width = 1.6f))
        drawCircle(red.copy(alpha = 0.22f), radius = h * 0.08f, center = Offset(w * 0.12f, h * 0.50f))
        drawCircle(navy.copy(alpha = 0.12f), radius = h * 0.16f, center = Offset(w * 0.88f, h * 0.34f), style = Stroke(width = 1.4f))
    }
}

@Composable
fun AccentLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.secondary)
    )
}

@Composable
fun BrandHero(
    title: String,
    subtitle: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(Modifier.fillMaxWidth()) {
            CablePattern(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.padding(20.dp)) {
                Spacer(Modifier.height(8.dp))
                AccentLine()
                Spacer(Modifier.height(12.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (action != null && onAction != null) {
                    Spacer(Modifier.height(18.dp))
                    PrimaryAction(action, onAction)
                }
            }
        }
    }
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(Modifier.fillMaxWidth()) {
            CablePattern(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                AccentLine()
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    val border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = colors,
            elevation = elevation,
            border = border
        ) { Column(Modifier.padding(18.dp), content = content) }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            colors = colors,
            elevation = elevation,
            border = border
        ) { Column(Modifier.padding(18.dp), content = content) }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = modifier)
}

@Composable
fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pulse: Boolean = false
) {
    val infinite = rememberInfiniteTransition(label = "ctaPulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.86f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ctaAlpha"
    )
    val container = MaterialTheme.colorScheme.primary.copy(alpha = if (pulse) pulseAlpha else 1f)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CompetenceChip(
    text: String,
    selected: Boolean = true,
    accent: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bg by animateColorAsState(
        when {
            selected && accent -> MaterialTheme.colorScheme.secondaryContainer
            selected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "chipBg"
    )
    val fg by animateColorAsState(
        when {
            selected && accent -> MaterialTheme.colorScheme.secondary
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "chipFg"
    )
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = Modifier
            .clip(ChipShape)
            .background(bg)
            .border(1.dp, if (selected && accent) MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline, ChipShape)
            .then(clickMod)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun StatusBadge(text: String, tone: BadgeTone) {
    val (bg, fg) = when (tone) {
        BadgeTone.Success -> SuccessSoft to Success
        BadgeTone.Warning -> WarningSoft to Warning
        BadgeTone.Neutral -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        BadgeTone.Accent -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg, maxLines = 1)
    }
}

enum class BadgeTone { Success, Warning, Neutral, Accent }

@Composable
fun InterviewProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total == 0) 0f else current.toFloat() / total.toFloat()
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Вопрос $current из $total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(6.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
fun CommentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (action != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryAction(action, onAction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ScoreScale(
    value: Int,
    onChange: (Int) -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "scorePulse")
    val glow by infinite.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scoreGlow"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { score ->
                val selected = value == score
                val bg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    label = "score"
                )
                val fg by animateColorAsState(
                    if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    label = "scoreFg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onChange(score) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.White.copy(alpha = glow))
                        )
                    }
                    Text("$score", color = fg, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("1 — слабый", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("3 — приемлемый", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("5 — сильный", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

fun formatScore(value: Float): String = String.format("%.1f", value).replace('.', ',')

fun scoreTone(value: Float): BadgeTone = when {
    value >= 4f -> BadgeTone.Success
    value >= 3f -> BadgeTone.Warning
    else -> BadgeTone.Accent
}
