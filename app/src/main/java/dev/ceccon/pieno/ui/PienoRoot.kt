package dev.ceccon.pieno.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.ceccon.pieno.data.local.ThemeStore
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.screens.LoginScreen
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Paper
import dev.ceccon.pieno.ui.theme.Pieno
import dev.ceccon.pieno.ui.theme.PienoTheme
import kotlinx.coroutines.launch

/**
 * Radice: tema (di default segue il sistema, con override salvato) e gate sessione.
 */
@Composable
fun PienoRoot() {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()

    var demoMode by remember { mutableStateOf(false) }
    var loggingIn by remember { mutableStateOf(false) }
    val loggedIn by container.authManager.isLoggedIn().collectAsState(initial = null)
    val themeMode by container.themeStore.flow().collectAsState(initial = ThemeStore.SYSTEM)

    val dark = when (themeMode) {
        ThemeStore.DARK -> true
        ThemeStore.LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    // I dati di rete si ricaricano all'apertura/ritorno nell'app, non a ogni
    // navigazione tra le schede (che usano la cache in memoria).
    LifecycleResumeEffect(Unit) {
        container.repository.invalidate()
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            scope.launch {
                runCatching { container.authManager.handleAuthResult(data) }
                loggingIn = false
            }
        } else {
            loggingIn = false
        }
    }

    PienoTheme(darkTheme = dark) {
        when {
            loggedIn == null -> SplashScreen()
            loggedIn == true || demoMode -> MainShell(
                demo = loggedIn != true,
                onLogout = {
                    demoMode = false
                    scope.launch { container.authManager.logout() }
                },
            )
            else -> LoginScreen(
                loading = loggingIn,
                onLogin = {
                    loggingIn = true
                    launcher.launch(container.authManager.buildAuthIntent())
                },
                onDemo = { demoMode = true },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Pieno.colors.paper), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(Green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PienoIcons.Droplet, contentDescription = null, tint = Paper, modifier = Modifier.size(40.dp))
        }
    }
}
