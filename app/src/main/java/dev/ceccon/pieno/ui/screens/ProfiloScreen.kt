package dev.ceccon.pieno.ui.screens
import dev.ceccon.pieno.ui.theme.Pieno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.core.Format
import dev.ceccon.pieno.data.model.Beneficiario
import dev.ceccon.pieno.ui.components.HairlineDivider
import dev.ceccon.pieno.ui.components.LeadingIcon
import dev.ceccon.pieno.ui.components.ListRow
import dev.ceccon.pieno.ui.components.Loader
import dev.ceccon.pieno.ui.components.PienoCard
import dev.ceccon.pieno.ui.components.PienoIconButton
import dev.ceccon.pieno.ui.components.ScreenTitle
import dev.ceccon.pieno.ui.components.SkeletonBox
import dev.ceccon.pieno.ui.icons.PienoIcons
import dev.ceccon.pieno.ui.rememberContainer
import dev.ceccon.pieno.ui.theme.Radii
import dev.ceccon.pieno.ui.theme.Space
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import dev.ceccon.pieno.data.local.ThemeStore
import kotlinx.coroutines.launch

@Composable
fun ProfiloScreen(
    demo: Boolean,
    onOpenCondivise: () -> Unit,
    onOpenComunicazioni: () -> Unit,
    onOpenInfo: () -> Unit,
    onLogout: () -> Unit,
) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val themeMode by container.themeStore.flow().collectAsState(initial = ThemeStore.SYSTEM)
    var showTheme by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = Space.screen, end = Space.screen, top = Space.s3, bottom = Space.s9),
    ) {
        item {
            ScreenTitle("Profilo")
            Spacer(Modifier.height(Space.s5))
        }
        // L'anagrafica ha il suo caricamento: se fallisce non nasconde il resto,
        // cosi' "Esci" resta sempre raggiungibile.
        item {
            AnagraficaSection(demo)
            Spacer(Modifier.height(Space.s4))
        }
        item {
            PienoCard(contentPadding = PaddingValues(vertical = Space.s1)) {
                ListRow(
                    headline = "Tessere condivise",
                    supporting = "Auto non intestate a te",
                    leading = { LeadingIcon(PienoIcons.Card) },
                    trailing = { Icon(PienoIcons.ChevronRight, contentDescription = null, tint = Pieno.colors.inkSoft, modifier = Modifier.size(20.dp)) },
                    onClick = onOpenCondivise,
                )
                HairlineDivider(Modifier.padding(start = Space.s8))
                ListRow(
                    headline = "Comunicazioni",
                    supporting = "Avvisi e novità",
                    leading = { LeadingIcon(PienoIcons.Bell) },
                    trailing = { Icon(PienoIcons.ChevronRight, contentDescription = null, tint = Pieno.colors.inkSoft, modifier = Modifier.size(20.dp)) },
                    onClick = onOpenComunicazioni,
                )
                HairlineDivider(Modifier.padding(start = Space.s8))
                ListRow(
                    headline = "Tema",
                    supporting = themeLabel(themeMode),
                    leading = { LeadingIcon(PienoIcons.Droplet) },
                    trailing = { Icon(PienoIcons.ChevronRight, contentDescription = null, tint = Pieno.colors.inkSoft, modifier = Modifier.size(20.dp)) },
                    onClick = { showTheme = true },
                )
            }
            Spacer(Modifier.height(Space.s4))
        }
        item {
            PienoCard(contentPadding = PaddingValues(vertical = Space.s1)) {
                ListRow(
                    headline = "Informazioni",
                    leading = { LeadingIcon(PienoIcons.Info) },
                    trailing = { Icon(PienoIcons.ChevronRight, contentDescription = null, tint = Pieno.colors.inkSoft, modifier = Modifier.size(20.dp)) },
                    onClick = onOpenInfo,
                )
                HairlineDivider(Modifier.padding(start = Space.s8))
                ListRow(
                    headline = if (demo) "Accedi" else "Esci",
                    leading = { LeadingIcon(PienoIcons.Logout, tint = Pieno.colors.accent) },
                    onClick = onLogout,
                )
            }
        }
    }

    if (showTheme) {
        ThemeDialog(
            current = themeMode,
            onSelect = { mode ->
                scope.launch { container.themeStore.set(mode) }
                showTheme = false
            },
            onDismiss = { showTheme = false },
        )
    }
}

