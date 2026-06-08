package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.Space

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )
    val brush = Brush.linearGradient(
        colors = listOf(Pieno.colors.paperDim, Pieno.colors.hairline, Pieno.colors.paperDim),
        start = Offset(p * 700f - 350f, 0f),
        end = Offset(p * 700f, 0f),
    )
    Box(modifier.clip(shape).background(brush))
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Space.s7),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Pieno.colors.paperDim),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Pieno.colors.inkSoft, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(Space.s4))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(Space.s2))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft, textAlign = TextAlign.Center)
        }
        if (action != null) {
            Spacer(Modifier.height(Space.s5))
            action()
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "Qualcosa non ha funzionato",
        subtitle = message,
        icon = PienoIcons.Info,
        modifier = modifier,
        action = { SecondaryButton(text = "Riprova", onClick = onRetry, leadingIcon = PienoIcons.Refresh) },
    )
}
