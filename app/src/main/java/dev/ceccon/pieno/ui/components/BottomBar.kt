package dev.ceccon.pieno.ui.components
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.ui.nav.Tab
@Composable
fun PienoBottomBar(currentRoute: String?, onSelect: (Tab) -> Unit) {
    val tabs = Tab.entries
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    Column(Modifier.background(Pieno.colors.surface)) {
        HairlineDivider()
        BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPadding()) {
            val segWidth = maxWidth / tabs.size
            val indicatorWidth = 18.dp
            // Un solo indicatore che scivola tra le voci, invece di apparire/sparire.
            val offsetX by animateDpAsState(
                targetValue = segWidth * selectedIndex + (segWidth - indicatorWidth) / 2,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
                label = "navIndicator",
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(62.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    BottomItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onSelect(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = offsetX)
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Pieno.colors.accent),
            )
        }
    }
}

@Composable
private fun BottomItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(if (selected) Pieno.colors.ink else Pieno.colors.inkSoft, label = "tint")
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}
