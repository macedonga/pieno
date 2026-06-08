package dev.ceccon.pieno.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.google.android.gms.pay.Pay
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.core.Geo
import dev.ceccon.pieno.core.shareQrImage
import dev.ceccon.pieno.data.local.TessereSettings
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Tessera
import dev.ceccon.pieno.data.wallet.WalletManager
import dev.ceccon.pieno.ui.components.CustomizeTesseraDialog
import dev.ceccon.pieno.ui.components.InfoBanner
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.Pill
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.PrimaryButton
import dev.ceccon.pieno.ui.components.QrImage
import dev.ceccon.pieno.ui.components.SkeletonBox
import dev.ceccon.pieno.ui.components.fuelDisplay
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Pieno
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// Costanti del pannello QR: sempre bianco con moduli scuri, in ogni tema.
private val QrPanel = Color.White
private val QrInk = Color(0xFF15161A)

// QR a schermo intero con luminosita' al massimo (per scansionarlo al distributore);
// alla chiusura la luminosita' torna com'era.
@Composable
private fun FullscreenQrDialog(tessera: Tessera, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        DisposableEffect(Unit) {
            val window = (view.parent as? DialogWindowProvider)?.window
            val attrs = window?.attributes
            val original = attrs?.screenBrightness
            if (window != null && attrs != null) {
                attrs.screenBrightness = 1f
                window.attributes = attrs
            }
            onDispose {
                if (window != null && attrs != null) {
                    attrs.screenBrightness = original ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    window.attributes = attrs
                }
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(QrPanel)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss() }
                .padding(Space.s7),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(tessera.targa, style = MaterialTheme.typography.headlineMedium, color = QrInk)
                Spacer(Modifier.height(Space.s6))
                QrImage(
                    content = tessera.qrPayload,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    foreground = QrInk,
                    background = QrPanel,
                )
                Spacer(Modifier.height(Space.s6))
                Text("Tocca per chiudere", style = MaterialTheme.typography.bodyMedium, color = QrInk.copy(alpha = 0.55f))
            }
        }
    }
}
private val OnCard = Color.White
private val CardAction = Color.White.copy(alpha = 0.15f)

