package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.core.Geo
import dev.ceccon.pieno.core.LatLon
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Erogazione
import dev.ceccon.pieno.data.model.PrezzoCarburante
import dev.ceccon.pieno.data.model.Rifornimento
import dev.ceccon.pieno.data.model.Stazione
import dev.ceccon.pieno.data.model.isValidPrice
import dev.ceccon.pieno.data.model.stationKey
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.HairlineDivider
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.PrimaryButton
import dev.ceccon.pieno.ui.components.SecondaryButton
import dev.ceccon.pieno.ui.components.StazioniMap
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.launch

private fun heroPrice(s: Stazione): PrezzoCarburante? =
    s.prezzoDi(Carburante.VERDE) ?: s.prezzoDi(Carburante.GASOLIO)

@Composable
fun StationDetailScreen(
    stationId: String,
    onBack: () -> Unit,
    demo: Boolean,
    fuel: Carburante? = null,
) {
    val container = rememberContainer()
    val repo = container.repository
    val appScope = container.appScope
    val context = LocalContext.current
    val s = remember(stationId) { repo.stazioneById(stationId) }
    val distanceKm = remember(s) {
        val loc = Geo.lastKnownLocation(context)
        if (s != null && loc != null && s.lat != 0.0 && s.lon != 0.0) {
            Geo.distanceKm(loc.lat, loc.lon, s.lat, s.lon)
        } else {
            null
        }
    }
    val favorites by repo.stationFavoritesFlow().collectAsState(initial = repo.cachedStationFavorites())
    // Valore iniziale dalla cache (precaricata all'avvio): il bottone "acquisti qui"
    // c'e' subito, senza il ritardo della rete. Poi si aggiorna in background.
    var purchases by remember(s) {
        mutableStateOf(if (s != null) repo.cachedRifornimentiPerStazione(s.insegna, s.comune) else emptyList())
    }
    var showPurchases by remember { mutableStateOf(false) }
    LaunchedEffect(s, demo) {
        if (s != null) {
            purchases = runCatching { repo.rifornimentiPerStazione(demo, s.insegna, s.comune) }.getOrDefault(purchases)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (s == null) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Spacer(Modifier.height(Space.s10))
                EmptyState("Distributore non trovato", icon = PienoIcons.Pin)
            }
        } else {
            val key = stationKey(s)
            val isFav = key in favorites
            Column(Modifier.fillMaxSize()) {
                HeroMap(s, distanceKm)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Space.screen),
                ) {
                Spacer(Modifier.height(Space.s2))
                StatusRow(s)
                Spacer(Modifier.height(Space.s5))
                Text("Tutti i prezzi", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
                Spacer(Modifier.height(Space.s3))
                PrezziCard(s, fuel)

                Spacer(Modifier.height(Space.s6))
                SecondaryButton(
                    text = if (isFav) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                    onClick = { appScope.launch { repo.toggleStationFavorite(key) } },
                    leadingIcon = if (isFav) PienoIcons.StarFilled else PienoIcons.Star,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (purchases.isNotEmpty()) {
                    Spacer(Modifier.height(Space.s3))
                    SecondaryButton(
                        text = "I tuoi acquisti qui (${purchases.size})",
                        onClick = { showPurchases = true },
                        leadingIcon = PienoIcons.Receipt,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (s.lat != 0.0 && s.lon != 0.0) {
                    Spacer(Modifier.height(Space.s3))
                    PrimaryButton(
                        text = "Apri in Google Maps",
                        onClick = { openInMaps(context, s) },
                        leadingIcon = PienoIcons.Navigation,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                    Spacer(Modifier.height(Space.s7))
                }
            }
        }
        // Back flottante sopra la mappa, sotto la status bar: niente header con sfondo.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(Space.s2),
        ) {
            PienoIconButton(
                icon = PienoIcons.ChevronLeft,
                contentDescription = "Indietro",
                onClick = onBack,
                container = Pieno.colors.surface,
                tint = Pieno.colors.ink,
            )
        }
    }

    if (showPurchases) {
        PurchasesSheet(purchases, onDismiss = { showPurchases = false })
    }
}

@Composable
private fun HeroMap(s: Stazione, distanceKm: Double?) {
    // La mappa arriva fin sotto la status bar: aggiungo la sua altezza in cima,
    // cosi' la parte visibile resta piena. clipToBounds per non sbordare nel contenuto.
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(Modifier.fillMaxWidth().height(300.dp + statusBar).clipToBounds()) {
        if (s.lat != 0.0 && s.lon != 0.0) {
            // Marker semplice a spillo (il prezzo e' gia' elencato sotto).
            val markerStations = remember(s) {
                listOf(s to (heroPrice(s) ?: PrezzoCarburante(Carburante.VERDE, Erogazione.SELF, 0.0)))
            }
            StazioniMap(
                stations = markerStations,
                onOpenStation = {},
                modifier = Modifier.fillMaxSize(),
                // Centro spostato a sud: lo spillo (al distributore reale) sale nella
                // parte di mappa visibile, sopra il gradiente, e resta centrato.
                center = LatLon(s.lat - 0.0035, s.lon),
                interactive = false,
                simpleMarker = true,
            )
        } else {
            Box(Modifier.fillMaxSize().background(Pieno.colors.paperDim))
        }
        Box(
            Modifier
                .matchParentSize()
                .background(
                    // Gradiente piu' corto: la carta piena parte piu' in basso, cosi'
                    // si vede piu' mappa e il testo resta un po' piu' in alto.
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.58f to Color.Transparent,
                        0.70f to Pieno.colors.paper,
                        1.0f to Pieno.colors.paper,
                    ),
                ),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = Space.screen)
                .padding(top = Space.s1, bottom = Space.s6),
        ) {
            Text(s.insegna, style = MaterialTheme.typography.headlineMedium, color = Pieno.colors.ink, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text("${s.indirizzo}, ${s.comune}", style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft, maxLines = 1)
            if (distanceKm != null) {
                Spacer(Modifier.height(2.dp))
                Text("a ${Format.distance(distanceKm)} da te", style = MaterialTheme.typography.labelLarge, color = Pieno.colors.accent)
            }
        }
    }
}

@Composable
private fun StatusRow(s: Stazione) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(if (s.online) Green else Pieno.colors.hairline))
        Spacer(Modifier.width(Space.s2))
        val update = Format.relativeUpdate(s.aggiornamentoEpoch)
        Text(
            (if (s.online) "In linea" else "Non in linea") + (update?.let { " · prezzi aggiornati $it" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = Pieno.colors.inkSoft,
        )
    }
}

