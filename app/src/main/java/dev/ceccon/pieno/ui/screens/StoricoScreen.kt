package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.data.model.Rifornimento
import dev.ceccon.pieno.ui.components.EmptyState
import dev.ceccon.pieno.ui.components.HairlineDivider
import dev.ceccon.pieno.ui.components.InfoBanner
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.ScreenTitle
import dev.ceccon.pieno.ui.components.SectionHeader
import dev.ceccon.pieno.ui.components.SkeletonBox
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space
@Composable
fun StoricoScreen(demo: Boolean) {
    val repo = rememberContainer().repository
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Loader(key = demo, load = { repo.rifornimenti(demo) }, loading = { StoricoSkeleton() }) { rifornimenti ->
            StoricoContent(demo, rifornimenti)
        }
    }
}

@Composable
private fun StoricoSkeleton() {
    Column(Modifier.fillMaxSize().padding(horizontal = Space.screen)) {
        Spacer(Modifier.height(Space.s3))
        SkeletonBox(Modifier.fillMaxWidth(0.4f).height(34.dp))
        Spacer(Modifier.height(Space.s2))
        SkeletonBox(Modifier.fillMaxWidth(0.55f).height(18.dp))
        Spacer(Modifier.height(Space.s5))
        Row(Modifier.fillMaxWidth()) {
            SkeletonBox(Modifier.weight(1f).height(112.dp), shape = RoundedCornerShape(Radii.card))
            Spacer(Modifier.width(Space.s3))
            SkeletonBox(Modifier.weight(1f).height(112.dp), shape = RoundedCornerShape(Radii.card))
        }
        Spacer(Modifier.height(Space.s5))
        repeat(4) {
            SkeletonBox(Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(Radii.card))
            Spacer(Modifier.height(Space.s3))
        }
    }
}

@Composable
private fun StoricoContent(demo: Boolean, rifornimenti: List<Rifornimento>) {
    val totaleSpeso = remember(rifornimenti) { rifornimenti.sumOf { it.importo } }
    val totaleSconto = remember(rifornimenti) { rifornimenti.sumOf { it.sconto } }
    val primaData = remember(rifornimenti) { rifornimenti.minOfOrNull { it.dataEpochDay } }
    val groups = remember(rifornimenti) { rifornimenti.groupBy { Format.monthYear(it.dataEpochDay) } }
    var selected by remember { mutableStateOf<Rifornimento?>(null) }

    LazyColumn(contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.s3, bottom = Space.s9)) {
        item {
            ScreenTitle("Storico")
            Spacer(Modifier.height(Space.s5))
        }
        item {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    label = "Soldi spesi",
                    value = totaleSpeso,
                    sub = primaData?.let { "dal ${Format.dateNumeric(it)}" } ?: "nessun rifornimento",
                    valueColor = Pieno.colors.ink,
                )
                Spacer(Modifier.width(Space.s3))
                StatCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    label = "Sconto risparmiato",
                    value = totaleSconto,
                    sub = "in ${rifornimenti.size} rifornimenti",
                    valueColor = Pieno.colors.accent,
                )
            }
            Spacer(Modifier.height(Space.s4))
        }
        if (demo) {
            item {
                InfoBanner("Dati di esempio. Accedi per il tuo storico reale.")
                Spacer(Modifier.height(Space.s4))
            }
        }
        if (rifornimenti.isEmpty()) {
            item {
                EmptyState(
                    title = "Nessun rifornimento",
                    subtitle = "Qui compariranno i tuoi rifornimenti con lo sconto applicato.",
                    icon = PienoIcons.Receipt,
                )
            }
        }
        groups.forEach { (mese, items) ->
            item { SectionHeader(mese) }
            item {
                PienoCard(contentPadding = PaddingValues(vertical = Space.s1)) {
                    items.forEachIndexed { index, r ->
                        RifornimentoRow(r, onClick = { selected = r })
                        if (index < items.size - 1) HairlineDivider(Modifier.padding(start = Space.s8))
                    }
                }
                Spacer(Modifier.height(Space.s4))
            }
        }
    }

    selected?.let { r ->
        RifornimentoSheet(r, onDismiss = { selected = null })
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: Double, sub: String, valueColor: Color) {
    PienoCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Pieno.colors.inkSoft, maxLines = 1)
        Spacer(Modifier.height(Space.s2))
        Text(
            Format.euro(value),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp, lineHeight = 34.sp),
            color = valueColor,
            maxLines = 1,
        )
        Spacer(Modifier.height(Space.s1))
        Text(sub, style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft, maxLines = 1)
    }
}

@Composable
private fun RifornimentoRow(r: Rifornimento, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Space.s4, vertical = Space.s4),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text("${r.stazione}, ${r.comune}", style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink)
            Spacer(Modifier.height(2.dp))
            Text(
                "${Format.dateMedium(r.dataEpochDay)} · ${r.carburante.label} · ${Format.liters(r.litri)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Pieno.colors.inkSoft,
            )
        }
        Spacer(Modifier.width(Space.s3))
        Column(horizontalAlignment = Alignment.End) {
            Text(Format.euro(r.importo), style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink, textAlign = TextAlign.End)
            Spacer(Modifier.height(2.dp))
            Text("sconto ${Format.euro(r.sconto)}", style = MaterialTheme.typography.labelMedium, color = Pieno.colors.accent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RifornimentoSheet(r: Rifornimento, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Pieno.colors.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = Space.s5).padding(bottom = Space.s8)) {
            Text("${r.stazione}, ${r.comune}", style = MaterialTheme.typography.headlineMedium, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s2))
            Text(Format.dateLong(r.dataEpochDay), style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft)
            Spacer(Modifier.height(Space.s5))
            DetailRow("Carburante", r.carburante.label)
            DetailRow("Litri", Format.liters(r.litri))
            DetailRow("Prezzo al litro", "${Format.pricePerLiter(r.prezzoLitroEffettivo)} €/L")
            DetailRow("Importo pagato", Format.euro(r.importo))
            DetailRow("Sconto applicato", Format.euro(r.sconto), valueColor = Pieno.colors.accent)
            if (r.targa.isNotBlank()) DetailRow("Targa", r.targa)
            if (r.stato.isNotBlank()) DetailRow("Stato", r.stato)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Pieno.colors.ink) {
    Row(Modifier.fillMaxWidth().padding(vertical = Space.s3), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.inkSoft, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}
