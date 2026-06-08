package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Tessera
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.CardColors
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space

fun fuelDisplay(c: Carburante): String = if (c == Carburante.VERDE) "Benzina" else c.label

private fun hexOf(c: Color): String = "%08X".format(c.toArgb())

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeTesseraDialog(
    tessera: Tessera,
    onSave: (name: String, colorHex: String?, fuel: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(tessera.etichetta.orEmpty()) }
    var color by remember { mutableStateOf(tessera.colore) }
    var fuel by remember { mutableStateOf(tessera.carburante) }
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text("Personalizza tessera", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(
                "Nome, colore e carburante per ${tessera.targa}.",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
            Spacer(Modifier.height(Space.s4))
            PienoTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Nome (es. Panda)",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Space.s4))
            Text("Colore", style = MaterialTheme.typography.labelLarge, color = Pieno.colors.inkSoft)
            Spacer(Modifier.height(Space.s3))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.s3),
                verticalArrangement = Arrangement.spacedBy(Space.s3),
            ) {
                CardColors.forEach { c ->
                    val hex = hexOf(c)
                    val selected = if (color == null) c == CardColors.first() else color.equals(hex, ignoreCase = true)
                    ColorSwatch(c, selected) { color = hex }
                }
            }

            Spacer(Modifier.height(Space.s4))
            Text("Carburante", style = MaterialTheme.typography.labelLarge, color = Pieno.colors.inkSoft)
            Spacer(Modifier.height(Space.s3))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
                verticalArrangement = Arrangement.spacedBy(Space.s2),
            ) {
                Carburante.entries.forEach { c ->
                    FuelChip(fuelDisplay(c), selected = fuel == c) { fuel = if (fuel == c) null else c }
                }
            }

            Spacer(Modifier.height(Space.s5))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(text = "Salva", onClick = { onSave(text, color, fuel?.name) })
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (selected) Modifier.border(2.dp, Pieno.colors.ink, CircleShape) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(PienoIcons.Check, contentDescription = "Selezionato", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun FuelChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radii.sm))
            .background(if (selected) Pieno.colors.greenSoft else Pieno.colors.paperDim)
            .then(if (selected) Modifier.border(1.5.dp, Pieno.colors.onGreenSoft, RoundedCornerShape(Radii.sm)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = Space.s3, vertical = Space.s2),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Pieno.colors.onGreenSoft else Pieno.colors.inkSoft,
        )
    }
}
