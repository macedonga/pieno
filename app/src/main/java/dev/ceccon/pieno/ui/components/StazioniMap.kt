package dev.ceccon.pieno.ui.components

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.core.LatLon
import dev.ceccon.pieno.core.boundingBoxAround
import dev.ceccon.pieno.core.pinMarkerBitmap
import dev.ceccon.pieno.core.priceMarkerBitmap
import dev.ceccon.pieno.data.model.PrezzoCarburante
import dev.ceccon.pieno.data.model.Stazione
import dev.ceccon.pieno.ui.theme.Pieno
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Riquadro che contiene tutto il Friuli Venezia Giulia (nord, est, sud, ovest).
private val FVG = BoundingBox(46.70, 13.95, 45.55, 12.30)
private val FVG_SCROLL = BoundingBox(47.05, 14.35, 45.25, 11.95)

// Ultima inquadratura della mappa interattiva: si conserva quando si apre il
// dettaglio e si torna indietro, invece di resettare.
private object MapCamera {
    var center: GeoPoint? = null
    var zoom: Double? = null
}

// Prefetch tile FVG una sola volta per sessione (cosi' la mappa e' subito pronta).
private object TilePrefetch {
    var started = false
}

// Tile stilizzate e leggere (CartoDB Positron / Dark Matter): molto piu' pulite e
// veloci della Mapnik standard, gia' tematizzate quindi senza filtro colore.
private fun cartoTileSource(name: String, style: String): OnlineTileSourceBase =
    object : OnlineTileSourceBase(
        name, 0, 20, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/$style/",
            "https://b.basemaps.cartocdn.com/$style/",
            "https://c.basemaps.cartocdn.com/$style/",
            "https://d.basemaps.cartocdn.com/$style/",
        ),
        "© OpenStreetMap, © CARTO",
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String =
            baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }

private val positron = cartoTileSource("CartoPositron", "light_all")
private val darkMatter = cartoTileSource("CartoDarkMatter", "dark_all")

@Composable
fun StazioniMap(
    stations: List<Pair<Stazione, PrezzoCarburante>>,
    onOpenStation: (String) -> Unit,
    modifier: Modifier = Modifier,
    center: LatLon? = null,
    startRadiusKm: Double = 20.0,
    interactive: Boolean = true,
    simpleMarker: Boolean = false,
) {
    val context = LocalContext.current
    val colors = Pieno.colors
    val dark = colors.paper.luminance() < 0.5f
    val paperArgb = colors.paper.toArgb()
    val markerFill = MaterialTheme.colorScheme.primary.toArgb()
    val markerText = MaterialTheme.colorScheme.onPrimary.toArgb()
    val markerOutline = paperArgb

    val mapView = remember { createMapView(context) }
    val markerCache = remember(markerFill, markerText) { mutableMapOf<String, BitmapDrawable>() }
    val pinDrawable = remember(markerFill) {
        BitmapDrawable(context.resources, pinMarkerBitmap(context, markerFill, markerOutline))
    }
    val framed = remember { booleanArrayOf(false) }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            // Salva l'inquadratura della mappa interattiva, per ripristinarla al ritorno.
            if (interactive) {
                MapCamera.center = GeoPoint(mapView.mapCenter.latitude, mapView.mapCenter.longitude)
                MapCamera.zoom = mapView.zoomLevelDouble
            }
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.setTileSource(if (dark) darkMatter else positron)
            mv.setBackgroundColor(paperArgb)
            mv.overlayManager.tilesOverlay.loadingBackgroundColor = paperArgb
            if (!interactive) {
                mv.setMultiTouchControls(false)
                mv.setOnTouchListener { _, _ -> true }
            }
            // Prefetch FVG una volta, in background e SENZA UI (NoUI): nessun dialog,
            // l'utente non si accorge di nulla, la mappa e' subito usabile.
            if (interactive && !TilePrefetch.started) {
                TilePrefetch.started = true
                runCatching {
                    CacheManager(mv).downloadAreaAsyncNoUI(
                        context, FVG, 8, 12,
                        object : CacheManager.CacheManagerCallback {
                            override fun onTaskComplete() {}
                            override fun onTaskFailed(errors: Int) {}
                            override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {}
                            override fun downloadStarted() {}
                            override fun setPossibleTilesInArea(total: Int) {}
                        },
                    )
                }
            }
            if (!framed[0]) {
                if (!interactive) {
                    // Dettaglio: statica, zoom intero, centro gia' offsettato dal chiamante.
                    if (center != null) {
                        framed[0] = true
                        mv.post {
                            mv.controller.setZoom(15.0)
                            mv.controller.setCenter(GeoPoint(center.lat, center.lon))
                        }
                    }
                } else if (MapCamera.center != null) {
                    // Ritorno dal dettaglio: ripristina l'ultima inquadratura.
                    framed[0] = true
                    val c = MapCamera.center!!
                    val z = MapCamera.zoom ?: 11.5
                    mv.post {
                        mv.controller.setZoom(z)
                        mv.controller.setCenter(c)
                    }
                } else if (center != null) {
                    // Prima apertura con posizione nota: 20 km attorno all'utente.
                    framed[0] = true
                    val b = boundingBoxAround(center.lat, center.lon, startRadiusKm)
                    mv.post { mv.zoomToBoundingBox(BoundingBox(b[0], b[1], b[2], b[3]), false, 16) }
                } else {
                    // Posizione non ancora nota: intanto il FVG (si riprova quando arriva).
                    mv.post { mv.zoomToBoundingBox(FVG, false, 24) }
                }
            }
            mv.overlays.clear()
            stations.forEach { (s, prezzo) ->
                if (s.lat != 0.0 && s.lon != 0.0) {
                    val drawable = if (simpleMarker) {
                        pinDrawable
                    } else {
                        val label = "${Format.pricePerLiter(prezzo.prezzo)} €"
                        markerCache.getOrPut(label) {
                            BitmapDrawable(mv.resources, priceMarkerBitmap(context, label, markerFill, markerText, markerOutline))
                        }
                    }
                    val marker = Marker(mv).apply {
                        position = GeoPoint(s.lat, s.lon)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = drawable
                        title = s.insegna
                        setInfoWindow(null)
                        setOnMarkerClickListener { _, _ ->
                            onOpenStation(s.id)
                            true
                        }
                    }
                    mv.overlays.add(marker)
                }
            }
            mv.invalidate()
        },
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(positron)
        setMultiTouchControls(true)
        isHorizontalMapRepetitionEnabled = false
        isVerticalMapRepetitionEnabled = false
        minZoomLevel = 7.8
        maxZoomLevel = 19.0
        setScrollableAreaLimitDouble(FVG_SCROLL)
        // Inquadratura iniziale gestita nel blocco update (20 km / ripristino / FVG).
        controller.setZoom(8.2)
        controller.setCenter(GeoPoint(46.12, 13.12))
    }
}
