package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.theme.Radii

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = options.size.coerceAtLeast(1)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(Radii.md))
            .background(Pieno.colors.paperDim)
            .padding(4.dp),
    ) {
        val segWidth = maxWidth / count
        val thumbOffset by animateDpAsState(
            targetValue = segWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
            label = "thumb",
        )
        Box(
            Modifier
                .offset(x = thumbOffset)
                .width(segWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Radii.sm))
                .background(Pieno.colors.surface),
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val textColor by animateColorAsState(if (selected) Pieno.colors.ink else Pieno.colors.inkSoft, label = "segText")
                val interaction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Radii.sm))
                        .clickable(interaction, indication = null) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(option, style = MaterialTheme.typography.labelLarge, color = textColor)
                }
            }
        }
    }
}

@Composable
fun PienoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = if (placeholder != null) {
            { Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.inkSoft) }
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(Radii.md),
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null, tint = Pieno.colors.inkSoft) }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Pieno.colors.accent,
            unfocusedBorderColor = Pieno.colors.hairline,
            focusedContainerColor = Pieno.colors.surface,
            unfocusedContainerColor = Pieno.colors.surface,
            cursorColor = Pieno.colors.accent,
            focusedTextColor = Pieno.colors.ink,
            unfocusedTextColor = Pieno.colors.ink,
        ),
    )
}
