package dev.ceccon.pieno

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.ceccon.pieno.ui.PendingDeepLink
import dev.ceccon.pieno.ui.PienoRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        setContent {
            PienoRoot()
        }
    }

    // App gia' in primo piano/background (singleTop): l'intent arriva qui.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    // pieno://storico (pulsante del pass Google Wallet) -> apre lo Storico.
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "pieno") PendingDeepLink.route = data.host
    }
}