@Composable
private fun PrezziCard(s: Stazione, fuel: Carburante?) {
    val cheapest = remember(s, fuel) {
        if (fuel != null) s.prezzoDi(fuel) else s.prezzi.filter { isValidPrice(it.prezzo) }.minByOrNull { it.prezzo }
    }
    val ordered = remember(s) {
        s.prezzi.filter { isValidPrice(it.prezzo) }.sortedWith(compareBy({ it.carburante.id }, { it.prezzo }))
    }
    if (ordered.isEmpty()) {
        PienoCard {
            Text("Nessun prezzo disponibile.", style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.inkSoft)
        }
        return
    }
    PienoCard {
        ordered.forEachIndexed { i, p ->
            PrezzoRow(p, highlight = p == cheapest)
            if (i < ordered.size - 1) HairlineDivider()
        }
    }
}

@Composable
private fun PrezzoRow(p: PrezzoCarburante, highlight: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(p.carburante.label, style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
            Text(p.erogazione.label, style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
        }
        Text(
            "${Format.pricePerLiter(p.prezzo)} €/L",
            style = MaterialTheme.typography.titleLarge,
            color = if (highlight) Pieno.colors.accent else Pieno.colors.ink,
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchasesSheet(purchases: List<Rifornimento>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Pieno.colors.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = Space.s5).padding(bottom = Space.s8)) {
            Text("I tuoi acquisti qui", style = MaterialTheme.typography.headlineMedium, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s4))
            purchases.forEachIndexed { i, r ->
                Row(Modifier.fillMaxWidth().padding(vertical = Space.s3), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("${Format.dateMedium(r.dataEpochDay)} · ${r.carburante.label}", style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
                        Text(Format.liters(r.litri), style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Format.euro(r.importo), style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
                        Text("sconto ${Format.euro(r.sconto)}", style = MaterialTheme.typography.labelMedium, color = Pieno.colors.accent)
                    }
                }
                if (i < purchases.size - 1) HairlineDivider()
            }
        }
    }
}

private fun openInMaps(context: android.content.Context, s: Stazione) {
    val label = Uri.encode("${s.insegna} ${s.comune}")
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${s.lat},${s.lon}?q=${s.lat},${s.lon}($label)"))
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${s.lat},${s.lon}"))
    runCatching { context.startActivity(geo) }.recoverCatching { context.startActivity(web) }
}
