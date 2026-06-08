package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.data.model.Comunicazione
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoTopBar
import dev.ceccon.pieno.ui.components.SegmentedControl
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Space

@Composable
fun ComunicazioniScreen(demo: Boolean, onBack: () -> Unit) {
    val repo = rememberContainer().repository
    Column(Modifier) {
        PienoTopBar("Comunicazioni", onBack)
        Loader(key = demo, load = { repo.comunicazioni(demo) }) { all ->
            ComunicazioniContent(all)
        }
    }
}

@Composable
private fun ComunicazioniContent(all: List<Comunicazione>) {
    val hasPast = remember(all) { all.any { !it.corrente } }
    var tab by remember { mutableIntStateOf(0) }
    val list = remember(tab, all) { if (tab == 0) all.filter { it.corrente } else all }

    Column {
        if (hasPast) {
            Column(Modifier.padding(horizontal = Space.screen)) {
                SegmentedControl(listOf("Correnti", "Tutte"), tab, { tab = it })
                Spacer(Modifier.height(Space.s4))
            }
        }
        if (list.isEmpty()) {
            EmptyState(
                title = "Nessuna comunicazione",
                subtitle = "Qui compariranno avvisi e novità.",
                icon = PienoIcons.Bell,
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(start = Space.screen, end = Space.screen, bottom = Space.s9)) {
                items(list) { c ->
                    ComunicazioneCard(c)
                    Spacer(Modifier.height(Space.s3))
                }
            }
        }
    }
}

@Composable
private fun ComunicazioneCard(c: Comunicazione) {
    PienoCard {
        Text(Format.dateLong(c.dataEpochDay), style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
        Spacer(Modifier.height(Space.s2))
        Text(c.titolo, style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
        Spacer(Modifier.height(Space.s2))
        Text(c.testo, style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.ink)
    }
}
