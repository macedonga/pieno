package dev.ceccon.pieno.data.repo

import dev.ceccon.pieno.core.AppConfig
import dev.ceccon.pieno.core.Geo
import dev.ceccon.pieno.core.LatLon
import dev.ceccon.pieno.data.api.PienoApi
import dev.ceccon.pieno.data.api.RifornimentoDto
import dev.ceccon.pieno.data.api.toDomain
import dev.ceccon.pieno.data.auth.AuthManager
import dev.ceccon.pieno.data.local.CachedTessera
import dev.ceccon.pieno.data.local.CondiviseStore
import dev.ceccon.pieno.data.local.CondivisaEntry
import dev.ceccon.pieno.data.local.StationFavoritesStore
import dev.ceccon.pieno.data.local.TesseraCacheStore
import dev.ceccon.pieno.data.local.TessereStore
import dev.ceccon.pieno.data.model.Beneficiario
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Comunicazione
import dev.ceccon.pieno.data.model.Rifornimento
import dev.ceccon.pieno.data.model.Stazione
import dev.ceccon.pieno.data.model.Tessera
import dev.ceccon.pieno.data.qr.Hc1Decoder
import dev.ceccon.pieno.demo.DemoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.LocalDate

// Sorgente unica dei dati. In demo restituisce dati di esempio, altrimenti chiama
// l'API reale. I risultati di rete sono in cache in memoria: si caricano una
// volta per apertura dell'app (invalidate() al resume), non a ogni navigazione.
class Repository(
    private val api: PienoApi,
    private val auth: AuthManager,
    private val condiviseStore: CondiviseStore,
    private val tessereStore: TessereStore,
    private val tesseraCacheStore: TesseraCacheStore,
    private val stationFavoritesStore: StationFavoritesStore,
) {
    @Volatile private var cacheDemo: Boolean? = null
    @Volatile private var stazioniCache: List<Stazione>? = null
    @Volatile private var rifornimentiCache: List<Rifornimento>? = null
    @Volatile private var proprieCache: List<Tessera>? = null
    @Volatile private var tessereCache: List<Tessera>? = null
    @Volatile private var beneficiarioCache: Beneficiario? = null
    @Volatile private var comunicazioniCache: List<Comunicazione>? = null

    // Svuota le cache: chiamato all'apertura/resume dell'app per dati freschi.
    fun invalidate() {
        stazioniCache = null
        rifornimentiCache = null
        proprieCache = null
        tessereCache = null
        beneficiarioCache = null
        comunicazioniCache = null
    }

    private fun ensureDemo(demo: Boolean) {
        if (cacheDemo != demo) {
            invalidate()
            cacheDemo = demo
        }
    }

    // Valori in cache per evitare lo skeleton quando si torna su una schermata.
    fun cachedStazioni(): List<Stazione>? = stazioniCache
    fun cachedRifornimenti(): List<Rifornimento>? = rifornimentiCache
    // Ultima lista tessere gia' ordinata/personalizzata: evita lo skeleton (e il
    // reset del pager) quando cambia una personalizzazione.
    fun cachedTessere(): List<Tessera>? = tessereCache

    suspend fun beneficiario(demo: Boolean): Beneficiario = withContext(Dispatchers.IO) {
        ensureDemo(demo)
        beneficiarioCache?.let { return@withContext it }
        val b = if (demo) {
            DemoData.beneficiario()
        } else {
            val cf = auth.codiceFiscale() ?: error("Sessione scaduta, accedi di nuovo")
            val base = api.beneficiari("codiceFiscale:$cf").beneficiari.firstOrNull()?.toDomain()
                ?: error("Nessun beneficiario per questo profilo")
            // L'anagrafica beneficiari spesso non porta email/telefono: li si prende
            // dagli attributi SPID nel token.
            base.copy(
                email = base.email.ifBlank { auth.email().orEmpty() },
                telefono = base.telefono.ifBlank { auth.telefono().orEmpty() },
            )
        }
        b.also { beneficiarioCache = it }
    }

    suspend fun tessere(demo: Boolean): List<Tessera> = withContext(Dispatchers.IO) {
        ensureDemo(demo)
        val proprie = proprieCache ?: run {
            val list = if (demo) {
                DemoData.tessere()
            } else {
                try {
                    val cf = auth.codiceFiscale() ?: error("Sessione non disponibile")
                    val b = api.beneficiari("codiceFiscale:$cf").beneficiari.firstOrNull()
                        ?: error("Nessun beneficiario")
                    val nome = "${b.nome.orEmpty()} ${b.cognome.orEmpty()}".trim()
                    val validUntil = LocalDate.now().toEpochDay() + AppConfig.QRCODE_DURATION_DAYS
                    val fetched = api.domande("idBeneficiario:${b.id}").domande
                        .filter { !it.payload.isNullOrBlank() }
                        .map { d ->
                            Tessera(
                                id = d.id ?: d.targa.orEmpty(),
                                intestatario = nome,
                                targa = d.targa.orEmpty(),
                                qrPayload = d.payload!!,
                                validoFinoAlEpochDay = validUntil,
                                carburante = Carburante.fromName(d.descrizioneTipoCarburante),
                            )
                        }
                    if (fetched.isNotEmpty()) {
                        tesseraCacheStore.save(
                            fetched.map { CachedTessera(it.targa, it.intestatario, it.qrPayload, it.validoFinoAlEpochDay, it.carburante?.name) },
                        )
                    }
                    fetched
                } catch (e: Throwable) {
                    // Rete assente o sessione non rinnovabile: l'ultima copia salvata
                    // del QR (valido ~10 giorni) deve restare mostrabile alla pompa.
                    val cached = tesseraCacheStore.load()
                    if (cached.isNotEmpty()) {
                        cached.map {
                            Tessera(
                                id = it.targa,
                                intestatario = it.intestatario,
                                targa = it.targa,
                                qrPayload = it.payload,
                                validoFinoAlEpochDay = it.validUntil,
                                carburante = it.carburante?.let { n -> Carburante.fromName(n) },
                            )
                        }
                    } else {
                        throw e
                    }
                }
            }
            proprieCache = list
            list
        }
        applyOrderAndCustomizations(proprie + condiviseAsTessere()).also { tessereCache = it }
    }

    private suspend fun condiviseAsTessere(): List<Tessera> {
        val fallback = LocalDate.now().toEpochDay() + AppConfig.QRCODE_DURATION_DAYS
        return condiviseStore.list().map { c ->
            Tessera(
                id = "cond_${c.targa}",
                intestatario = "Auto condivisa",
                targa = c.targa,
                qrPayload = c.payload,
                validoFinoAlEpochDay = c.endEpochDay ?: fallback,
                condivisa = true,
            )
        }
    }

    private suspend fun applyOrderAndCustomizations(all: List<Tessera>): List<Tessera> {
        val settings = tessereStore.get()
        return all
            .sortedBy { val i = settings.order.indexOf(it.targa); if (i < 0) Int.MAX_VALUE else i }
            .map {
                it.copy(
                    etichetta = settings.names[it.targa],
                    colore = settings.colors[it.targa],
                    carburante = settings.fuels[it.targa]?.let { f -> Carburante.fromName(f) } ?: it.carburante,
                )
            }
    }

    fun tessereSettingsFlow() = tessereStore.flow()

    suspend fun setTessereOrder(order: List<String>) = tessereStore.setOrder(order)

    suspend fun setTessereName(targa: String, name: String) = tessereStore.setName(targa, name)

    suspend fun setTessereColor(targa: String, hex: String?) = tessereStore.setColor(targa, hex)

    suspend fun setTessereFuel(targa: String, fuel: String?) = tessereStore.setFuel(targa, fuel)

    suspend fun rifornimenti(demo: Boolean): List<Rifornimento> = withContext(Dispatchers.IO) {
        ensureDemo(demo)
        rifornimentiCache?.let { return@withContext it }
        val list = if (demo) {
            DemoData.rifornimenti()
        } else {
            val cf = auth.codiceFiscale() ?: error("Sessione scaduta, accedi di nuovo")
            val b = api.beneficiari("codiceFiscale:$cf").beneficiari.firstOrNull()
                ?: error("Nessun beneficiario")
            val domande = api.domande("idBeneficiario:${b.id}").domande
            val all = mutableListOf<RifornimentoDto>()
            for (d in domande) {
                val id = d.id ?: continue
                all += api.rifornimenti("idDomanda:$id").rifornimenti
            }
            all.sortedByDescending { it.dataRifornimento ?: "" }
                .mapIndexed { i, r -> r.toDomain(i) }
        }
        list.also { rifornimentiCache = it }
    }

    suspend fun stazioni(demo: Boolean): List<Stazione> = withContext(Dispatchers.IO) {
        ensureDemo(demo)
        stazioniCache?.let { return@withContext it }
        val list = if (demo) {
            DemoData.stazioni()
        } else {
            api.puntiVendita().puntiVendita.mapIndexed { i, p -> p.toDomain(i) }
        }
        list.also { stazioniCache = it }
    }

    fun stazioneById(id: String): Stazione? = stazioniCache?.firstOrNull { it.id == id }

    // ---- distributori preferiti ----
    // Cache in memoria dell'ultimo set: cosi' la lista mostra subito i preferiti
    // (valore iniziale del collectAsState), senza inserirli dopo e spostare lo scroll.
    @Volatile private var favoritesCache: Set<String> = emptySet()
    fun cachedStationFavorites(): Set<String> = favoritesCache
    fun stationFavoritesFlow(): Flow<Set<String>> = stationFavoritesStore.flow().onEach { favoritesCache = it }

    suspend fun toggleStationFavorite(key: String) = stationFavoritesStore.toggle(key)

    suspend fun rifornimentiPerStazione(demo: Boolean, insegna: String, comune: String): List<Rifornimento> =
        rifornimenti(demo).filter { it.stazione.equals(insegna, ignoreCase = true) && it.comune.equals(comune, ignoreCase = true) }

    // Versione sincrona dalla cache (se gia' precaricata): il dettaglio mostra subito
    // il bottone "I tuoi acquisti qui" senza attendere la rete.
    fun cachedRifornimentiPerStazione(insegna: String, comune: String): List<Rifornimento> =
        rifornimentiCache.orEmpty().filter { it.stazione.equals(insegna, ignoreCase = true) && it.comune.equals(comune, ignoreCase = true) }

    suspend fun cheapestStation(
        demo: Boolean,
        carburante: Carburante?,
        radiusKm: Double,
        from: LatLon,
    ): Stazione? = withContext(Dispatchers.IO) {
        val within = stazioni(demo)
            .filter { it.lat != 0.0 && it.lon != 0.0 }
            .filter { Geo.distanceKm(from.lat, from.lon, it.lat, it.lon) <= radiusKm }
            .filter { priceFor(it, carburante) != null }
        if (within.isEmpty()) return@withContext null
        val nowSec = System.currentTimeMillis() / 1000
        val recentCutoff = nowSec - 7 * 86_400
        val online = within.filter { it.online }.ifEmpty { within }
        val recent = online.filter { it.aggiornamentoEpoch >= recentCutoff }.ifEmpty { online }
        // Il piu' economico e, a parita' di prezzo (al centesimo), il piu' vicino.
        recent.minWithOrNull(
            compareBy<Stazione> { Math.round((priceFor(it, carburante) ?: Double.MAX_VALUE) * 100) }
                .thenBy { Geo.distanceKm(from.lat, from.lon, it.lat, it.lon) },
        )
    }

    // Carburante piu' usato per una targa, dedotto dallo storico rifornimenti.
    // Targa normalizzata (senza spazi, maiuscola) perche' i formati differiscono.
    suspend fun carburantePerTarga(demo: Boolean, targa: String): Carburante? = withContext(Dispatchers.IO) {
        val norm = targa.filter { !it.isWhitespace() }.uppercase()
        rifornimenti(demo)
            .filter { it.targa.filter { c -> !c.isWhitespace() }.uppercase() == norm }
            .groupingBy { it.carburante }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    private fun priceFor(s: Stazione, carb: Carburante?): Double? =
        if (carb != null) {
            s.prezzoDi(carb)?.prezzo
        } else {
            listOfNotNull(s.prezzoDi(Carburante.VERDE)?.prezzo, s.prezzoDi(Carburante.GASOLIO)?.prezzo).minOrNull()
        }

    suspend fun comunicazioni(demo: Boolean): List<Comunicazione> = withContext(Dispatchers.IO) {
        ensureDemo(demo)
        comunicazioniCache?.let { return@withContext it }
        val list = if (demo) {
            DemoData.comunicazioni()
        } else {
            val cf = auth.codiceFiscale() ?: error("Sessione scaduta, accedi di nuovo")
            val b = api.beneficiari("codiceFiscale:$cf").beneficiari.firstOrNull()
                ?: error("Nessun beneficiario")
            val bid = b.id ?: return@withContext emptyList()
            api.comunicazioni(bid, "soloCorrenti:false").comunicazioni
                .mapIndexed { i, c -> c.toDomain(i, corrente = true) }
        }
        list.also { comunicazioniCache = it }
    }

    // ---- tessere condivise (locali) ----

    fun condiviseFlow(): Flow<List<CondivisaEntry>> = condiviseStore.flow()

    suspend fun importCondivisa(payload: String): ImportResult = withContext(Dispatchers.IO) {
        val decoded = runCatching { Hc1Decoder.decode(payload) }.getOrElse {
            return@withContext ImportResult.Error("QR non valido o non decodificabile")
        }
        val targa = decoded.targa
            ?: return@withContext ImportResult.Error("Targa non trovata nel QR")
        val verify = Hc1Decoder.verify(decoded)
        if (verify == false) return@withContext ImportResult.InvalidSignature
        condiviseStore.upsert(
            CondivisaEntry(
                targa = targa,
                id = decoded.id,
                payload = payload.trim(),
                endEpochDay = decoded.endEpochDay(),
                verificata = verify == true,
            ),
        )
        proprieCache = null // la lista tessere include le condivise
        ImportResult.Success(targa, verify == true)
    }

    suspend fun removeCondivisa(targa: String) {
        condiviseStore.remove(targa)
        proprieCache = null
    }
}
