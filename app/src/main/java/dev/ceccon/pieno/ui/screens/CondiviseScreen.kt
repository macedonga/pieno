package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.data.local.CondivisaEntry
import dev.ceccon.pieno.data.repo.ImportResult
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.GhostButton
import dev.ceccon.pieno.ui.components.HairlineDivider
import dev.ceccon.pieno.ui.components.LeadingIcon
import dev.ceccon.pieno.ui.components.ListRow
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.PienoTextField
import dev.ceccon.pieno.ui.components.PienoTopBar
import dev.ceccon.pieno.ui.components.PrimaryButton
import dev.ceccon.pieno.ui.components.SecondaryButton
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.launch

@Composable
fun CondiviseScreen(onBack: () -> Unit, onScan: () -> Unit) {
    val repo = rememberContainer().repository
    val scope = rememberCoroutineScope()
    val condivise by repo.condiviseFlow().collectAsState(initial = emptyList())
    var showImport by remember { mutableStateOf(false) }

    Column(Modifier) {
        PienoTopBar(
            title = "Tessere condivise",
            onBack = onBack,
            action = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PienoIconButton(PienoIcons.Scan, "Scansiona QR", onClick = onScan, tint = Pieno.colors.ink)
                    PienoIconButton(PienoIcons.Plus, "Incolla QR", onClick = { showImport = true }, tint = Pieno.colors.ink)
                }
            },
        )

        if (condivise.isEmpty()) {
            EmptyState(
                title = "Nessuna tessera condivisa",
                subtitle = "Importa il QR di un'auto non intestata a te, per esempio quella di un familiare.",
                icon = PienoIcons.Card,
                action = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PrimaryButton(text = "Scansiona QR", onClick = onScan, leadingIcon = PienoIcons.Scan)
                        Spacer(Modifier.height(Space.s2))
                        GhostButton(text = "Incolla la stringa", onClick = { showImport = true }, contentColor = Pieno.colors.inkSoft)
                    }
                },
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(start = Space.screen, end = Space.screen, bottom = Space.s9)) {
                item {
                    PienoCard(contentPadding = PaddingValues(vertical = Space.s1)) {
                        condivise.forEachIndexed { index, c ->
                            CondivisaRow(c, onRemove = { scope.launch { repo.removeCondivisa(c.targa) } })
                            if (index < condivise.size - 1) HairlineDivider(Modifier.padding(start = Space.s8))
                        }
                    }
                }
            }
        }
    }

    if (showImport) {
        ImportDialog(
            onDismiss = { showImport = false },
            onScan = { showImport = false; onScan() },
            onImport = { payload -> repo.importCondivisa(payload) },
        )
    }
}

@Composable
private fun CondivisaRow(c: CondivisaEntry, onRemove: () -> Unit) {
    // La scadenza e' un sentinella far-future (questi QR non scadono): inutile
    // mostrarla. Si tiene solo l'esito della verifica firma.
    val firma = if (c.verificata) "Firma verificata" else "Firma non verificata"
    ListRow(
        headline = c.targa,
        supporting = firma,
        leading = { LeadingIcon(PienoIcons.Card) },
        trailing = {
            PienoIconButton(
                icon = PienoIcons.Close,
                contentDescription = "Rimuovi ${c.targa}",
                onClick = onRemove,
                tint = Pieno.colors.inkSoft,
                size = 40,
            )
        },
    )
}

@Composable
private fun ImportDialog(
    onDismiss: () -> Unit,
    onScan: () -> Unit,
    onImport: suspend (String) -> ImportResult,
) {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        PienoCard(modifier = Modifier.fillMaxWidth()) {
            Text("Importa tessera condivisa", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(
                "Incolla la stringa del QR (HC1:...) di un'auto non intestata a te. La firma viene verificata localmente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
            Spacer(Modifier.height(Space.s4))
            PienoTextField(
                value = text,
                onValueChange = { text = it; error = null },
                placeholder = "HC1:...",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Spacer(Modifier.height(Space.s2))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PienoIcons.Info, contentDescription = null, tint = Pieno.colors.accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.s2))
                    Text(error!!, style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.accent)
                }
            }
            Spacer(Modifier.height(Space.s4))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                GhostButton(text = "Scansiona", onClick = onScan)
                Spacer(Modifier.width(Space.s2))
                PrimaryButton(
                    text = "Verifica e importa",
                    loading = importing,
                    onClick = {
                        if (text.isBlank()) {
                            error = "Incolla prima la stringa del QR."
                        } else {
                            scope.launch {
                                importing = true
                                val result = onImport(text.trim())
                                importing = false
                                when (result) {
                                    is ImportResult.Success -> onDismiss()
                                    is ImportResult.InvalidSignature ->
                                        error = "Firma non valida: il QR potrebbe essere falso o alterato."
                                    is ImportResult.Error -> error = result.message
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}
