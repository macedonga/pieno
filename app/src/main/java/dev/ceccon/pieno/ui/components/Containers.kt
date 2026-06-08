package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space
// Card piatta: superficie chiara, bordo a filo, nessuna ombra vistosa (vedi
// design-principles.md, gerarchia per colore e spazio, non per ombre).
@Composable
fun PienoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radii.xl),
    container: Color = Pieno.colors.surface,
    bordered: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(Space.s4),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(container)
            .then(if (bordered) Modifier.border(1.dp, Pieno.colors.hairline, shape) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink, modifier = Modifier.weight(1f))
        if (action != null) action()
    }
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = Pieno.colors.paperDim,
    contentColor: Color = Pieno.colors.ink,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.sm))
            .background(container)
            .padding(horizontal = Space.s3, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = PienoIcons.Info,
    container: Color = Pieno.colors.greenSoft,
    contentColor: Color = Pieno.colors.onGreenSoft,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.md))
            .background(container)
            .padding(Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Space.s2))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = contentColor)
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier, color: Color = Pieno.colors.hairline) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}
