package dev.ceccon.pieno.ui.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.theme.InkSoft
import dev.ceccon.pieno.ui.theme.Ink
import dev.ceccon.pieno.ui.theme.Paper
import dev.ceccon.pieno.ui.theme.Surface as SurfaceColor

@Composable
fun IconGallery() {
    val items = listOf(
        "Card" to PienoIcons.Card,
        "Pin" to PienoIcons.Pin,
        "Receipt" to PienoIcons.Receipt,
        "Person" to PienoIcons.Person,
        "Droplet" to PienoIcons.Droplet,
        "Pump" to PienoIcons.Pump,
        "Refresh" to PienoIcons.Refresh,
        "Wallet" to PienoIcons.Wallet,
        "Share" to PienoIcons.Share,
        "Scan" to PienoIcons.Scan,
        "Bell" to PienoIcons.Bell,
        "Info" to PienoIcons.Info,
        "Logout" to PienoIcons.Logout,
        "Close" to PienoIcons.Close,
        "Check" to PienoIcons.Check,
        "ChevRight" to PienoIcons.ChevronRight,
        "ChevLeft" to PienoIcons.ChevronLeft,
        "ChevDown" to PienoIcons.ChevronDown,
        "Search" to PienoIcons.Search,
        "Sliders" to PienoIcons.Sliders,
        "ArrowRight" to PienoIcons.ArrowRight,
        "Plus" to PienoIcons.Plus,
    )
    Surface(Modifier.fillMaxSize(), color = Paper) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 70.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(items) { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(item.second, contentDescription = item.first, tint = Ink, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(item.first, style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            }
        }
    }
}
