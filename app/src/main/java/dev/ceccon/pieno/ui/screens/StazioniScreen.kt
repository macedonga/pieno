package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.core.Geo
import dev.ceccon.pieno.core.LatLon
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.PrezzoCarburante
import dev.ceccon.pieno.data.model.Stazione
import dev.ceccon.pieno.data.model.isValidPrice
import dev.ceccon.pieno.data.model.stationKey
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.HairlineDivider
import dev.ceccon.pieno.ui.components.InfoBanner
import dev.ceccon.pieno.ui.components.fuelDisplay
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.ScreenTitle
import dev.ceccon.pieno.ui.components.SecondaryButton
import dev.ceccon.pieno.ui.components.SkeletonBox
import dev.ceccon.pieno.ui.components.StazioniMap
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space

private const val PAGE = 16

// Prezzo per il marker / ordinamento senza posizione: il piu' basso tra benzina e gasolio.
private fun markerPrice(s: Stazione): PrezzoCarburante? =
    s.prezzoDi(Carburante.VERDE) ?: s.prezzoDi(Carburante.GASOLIO)

// Un prezzo per carburante (il piu' basso), per mostrarli puliti nella card.
private fun pricesByFuel(s: Stazione): List<PrezzoCarburante> =
    s.prezzi.filter { isValidPrice(it.prezzo) }
        .groupBy { it.carburante }
        .map { (_, l) -> l.minBy { it.prezzo } }
        .sortedBy { it.carburante.id }

@Composable
fun StazioniScreen(demo: Boolean, onOpenStation: (String, Carburante?) -> Unit) {
    val repo = rememberContainer().repository
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Loader(
            key = demo,
            load = { repo.stazioni(demo) },
            initial = repo.cachedStazioni(),
            loading = { StazioniSkeleton() },
        ) { stazioni ->
            StazioniContent(demo, stazioni, onOpenStation)
        }
    }
}

@Composable
private fun StazioniSkeleton() {
    Column(Modifier.fillMaxSize().padding(horizontal = Space.screen)) {
        Spacer(Modifier.height(Space.s3))
        SkeletonBox(Modifier.fillMaxWidth(0.45f).height(34.dp))
        Spacer(Modifier.height(Space.s2))
        SkeletonBox(Modifier.fillMaxWidth(0.6f).height(18.dp))
        Spacer(Modifier.height(Space.s5))
        repeat(4) {
            SkeletonBox(Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(Radii.card))
            Spacer(Modifier.height(Space.s3))
        }
    }
}