@Composable
private fun AnagraficaSection(demo: Boolean) {
    val repo = rememberContainer().repository
    Loader(
        key = demo,
        load = { repo.beneficiario(demo) },
        loading = { SkeletonBox(Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(Radii.card)) },
    ) { b ->
        AnagraficaCard(b)
    }
}

private fun themeLabel(mode: String): String = when (mode) {
    ThemeStore.DARK -> "Scuro"
    ThemeStore.LIGHT -> "Chiaro"
    else -> "Sistema"
}

@Composable
private fun ThemeDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        PienoCard(Modifier.fillMaxWidth()) {
            Text("Tema", style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
            Spacer(Modifier.height(Space.s3))
            ThemeOption("Sistema", ThemeStore.SYSTEM, current, onSelect)
            ThemeOption("Chiaro", ThemeStore.LIGHT, current, onSelect)
            ThemeOption("Scuro", ThemeStore.DARK, current, onSelect)
        }
    }
}

@Composable
private fun ThemeOption(label: String, value: String, current: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect(value) }.padding(vertical = Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Pieno.colors.ink, modifier = Modifier.weight(1f))
        if (value == current) {
            Icon(PienoIcons.Check, contentDescription = null, tint = Pieno.colors.accent, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun AnagraficaCard(b: Beneficiario) {
    var revealed by remember { mutableStateOf(false) }
    PienoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(Pieno.colors.greenSoft),
                contentAlignment = Alignment.Center,
            ) {
                val initials = listOfNotNull(b.nome.firstOrNull(), b.cognome.firstOrNull()).joinToString("")
                Text(initials.ifBlank { "?" }, style = MaterialTheme.typography.titleLarge, color = Pieno.colors.onGreenSoft)
            }
            Spacer(Modifier.width(Space.s4))
            Column(Modifier.weight(1f)) {
                Text(Format.properName("${b.nome} ${b.cognome}".trim()), style = MaterialTheme.typography.titleLarge, color = Pieno.colors.ink)
                if (b.comune.isNotBlank()) Text(Format.properName(b.comune), style = MaterialTheme.typography.bodyMedium, color = Pieno.colors.inkSoft)
            }
            // Occhio per mostrare/nascondere i dati sensibili: piu' pulito di un bottone testuale.
            PienoIconButton(
                icon = PienoIcons.Eye,
                contentDescription = if (revealed) "Nascondi dati" else "Mostra dati",
                onClick = { revealed = !revealed },
                container = if (revealed) Pieno.colors.greenSoft else Pieno.colors.paperDim,
                tint = if (revealed) Pieno.colors.onGreenSoft else Pieno.colors.inkSoft,
                size = 42,
            )
        }
        Spacer(Modifier.height(Space.s4))
        HairlineDivider()
        Spacer(Modifier.height(Space.s3))
        if (b.codiceFiscale.isNotBlank()) {
            Field("Codice fiscale", if (revealed) b.codiceFiscale else Format.maskFiscalCode(b.codiceFiscale))
            Spacer(Modifier.height(Space.s3))
        }
        if (b.email.isNotBlank()) {
            Field("Email", if (revealed) b.email else Format.maskEmail(b.email))
            Spacer(Modifier.height(Space.s3))
        }
        if (b.telefono.isNotBlank()) {
            Field("Telefono", if (revealed) b.telefono else Format.maskPhone(b.telefono))
        }
    }
}

@Composable
private fun Field(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Pieno.colors.inkSoft)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Pieno.colors.ink)
    }
}
