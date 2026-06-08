package dev.ceccon.pieno.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoTopBar
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.theme.Green
import dev.ceccon.pieno.ui.theme.Paper
import dev.ceccon.pieno.ui.theme.Pieno
import dev.ceccon.pieno.ui.theme.Space

@Composable
fun InfoScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PienoTopBar("Informazioni", onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Space.s4))
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Green),
                contentAlignment = Alignment.Center,
            ) {
                Icon(PienoIcons.Droplet, contentDescription = null, tint = Paper, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(Space.s3))
            Text("Pieno", style = MaterialTheme.typography.headlineMedium, color = Pieno.colors.ink)
            Text("versione 0.1.0", style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
            Spacer(Modifier.height(Space.s5))

            PienoCard {
                Text(
                    "Pieno mostra la tessera carburante agevolata del Friuli Venezia Giulia come " +
                        "codice QR, pronta alla pompa anche senza connessione. Mostra inoltre i " +
                        "distributori convenzionati con i prezzi, lo storico dei rifornimenti e gli avvisi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Pieno.colors.ink,
                )
            }
            Spacer(Modifier.height(Space.s3))
            PienoCard {
                Text("Privacy", style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
                Spacer(Modifier.height(Space.s2))
                Text(
                    "L'accesso avviene tramite il sistema regionale LoginFVG (SPID o CIE). " +
                        "I dati e i token restano sul tuo dispositivo. Nessun dato passa da server di terzi." +
                            " L'app non contiene pubblicità né funzionalità di tracciamento." +
                            " Quando aggiungi una tessera a Google Wallet, i dati necessari per generare il QR code vengono inviati a Google, ma solo per quella tessera e solo se scegli di farlo." + 
                            " Nessun dato viene salvato.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Pieno.colors.ink,
                )
            }
            Spacer(Modifier.height(Space.s3))
            PienoCard {
                Text(
                    "App indipendente, sviluppata per uso personale e di interoperabilità. " +
                        "Non è affiliata, sponsorizzata o approvata da Insiel S.p.A. né dalla " +
                        "Regione Autonoma Friuli Venezia Giulia. Tutti i marchi citati appartengono " +
                        "ai rispettivi proprietari.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Pieno.colors.inkSoft,
                )
            }
            Spacer(Modifier.height(Space.s7))
        }
    }
}
