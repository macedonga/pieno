package dev.ceccon.pieno.ui.nav

import androidx.compose.ui.graphics.vector.ImageVector
import dev.ceccon.pieno.ui.icons.PienoIcons

// Tessera e' la prima: compito primario dell'app.
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Tessera("tessera", "Tessera", PienoIcons.Card),
    Stazioni("stazioni", "Stazioni", PienoIcons.Pump),
    Storico("storico", "Storico", PienoIcons.Receipt),
    Profilo("profilo", "Profilo", PienoIcons.Person),
}