@Composable
fun TesseraScreen(demo: Boolean, onGestisci: () -> Unit, onOpenStation: (String, Carburante?) -> Unit) {
    val repo = rememberContainer().repository
    val settings by repo.tessereSettingsFlow().collectAsState(initial = TessereSettings())
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Loader(
            key = Pair(demo, settings),
            load = { repo.tessere(demo) },
            initial = repo.cachedTessere(),
            loading = { TesseraLoading() },
        ) { loaded ->
            TesseraContent(demo, loaded, onGestisci = onGestisci, onOpenStation = onOpenStation)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TesseraContent(
    demo: Boolean,
    loaded: List<Tessera>,
    onGestisci: () -> Unit,
    onOpenStation: (String, Carburante?) -> Unit,
) {
    val context = LocalContext.current
    val container = rememberContainer()
    val repo = container.repository
    // Scope a livello app: refresh/ricerca non crashano se si lascia la schermata.
    val appScope = container.appScope
    val wallet = remember { WalletManager(context) }

    var showWalletInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    var tessere by remember(loaded) { mutableStateOf(loaded) }

    LifecycleResumeEffect(demo) {
        appScope.launch { runCatching { repo.tessere(demo) }.onSuccess { tessere = it } }
        onPauseOrDispose { }
    }

    val name = tessere.firstOrNull { !it.condivisa }?.intestatario?.substringBefore(" ").orEmpty()
    val pager = rememberPagerState(pageCount = { tessere.size.coerceAtLeast(1) })

    val savedRadius by container.prefsStore.radiusFlow().collectAsState(initial = 15)
    // Targhe gia' aggiunte a Wallet: cambia l'etichetta "Aggiungi" -> "Aggiorna".
    val walletAdded by container.prefsStore.walletAddedFlow().collectAsState(initial = emptySet())
    var findTessera by remember { mutableStateOf<Tessera?>(null) }
    var searchFuel by remember { mutableStateOf<Carburante?>(null) }
    var showFuelPicker by remember { mutableStateOf(false) }
    var showRadius by remember { mutableStateOf(false) }
    var finding by remember { mutableStateOf(false) }
    var findMessage by remember { mutableStateOf<String?>(null) }
    var pendingKm by remember { mutableStateOf(0) }
    var customizeTessera by remember { mutableStateOf<Tessera?>(null) }

    fun beginFind(t: Tessera) {
        findTessera = t
        appScope.launch {
            val f = t.carburante ?: runCatching { repo.carburantePerTarga(demo, t.targa) }.getOrNull()
            searchFuel = f
            if (f == null) showFuelPicker = true else showRadius = true
        }
    }

    fun runFind(km: Int) {
        val fuel = searchFuel
        finding = true
        appScope.launch {
            val loc = Geo.currentLocation(context)
            if (loc == null) {
                finding = false
                findMessage = "Posizione non disponibile. Attiva il GPS e riprova."
                return@launch
            }
            val station = runCatching { repo.cheapestStation(demo, fuel, km.toDouble(), loc) }.getOrNull()
            finding = false
            if (station == null) {
                findMessage = "Nessun distributore con ${fuel?.label ?: "questo carburante"} entro $km km da te."
            } else {
                onOpenStation(station.id, fuel)
            }
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) runFind(pendingKm)
        else findMessage = "Serve l'accesso alla posizione per cercare i distributori vicini."
    }

    fun startFind(km: Int) {
        appScope.launch { container.prefsStore.setRadius(km) }
        pendingKm = km
        if (Geo.hasPermission(context)) runFind(km)
        else locationLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.screen),
    ) {
        Spacer(Modifier.height(Space.s3))
        HomeHeader(
            name = Format.properName(name).ifBlank { "ciao" },
            showOptions = tessere.isNotEmpty(),
            onOptions = { showSheet = true },
        )
        Spacer(Modifier.height(Space.s5))

        if (demo) {
            InfoBanner("Stai vedendo dati di esempio. Accedi per i tuoi dati reali.")
            Spacer(Modifier.height(Space.s4))
        }

        if (tessere.isEmpty()) {
            Spacer(Modifier.height(Space.s8))
            Text("Non risultano tessere attive sul tuo profilo.", style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.inkSoft)
            return@Column
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val pagerHeight = maxWidth + 118.dp
            HorizontalPager(
                state = pager,
                pageSpacing = Space.s3,
                modifier = Modifier.fillMaxWidth().height(pagerHeight),
            ) { page ->
                val t = tessere[page]
                TesseraCard(
                    tessera = t,
                    onFind = { beginFind(t) },
                    onPersonalizza = { customizeTessera = t },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (tessere.size > 1) {
            Spacer(Modifier.height(Space.s4))
            PageDots(
                pageCount = tessere.size,
                pagerState = pager,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.height(Space.s7))
    }

    if (showSheet) {
        val current = tessere.getOrNull(pager.currentPage) ?: tessere.first()
        OptionsSheet(
            tessera = current,
            showGestisci = tessere.size > 1,
            walletAlreadyAdded = current.targa in walletAdded,
            onDismiss = { showSheet = false },
            onWallet = {
                showSheet = false
                val activity = context as? Activity
                if (wallet.isConfigured() && activity != null) {
                    // Il JWT lo firma il Worker (rete); poi lo passiamo a Google Pay.
                    appScope.launch {
                        val jwt = wallet.fetchSaveJwt(current)
                        if (jwt != null) {
                            Pay.getClient(activity).savePassesJwt(jwt, activity, WalletManager.REQUEST_CODE)
                            container.prefsStore.markWalletAdded(current.targa)
                        } else {
                            showWalletInfo = true
                        }
                    }
                } else {
                    showWalletInfo = true
                }
            },
            onShare = {
                showSheet = false
                shareQrImage(context, current.qrPayload, current.targa)
            },
            onGestisci = {
                showSheet = false
                onGestisci()
            },
        )
    }

    if (showFuelPicker) {
        FuelPickerDialog(
            onPick = { c ->
                val t = findTessera
                showFuelPicker = false
                if (t != null) {
                    appScope.launch { repo.setTessereFuel(t.targa, c.name) }
                    searchFuel = c
                    showRadius = true
                }
            },
            onDismiss = { showFuelPicker = false },
        )
    }

    if (showRadius) {
        RadiusDialog(
            initialKm = savedRadius,
            fuel = searchFuel,
            onConfirm = { km -> showRadius = false; startFind(km) },
            onDismiss = { showRadius = false },
        )
    }

    if (finding) FindingDialog()

    findMessage?.let { msg -> MessageDialog(message = msg, onDismiss = { findMessage = null }) }

    if (showWalletInfo) WalletInfoDialog(onDismiss = { showWalletInfo = false })

    customizeTessera?.let { t ->
        CustomizeTesseraDialog(
            tessera = t,
            onSave = { name2, colorHex, fuel ->
                appScope.launch {
                    repo.setTessereName(t.targa, name2)
                    repo.setTessereColor(t.targa, colorHex)
                    repo.setTessereFuel(t.targa, fuel)
                }
                customizeTessera = null
            },
            onDismiss = { customizeTessera = null },
        )
    }
}

private fun greetingForNow(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "Buongiorno"
    in 12..13 -> "Buon pranzo"
    in 14..17 -> "Buon pomeriggio"
    in 18..22 -> "Buonasera"
    else -> "Buonanotte"
}

@Composable
private fun HomeHeader(name: String, showOptions: Boolean, onOptions: () -> Unit) {
    val greeting = remember { greetingForNow() }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("$greeting,", style = MaterialTheme.typography.labelLarge, color = Pieno.colors.inkSoft)
            Spacer(Modifier.height(2.dp))
            Text(name, style = MaterialTheme.typography.displayLarge, color = Pieno.colors.ink)
        }
        if (showOptions) {
            PienoIconButton(
                icon = PienoIcons.MoreHorizontal,
                contentDescription = "Opzioni tessera",
                onClick = onOptions,
                container = Pieno.colors.surface,
                tint = Pieno.colors.ink,
                size = 46,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsSheet(
    tessera: Tessera,
    showGestisci: Boolean,
    walletAlreadyAdded: Boolean,
    onDismiss: () -> Unit,
    onWallet: () -> Unit,
    onShare: () -> Unit,
    onGestisci: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Pieno.colors.surface) {
        Column(Modifier.fillMaxWidth().padding(bottom = Space.s8)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s5),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Opzioni", style = MaterialTheme.typography.headlineMedium, color = Pieno.colors.ink, modifier = Modifier.weight(1f))
                val cardColor = remember(tessera.colore) {
                    tessera.colore?.let { runCatching { Color(it.toLong(16).toInt()) }.getOrNull() } ?: Green
                }
                Pill(text = tessera.targa, container = cardColor, contentColor = Color.White)
            }
            Spacer(Modifier.height(Space.s4))
            SheetItem(
                PienoIcons.Wallet,
                if (walletAlreadyAdded) "Aggiorna Google Wallet" else "Aggiungi a Google Wallet",
                onWallet,
            )
            SheetItem(PienoIcons.Share, "Condividi QR", onShare)
            if (showGestisci) {
                SheetItem(PienoIcons.ListView, "Riordina tessere", onGestisci)
            }
        }
    }
}

@Composable
private fun SheetItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Space.s5, vertical = Space.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Pieno.colors.ink, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(Space.s4))
        Text(label, style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FuelPickerDialog(onPick: (Carburante) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text("Carburante dell'auto", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(
                "Questa tessera non indica il carburante. Scegli quello dell'auto: lo ricorderò (modificabile da Personalizza).",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
            Spacer(Modifier.height(Space.s4))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
                verticalArrangement = Arrangement.spacedBy(Space.s2),
            ) {
                Carburante.entries.forEach { c ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radii.sm))
                            .background(Pieno.colors.paperDim)
                            .clickable { onPick(c) }
                            .padding(horizontal = Space.s4, vertical = Space.s3),
                    ) {
                        Text(fuelDisplay(c), style = MaterialTheme.typography.labelLarge, color = Pieno.colors.ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadiusDialog(initialKm: Int, fuel: Carburante?, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var km by remember { mutableFloatStateOf(initialKm.toFloat()) }
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text("Cerca il più economico", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(
                if (fuel != null) "Il prezzo più basso di ${fuel.label} vicino a te."
                else "Il distributore più conveniente per quest'auto, vicino a te.",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
            Spacer(Modifier.height(Space.s6))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    "${km.roundToInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp, lineHeight = 48.sp),
                    color = Pieno.colors.accent,
                )
                Spacer(Modifier.width(Space.s2))
                Text(
                    "km",
                    style = MaterialTheme.typography.titleMedium,
                    color = Pieno.colors.inkSoft,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Spacer(Modifier.height(Space.s3))
            Slider(value = km, onValueChange = { km = it }, valueRange = 1f..50f, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Space.s5))
            PrimaryButton(
                text = "Cerca",
                onClick = { onConfirm(km.roundToInt()) },
                leadingIcon = PienoIcons.Navigation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FindingDialog() {
    Dialog(onDismissRequest = {}) {
        PienoCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = Pieno.colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(Space.s4))
                Text("Cerco il distributore più economico…", style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
            }
        }
    }
}

@Composable
private fun MessageDialog(message: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text(message, style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s4))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(text = "Ho capito", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun WalletInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text("Aggiungi a Google Wallet", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(
                "Per salvare la tessera in Google Wallet serve configurare un account issuer " +
                    "(Google Wallet Console) e un service account. La guida è nel README dell'app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
            Spacer(Modifier.height(Space.s4))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(text = "Ho capito", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun TesseraLoading() {
    Column(Modifier.fillMaxSize().padding(horizontal = Space.screen)) {
        Spacer(Modifier.height(Space.s7))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SkeletonBox(Modifier.fillMaxWidth(0.3f).height(16.dp))
                Spacer(Modifier.height(Space.s2))
                SkeletonBox(Modifier.fillMaxWidth(0.55f).height(34.dp))
            }
            SkeletonBox(Modifier.size(46.dp), shape = CircleShape)
        }
        Spacer(Modifier.height(Space.s6))
        SkeletonBox(Modifier.fillMaxWidth().height(440.dp), shape = RoundedCornerShape(Radii.card))
    }
}

@Composable
private fun TesseraCard(
    tessera: Tessera,
    onFind: () -> Unit,
    onPersonalizza: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titolo = tessera.etichetta?.takeIf { it.isNotBlank() } ?: Format.properName(tessera.intestatario)
    val cardColor = remember(tessera.colore) {
        tessera.colore?.let { runCatching { Color(it.toLong(16).toInt()) }.getOrNull() } ?: Green
    }
    var showQr by remember { mutableStateOf(false) }
    if (showQr) {
        FullscreenQrDialog(tessera, onDismiss = { showQr = false })
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.card))
            .background(cardColor)
            .padding(Space.s4),
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (tessera.condivisa) "Tessera condivisa" else "Tessera carburante",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnCard.copy(alpha = 0.62f),
                )
                Spacer(Modifier.height(3.dp))
                Text(titolo, style = MaterialTheme.typography.titleLarge, color = OnCard, maxLines = 1)
            }
            Pill(text = tessera.targa, container = Color.Black.copy(alpha = 0.22f), contentColor = OnCard)
        }

        Spacer(Modifier.height(Space.s3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(Radii.md))
                .background(QrPanel)
                .clickable { showQr = true }
                .padding(Space.s4),
            contentAlignment = Alignment.Center,
        ) {
            QrImage(content = tessera.qrPayload, modifier = Modifier.fillMaxSize(), foreground = QrInk, background = QrPanel)
        }

        Spacer(Modifier.height(Space.s3))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.s2), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radii.md))
                    .background(CardAction)
                    .clickable { onFind() }
                    .padding(horizontal = Space.s4, vertical = Space.s3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(PienoIcons.Navigation, contentDescription = null, tint = OnCard, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Space.s2))
                Text("Trova il più economico", style = MaterialTheme.typography.labelLarge, color = OnCard, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(Radii.md))
                    .background(CardAction)
                    .clickable { onPersonalizza() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(PienoIcons.Edit, contentDescription = "Personalizza tessera", tint = OnCard, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// Indicatore di pagina: pallini con spazio costante (spacedBy); quello attivo si
// allunga e colora seguendo la posizione del pager.
@Composable
private fun PageDots(pageCount: Int, pagerState: PagerState, modifier: Modifier = Modifier) {
    val pos = pagerState.currentPage + pagerState.currentPageOffsetFraction
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { i ->
            val activeness = (1f - abs(i - pos)).coerceIn(0f, 1f)
            Box(
                Modifier
                    .height(8.dp)
                    .width(lerpDp(8.dp, 22.dp, activeness))
                    .clip(CircleShape)
                    .background(lerpColor(Pieno.colors.hairline, Pieno.colors.accent, activeness)),
            )
        }
    }
}
