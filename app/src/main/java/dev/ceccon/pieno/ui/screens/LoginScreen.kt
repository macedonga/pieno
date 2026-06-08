package dev.ceccon.pieno.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.components.PrimaryButton
import dev.ceccon.pieno.ui.components.SecondaryButton
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Paper
import dev.ceccon.pieno.ui.theme.Pieno
import dev.ceccon.pieno.ui.theme.Space

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onDemo: () -> Unit,
    loading: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Pieno.colors.paper)
            .systemBarsPadding()
            .padding(horizontal = Space.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PienoIcons.Droplet, contentDescription = null, tint = Paper, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(Space.s5))
        Text("Pieno", style = MaterialTheme.typography.displayLarge, color = Pieno.colors.ink)
        Spacer(Modifier.height(Space.s2))
        Text(
            "La tua tessera carburante del Friuli Venezia Giulia, pronta alla pompa anche offline.",
            style = MaterialTheme.typography.bodyLarge,
            color = Pieno.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Space.s4),
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = "Accedi con LoginFVG",
            onClick = onLogin,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.s3))
        SecondaryButton(
            text = "Esplora con dati di esempio",
            onClick = onDemo,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Space.s5))
        Text(
            "App indipendente, non affiliata a Insiel né alla Regione FVG. " +
                "Accedi con SPID o CIE tramite il sistema regionale LoginFVG.",
            style = MaterialTheme.typography.labelMedium,
            color = Pieno.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Space.s5))
    }
}
