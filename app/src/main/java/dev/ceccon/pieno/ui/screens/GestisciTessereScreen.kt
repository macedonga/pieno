package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.data.model.Tessera
import dev.ceccon.pieno.ui.components.LeadingIcon
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.PienoTopBar
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.launch

private fun swap(list: List<String>, a: Int, b: Int): List<String> {
    if (a < 0 || b < 0 || a >= list.size || b >= list.size) return list
    val m = list.toMutableList()
    val t = m[a]; m[a] = m[b]; m[b] = t
    return m
}

fun cardColorOf(tessera: Tessera): Color =
    tessera.colore?.let { runCatching { Color(it.toLong(16).toInt()) }.getOrNull() } ?: Green

@Composable
fun GestisciTessereScreen(demo: Boolean, onBack: () -> Unit) {
    val repo = rememberContainer().repository
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        PienoTopBar("Riordina tessere", onBack)
        Loader(key = demo, load = { repo.tessere(demo) }, initial = repo.cachedTessere()) { loaded ->
            ReorderList(loaded) { newOrder -> scope.launch { repo.setTessereOrder(newOrder) } }
        }
    }
}

@Composable
private fun ReorderList(loaded: List<Tessera>, onReorder: (List<String>) -> Unit) {
    // Ordine locale: il riordino e' immediato e animato (animateItem), poi si
    // persiste in background senza ricaricare (niente skeleton, niente scatto).
    var order by remember(loaded) { mutableStateOf(loaded.map { it.targa }) }
    val byTarga = remember(loaded) { loaded.associateBy { it.targa } }
    val items = order.mapNotNull { byTarga[it] }

    LazyColumn(
        contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.s2, bottom = Space.s9),
        verticalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        itemsIndexed(items, key = { _, t -> t.targa }) { index, t ->
            ManageRow(
                tessera = t,
                canUp = index > 0,
                canDown = index < items.size - 1,
                onUp = { val n = swap(order, index, index - 1); order = n; onReorder(n) },
                onDown = { val n = swap(order, index, index + 1); order = n; onReorder(n) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun ManageRow(
    tessera: Tessera,
    canUp: Boolean,
    canDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titolo = tessera.etichetta?.takeIf { it.isNotBlank() }
        ?: if (tessera.condivisa) "Tessera condivisa" else Format.properName(tessera.intestatario)
    PienoCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeadingIcon(
                icon = if (tessera.condivisa) PienoIcons.Card else PienoIcons.Droplet,
                container = cardColorOf(tessera),
                tint = Color.White,
            )
            Spacer(Modifier.width(Space.s4))
            Column(Modifier.weight(1f)) {
                Text(titolo, style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink, maxLines = 1)
                Text(tessera.targa, style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft)
            }
            PienoIconButton(
                icon = PienoIcons.ChevronUp,
                contentDescription = "Sposta su",
                onClick = onUp,
                tint = if (canUp) Pieno.colors.ink else Pieno.colors.hairline,
                size = 42,
            )
            PienoIconButton(
                icon = PienoIcons.ChevronDown,
                contentDescription = "Sposta giù",
                onClick = onDown,
                tint = if (canDown) Pieno.colors.ink else Pieno.colors.hairline,
                size = 42,
            )
        }
    }
}
