package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.Space

@Composable
fun PienoTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = Space.s2, vertical = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PienoIconButton(
            icon = PienoIcons.ChevronLeft,
            contentDescription = "Indietro",
            onClick = onBack,
            tint = Pieno.colors.ink,
        )
        Spacer(Modifier.width(Space.s1))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = Pieno.colors.ink,
            modifier = Modifier.weight(1f),
        )
        if (action != null) action()
    }
}