@Composable
private fun StazioniContent(
    demo: Boolean,
    stazioni: List<Stazione>,
    onOpenStation: (String, Carburante?) -> Unit,
) {
    val context = LocalContext.current
    var mapMode by rememberSaveable { mutableStateOf(false) }
    // rememberSaveable: il filtro carburante si conserva quando apro un distributore e torno.
    var mapFuel by rememberSaveable { mutableStateOf(Carburante.VERDE) }

    var loc by remember { mutableStateOf<LatLon?>(null) }
    var locating by remember { mutableStateOf(Geo.hasPermission(context)) }
    var asked by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locating = granted
    }
    LaunchedEffect(Unit) {
        if (!Geo.hasPermission(context) && !asked) {
            asked = true
            permLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    LaunchedEffect(locating) {
        if (locating) {
            loc = Geo.currentLocation(context)
            locating = false
        }
    }

    val repo = rememberContainer().repository
    val favorites by repo.stationFavoritesFlow().collectAsState(initial = repo.cachedStationFavorites())
    val here = loc
    val sorted = remember(stazioni, here, favorites) {
        stazioni
            .map { s ->
                val d = if (here != null && s.lat != 0.0 && s.lon != 0.0) {
                    Geo.distanceKm(here.lat, here.lon, s.lat, s.lon)
                } else {
                    null
                }
                s to d
            }
            // Preferiti in cima; poi per distanza (dai piu' vicini). Senza posizione,
            // ripiego sul prezzo piu' basso cosi' l'ordine resta sensato.
            .sortedWith(
                compareBy(
                    { stationKey(it.first) !in favorites },
                    { p -> if (here != null) (p.second ?: Double.MAX_VALUE) else (markerPrice(p.first)?.prezzo ?: Double.MAX_VALUE) },
                ),
            )
    }
    val mapFuels = remember(stazioni) {
        Carburante.entries.filter { f -> stazioni.any { it.prezzoDi(f) != null } }
    }
    val mapList = remember(stazioni, mapFuel) {
        stazioni.mapNotNull { s -> s.prezzoDi(mapFuel)?.let { s to it } }
    }

    Box(Modifier.fillMaxSize().clipToBounds()) {
        if (mapMode) {
            StazioniMap(mapList, { id -> onOpenStation(id, null) }, Modifier.fillMaxSize(), center = here, startRadiusKm = 10.0)
        }

        Column(Modifier.fillMaxSize()) {
            val headerBg = if (mapMode) {
                Modifier.background(
                    Brush.verticalGradient(
                        0.0f to Pieno.colors.paper,
                        0.58f to Pieno.colors.paper,
                        1.0f to Pieno.colors.paper.copy(alpha = 0f),
                    ),
                )
            } else {
                Modifier
            }
            Column(
                modifier = headerBg
                    .fillMaxWidth()
                    .padding(horizontal = Space.screen)
                    .padding(bottom = if (mapMode) Space.s10 else Space.s2),
            ) {
                Spacer(Modifier.height(Space.s3))
                ScreenTitle(
                    title = "Stazioni",
                    action = {
                        PienoIconButton(
                            icon = if (mapMode) PienoIcons.ListView else PienoIcons.Pin,
                            contentDescription = if (mapMode) "Vedi lista" else "Vedi mappa",
                            onClick = { mapMode = !mapMode },
                            container = Pieno.colors.surface,
                            tint = Pieno.colors.ink,
                        )
                    },
                )
                if (demo) {
                    Spacer(Modifier.height(Space.s4))
                    InfoBanner("Prezzi di esempio. Accedi per i dati aggiornati.")
                }
                if (mapMode && mapFuels.isNotEmpty()) {
                    Spacer(Modifier.height(Space.s4))
                    FuelFilterRow(mapFuels, mapFuel) { mapFuel = it }
                }
            }

            if (!mapMode) {
                when {
                    locating && loc == null -> LocatingState()
                    else -> StationList(
                        sorted = sorted,
                        favorites = favorites,
                        hasLocation = here != null,
                        onRequestLocation = { permLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
                        onOpenStation = { id -> onOpenStation(id, null) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LocatingState() {
    Column(
        Modifier.fillMaxWidth().padding(Space.s9),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Pieno.colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(Space.s4))
        Text("Trovo la tua posizione…", style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.inkSoft)
    }
}

@Composable
private fun StationList(
    sorted: List<Pair<Stazione, Double?>>,
    favorites: Set<String>,
    hasLocation: Boolean,
    onRequestLocation: () -> Unit,
    onOpenStation: (String) -> Unit,
) {
    if (sorted.isEmpty()) {
        EmptyState(title = "Nessun distributore", subtitle = "Nessun prezzo disponibile.", icon = PienoIcons.Pump)
        return
    }
    val listState = rememberLazyListState()
    var visible by remember(hasLocation) { mutableIntStateOf(PAGE) }
    val loadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= visible - 3
        }
    }
    LaunchedEffect(loadMore, sorted.size) {
        if (loadMore && visible < sorted.size) visible = (visible + PAGE).coerceAtMost(sorted.size)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.s2, bottom = Space.s9),
        verticalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        if (!hasLocation) {
            item { LocationPrompt(onRequestLocation) }
        }
        val visibleList = sorted.take(visible)
        val favs = visibleList.filter { stationKey(it.first) in favorites }
        val others = visibleList.filter { stationKey(it.first) !in favorites }
        if (favs.isNotEmpty()) {
            item("h_fav") { SectionLabel("Preferiti") }
            items(favs, key = { it.first.id }) { (s, dist) ->
                StationCard(s, dist, isFav = true, onClick = { onOpenStation(s.id) })
            }
            if (others.isNotEmpty()) {
                item("h_other") { SectionLabel("Altri distributori") }
            }
        }
        items(others, key = { it.first.id }) { (s, dist) ->
            StationCard(s, dist, isFav = false, onClick = { onOpenStation(s.id) })
        }
        if (visible < sorted.size) {
            item {
                Box(Modifier.fillMaxWidth().padding(Space.s3), contentAlignment = Alignment.Center) {
                    Text("Scorri per i distributori più lontani", style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Pieno.colors.inkSoft,
        modifier = Modifier.padding(top = Space.s1, bottom = Space.s1),
    )
}

@Composable
private fun FuelFilterRow(fuels: List<Carburante>, selected: Carburante, onSelect: (Carburante) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        fuels.forEach { c ->
            val on = c == selected
            Text(
                fuelDisplay(c),
                style = MaterialTheme.typography.labelLarge,
                color = if (on) MaterialTheme.colorScheme.onPrimary else Pieno.colors.ink,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radii.pill))
                    .background(if (on) MaterialTheme.colorScheme.primary else Pieno.colors.surface)
                    .clickable { onSelect(c) }
                    .padding(horizontal = Space.s4, vertical = Space.s2),
            )
        }
    }
}

@Composable
private fun LocationPrompt(onRequestLocation: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        InfoBanner("Posizione non attiva: i distributori non sono ordinati per vicinanza.")
        Spacer(Modifier.height(Space.s3))
        SecondaryButton(
            text = "Consenti posizione",
            onClick = onRequestLocation,
            leadingIcon = PienoIcons.Navigation,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StationCard(s: Stazione, distanceKm: Double?, isFav: Boolean, onClick: () -> Unit) {
    val prices = remember(s) { pricesByFuel(s) }
    val cheapest = remember(prices) { prices.minByOrNull { it.prezzo } }
    PienoCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFav) {
                        Icon(PienoIcons.StarFilled, contentDescription = "Preferito", tint = Pieno.colors.accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(Space.s1))
                    }
                    Text(s.insegna, style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink, maxLines = 1)
                }
                Spacer(Modifier.height(2.dp))
                Text("${s.indirizzo}, ${s.comune}", style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft, maxLines = 1)
            }
            if (distanceKm != null) {
                Spacer(Modifier.width(Space.s3))
                Column(horizontalAlignment = Alignment.End) {
                    Text(Format.distance(distanceKm), style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
                    Text("da te", style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
                }
            }
        }
        Spacer(Modifier.height(Space.s3))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (s.online) Green else Pieno.colors.hairline))
            Spacer(Modifier.width(Space.s2))
            val update = Format.relativeUpdate(s.aggiornamentoEpoch)
            Text(
                (if (s.online) "In linea" else "Non in linea") + (update?.let { " · aggiornato $it" } ?: ""),
                style = MaterialTheme.typography.labelMedium,
                color = Pieno.colors.inkSoft,
            )
        }
        if (prices.isNotEmpty()) {
            Spacer(Modifier.height(Space.s3))
            HairlineDivider()
            Spacer(Modifier.height(Space.s3))
            // Mini tabella a colonne uguali: si distribuisce su tutta la larghezza e
            // sta bene con 2, 3 o 4 prezzi. Divisori verticali sottili tra le colonne.
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                prices.forEachIndexed { i, p ->
                    if (i > 0) {
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Pieno.colors.hairline),
                        )
                    }
                    val hi = p == cheapest
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = Space.s2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(p.carburante.label, style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft, maxLines = 1)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${Format.pricePerLiter(p.prezzo)} €",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (hi) Pieno.colors.accent else Pieno.colors.ink,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
