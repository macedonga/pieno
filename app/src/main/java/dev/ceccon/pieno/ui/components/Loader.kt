package dev.ceccon.pieno.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ceccon.pieno.core.UiState
import dev.ceccon.pieno.ui.theme.Space
import kotlinx.coroutines.CancellationException

@Composable
fun <T> Loader(
    key: Any?,
    load: suspend () -> T,
    modifier: Modifier = Modifier,
    initial: T? = null,
    loading: @Composable () -> Unit = { ListSkeleton() },
    content: @Composable (T) -> Unit,
) {
    var reload by remember { mutableIntStateOf(0) }
    // Con un valore iniziale (cache) si parte gia' dal contenuto: niente skeleton
    // quando si torna su una schermata gia' caricata in questa sessione.
    val start: UiState<T> = if (initial != null) UiState.Content(initial) else UiState.Loading
    val state by produceState(start, key, reload) {
        if (initial == null) value = UiState.Loading
        value = try {
            UiState.Content(load())
        } catch (e: CancellationException) {
            // Cambio tab / chiave: e' una cancellazione normale, non un errore da
            // mostrare ("The coroutine scope left the composition").
            throw e
        } catch (e: Throwable) {
            if (initial != null) UiState.Content(initial) else UiState.Error(e.message ?: "Errore di rete")
        }
    }
    when (val s = state) {
        is UiState.Loading -> loading()
        is UiState.Content -> content(s.data)
        is UiState.Error -> ErrorState(message = s.message, onRetry = { reload++ }, modifier = modifier)
    }
}

@Composable
fun ListSkeleton(rows: Int = 5) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Space.screen, vertical = Space.s4)) {
        SkeletonBox(Modifier.fillMaxWidth(0.5f).height(34.dp))
        Spacer(Modifier.height(Space.s5))
        repeat(rows) {
            SkeletonBox(Modifier.fillMaxWidth().height(64.dp))
            Spacer(Modifier.height(Space.s3))
        }
    }
}
