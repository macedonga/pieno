package dev.ceccon.pieno.ui
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.ui.components.PienoBottomBar
import dev.ceccon.pieno.ui.nav.Tab
import dev.ceccon.pieno.ui.screens.ComunicazioniScreen
import dev.ceccon.pieno.ui.screens.CondiviseScreen
import dev.ceccon.pieno.ui.screens.GestisciTessereScreen
import dev.ceccon.pieno.ui.screens.InfoScreen
import dev.ceccon.pieno.ui.screens.ProfiloScreen
import dev.ceccon.pieno.ui.screens.ScanScreen
import dev.ceccon.pieno.ui.screens.StationDetailScreen
import dev.ceccon.pieno.ui.screens.StazioniScreen
import dev.ceccon.pieno.ui.screens.StoricoScreen
import dev.ceccon.pieno.ui.screens.TesseraScreen
// Deep link in attesa (es. dal pass Wallet "pieno://storico"). Lo setta
// MainActivity (onCreate/onNewIntent); MainShell lo consuma quando e' montato,
// cosi' funziona sia ad app chiusa sia in background.
object PendingDeepLink {
    var route by mutableStateOf<String?>(null)
}

@Composable
fun MainShell(
    demo: Boolean,
    onLogout: () -> Unit,
) {
    val nav = rememberNavController()
    LaunchedEffect(PendingDeepLink.route) {
        if (PendingDeepLink.route == "storico") {
            nav.switchTab(Tab.Storico)
            PendingDeepLink.route = null
        }
    }
    // Precarico in background i dati pesanti all'avvio (rifornimenti, stazioni): cosi'
    // il dettaglio (bottone "I tuoi acquisti qui"), lo storico e la mappa sono pronti
    // subito, senza il ritardo della rete alla prima apertura.
    val container = rememberContainer()
    LaunchedEffect(demo) {
        runCatching { container.repository.rifornimenti(demo) }
        runCatching { container.repository.stazioni(demo) }
    }
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBottom = Tab.entries.any { it.route == route }

    Scaffold(
        containerColor = Pieno.colors.paper,
        // Consumiamo solo la navigation bar (in basso). La status bar (in alto) la
        // gestisce ogni schermata: cosi' il modifier del NavHost resta STABILE e non
        // c'e' shift di layout durante la transizione (il dettaglio va edge-to-edge,
        // le altre mettono statusBarsPadding da sole).
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (showBottom) {
                PienoBottomBar(route) { tab -> nav.switchTab(tab) }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Tab.Tessera.route,
            modifier = Modifier.fillMaxSize().padding(inner),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
        ) {
            composable(Tab.Tessera.route) {
                TesseraScreen(
                    demo = demo,
                    onGestisci = { nav.navigate("gestisci") },
                    onOpenStation = { id, fuel -> nav.openStation(id, fuel) },
                )
            }
            composable(Tab.Stazioni.route) {
                StazioniScreen(demo = demo, onOpenStation = { id, fuel -> nav.openStation(id, fuel) })
            }
            composable(Tab.Storico.route) {
                StoricoScreen(demo = demo)
            }
            composable(Tab.Profilo.route) {
                ProfiloScreen(
                    demo = demo,
                    onOpenCondivise = { nav.navigate("condivise") },
                    onOpenComunicazioni = { nav.navigate("comunicazioni") },
                    onOpenInfo = { nav.navigate("info") },
                    onLogout = onLogout,
                )
            }
            composable(
                "stazione/{id}?fuel={fuel}",
                arguments = listOf(navArgument("fuel") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { entry ->
                StationDetailScreen(
                    stationId = entry.arguments?.getString("id").orEmpty(),
                    fuel = Carburante.fromName(entry.arguments?.getString("fuel")),
                    demo = demo,
                    onBack = { nav.popBackStack() },
                )
            }
            composable("comunicazioni") { ComunicazioniScreen(demo = demo, onBack = { nav.popBackStack() }) }
            composable("condivise") {
                CondiviseScreen(onBack = { nav.popBackStack() }, onScan = { nav.navigate("scan") })
            }
            composable("scan") { ScanScreen(onBack = { nav.popBackStack() }) }
            composable("gestisci") { GestisciTessereScreen(demo = demo, onBack = { nav.popBackStack() }) }
            composable("info") { InfoScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

private fun NavController.openStation(id: String, fuel: Carburante?) {
    navigate("stazione/$id" + (fuel?.let { "?fuel=${it.name}" } ?: ""))
}

private fun NavController.switchTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
