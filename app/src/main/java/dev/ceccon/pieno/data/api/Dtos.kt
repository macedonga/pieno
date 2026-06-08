package dev.ceccon.pieno.data.api

import dev.ceccon.pieno.data.model.Beneficiario
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Comunicazione
import dev.ceccon.pieno.data.model.Erogazione
import dev.ceccon.pieno.data.model.PrezzoCarburante
import dev.ceccon.pieno.data.model.isValidPrice
import dev.ceccon.pieno.data.model.Rifornimento
import dev.ceccon.pieno.data.model.Stazione
import kotlinx.serialization.Serializable
import java.time.LocalDate

// DTO fedeli ai nomi di campo dell'API (vedi client/benzapp.py). Tutti i campi
// nullable + ignoreUnknownKeys lato Json, per robustezza.

@Serializable
data class Money(val amount: String? = null, val currency: String? = null) {
    fun toDouble(): Double = amount?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
}

@Serializable
data class BeneficiarioDto(
    // L'API reale usa UUID stringa per gli id, non numeri.
    val id: String? = null,
    val nome: String? = null,
    val cognome: String? = null,
    val codiceFiscale: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val indirizzo: String? = null,
    val descrizioneComune: String? = null,
    val provincia: String? = null,
    val cap: String? = null,
)

@Serializable
data class BeneficiariResp(val beneficiari: List<BeneficiarioDto> = emptyList())

@Serializable
data class DomandaDto(
    val id: String? = null,
    val payload: String? = null,
    val targa: String? = null,
    val stato: String? = null,
    val descrizioneTipoCarburante: String? = null,
)

@Serializable
data class DomandeResp(val domande: List<DomandaDto> = emptyList())

@Serializable
data class PuntoVenditaRef(val marchio: String? = null, val descrizioneComune: String? = null)

@Serializable
data class RifornimentoDto(
    val dataRifornimento: String? = null,
    val stato: String? = null,
    val targa: String? = null,
    val puntoVendita: PuntoVenditaRef? = null,
    val litri: Double? = null,
    val descrizioneTipoCarburante: String? = null,
    val tipoErogazione: String? = null,
    val prezzoApplicato: Money? = null,
    val importoPagato: Money? = null,
    val contributo: Money? = null,
)

@Serializable
data class RifornimentiResp(val rifornimenti: List<RifornimentoDto> = emptyList())

@Serializable
data class PrezzoDto(
    val idTipoCarburante: Int? = null,
    val tipoProdotto: String? = null,
    val tipoErogazione: String? = null,
    val prezzo: Money? = null,
)

@Serializable
data class PuntoVenditaDto(
    val marchio: String? = null,
    val indirizzo: String? = null,
    val descrizioneComune: String? = null,
    val latitudine: Double? = null,
    val longitudine: Double? = null,
    val online: Boolean? = null,
    val ultimoAggiornamentoPrezzi: String? = null,
    val prezzi: List<PrezzoDto> = emptyList(),
)

@Serializable
data class PuntiVenditaResp(val puntiVendita: List<PuntoVenditaDto> = emptyList())

@Serializable
data class ComunicazioneDto(
    val titolo: String? = null,
    val testo: String? = null,
    val dataInizioPubblicazione: String? = null,
)

@Serializable
data class ComunicazioniResp(val comunicazioni: List<ComunicazioneDto> = emptyList())

// ---- mapping verso il dominio ----

private fun epochDay(iso: String?): Long {
    if (iso.isNullOrBlank()) return LocalDate.now().toEpochDay()
    return runCatching { LocalDate.parse(iso.take(10)).toEpochDay() }
        .getOrElse { LocalDate.now().toEpochDay() }
}

// Epoch in secondi da un timestamp ISO (con o senza orario/fuso); 0 se ignoto.
private fun epochSeconds(iso: String?): Long {
    if (iso.isNullOrBlank()) return 0L
    runCatching { return java.time.OffsetDateTime.parse(iso).toEpochSecond() }
    runCatching { return java.time.LocalDateTime.parse(iso).toEpochSecond(java.time.ZoneOffset.UTC) }
    runCatching { return LocalDate.parse(iso.take(10)).toEpochDay() * 86_400L }
    return 0L
}

private fun carburanteByName(s: String?): Carburante {
    val up = s?.uppercase()?.trim()
    return Carburante.entries.firstOrNull { it.name == up || it.label.uppercase() == up } ?: Carburante.VERDE
}

private fun erogazione(s: String?): Erogazione =
    if (s?.uppercase()?.contains("SERVE") == true || s?.uppercase()?.contains("SERVI") == true) {
        Erogazione.SERVITO
    } else {
        Erogazione.SELF
    }

fun BeneficiarioDto.toDomain() = Beneficiario(
    nome = nome.orEmpty(),
    cognome = cognome.orEmpty(),
    codiceFiscale = codiceFiscale.orEmpty(),
    email = email.orEmpty(),
    telefono = telefono.orEmpty(),
    comune = descrizioneComune.orEmpty(),
)

fun RifornimentoDto.toDomain(index: Int) = Rifornimento(
    id = "r$index",
    dataEpochDay = epochDay(dataRifornimento),
    stazione = puntoVendita?.marchio.orEmpty().ifBlank { "Distributore" },
    comune = puntoVendita?.descrizioneComune.orEmpty(),
    carburante = carburanteByName(descrizioneTipoCarburante),
    litri = litri ?: 0.0,
    importo = importoPagato?.toDouble() ?: 0.0,
    sconto = contributo?.toDouble() ?: 0.0,
    prezzoLitro = prezzoApplicato?.toDouble() ?: 0.0,
    targa = targa.orEmpty(),
    stato = stato.orEmpty(),
)

fun PuntoVenditaDto.toDomain(index: Int) = Stazione(
    id = "pv$index",
    insegna = marchio.orEmpty().ifBlank { "Distributore" },
    indirizzo = indirizzo.orEmpty(),
    comune = descrizioneComune.orEmpty(),
    lat = latitudine ?: 0.0,
    lon = longitudine ?: 0.0,
    // L'API elenca piu' fasce (BASE/SPECIAL...) per lo stesso carburante+erogazione:
    // si tiene solo il prezzo piu' basso per coppia e si scartano i sentinella 1.000.
    prezzi = prezzi
        .mapNotNull { p ->
            val carb = Carburante.fromId(p.idTipoCarburante ?: -1) ?: return@mapNotNull null
            val v = p.prezzo?.toDouble() ?: 0.0
            if (!isValidPrice(v)) return@mapNotNull null
            PrezzoCarburante(carb, erogazione(p.tipoErogazione), v)
        }
        .groupBy { it.carburante to it.erogazione }
        .map { (_, list) -> list.minBy { it.prezzo } }
        .sortedWith(compareBy({ it.carburante.id }, { it.erogazione.ordinal })),
    online = online ?: false,
    aggiornamentoEpoch = epochSeconds(ultimoAggiornamentoPrezzi),
)

fun ComunicazioneDto.toDomain(index: Int, corrente: Boolean) = Comunicazione(
    id = "c$index",
    titolo = titolo.orEmpty(),
    testo = testo.orEmpty(),
    dataEpochDay = epochDay(dataInizioPubblicazione),
    corrente = corrente,
)
