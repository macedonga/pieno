package dev.ceccon.pieno.data.model

// Modelli di dominio usati dalla UI. Il data layer (API) mappa i DTO su questi;
// la modalita' demo li popola con dati di esempio.

data class Tessera(
    val id: String,
    val intestatario: String,
    val targa: String,
    val qrPayload: String,
    val validoFinoAlEpochDay: Long,
    val condivisa: Boolean = false,
    // Nome scelto dall'utente per la tessera (es. "Panda", "Auto lavoro").
    val etichetta: String? = null,
    // Carburante dell'auto, per "trova il distributore piu' economico".
    val carburante: Carburante? = null,
    // Colore scelto per la card (ARGB hex, es. "FF0E4F3C"); null = verde brand.
    val colore: String? = null,
)

enum class Carburante(val id: Int, val label: String) {
    VERDE(2, "Verde"),
    GASOLIO(3, "Gasolio"),
    GPL(4, "GPL"),
    METANO(5, "Metano"),
    LCNG(6, "L-CNG"),
    GNL(7, "GNL");

    companion object {
        fun fromId(id: Int): Carburante? = entries.firstOrNull { it.id == id }
        fun fromName(s: String?): Carburante? {
            val up = s?.uppercase()?.trim() ?: return null
            return entries.firstOrNull { it.name == up || it.label.uppercase() == up }
        }
    }
}

enum class Erogazione(val label: String) {
    SELF("Self"),
    SERVITO("Servito"),
}

data class PrezzoCarburante(
    val carburante: Carburante,
    val erogazione: Erogazione,
    val prezzo: Double,
)

// Il valore "1.000" e' il sentinella "prezzo non impostato": va scartato, ma NON
// i prezzi bassi reali (GPL ~0.7, metano ~1.0-1.4). Quindi si scarta solo l'esatto
// 1.000 (e i non positivi), non tutto cio' che sta sotto 1.001.
fun isValidPrice(p: Double): Boolean = p > 0.001 && kotlin.math.abs(p - 1.0) >= 0.0005

// Chiave stabile di un distributore (l'id "pv<indice>" cambia tra una fetch e
// l'altra): identita' basata sui dati, per i preferiti.
fun stationKey(s: Stazione): String = "${s.insegna}|${s.indirizzo}|${s.comune}".lowercase().trim()

data class Stazione(
    val id: String,
    val insegna: String,
    val indirizzo: String,
    val comune: String,
    val lat: Double,
    val lon: Double,
    val prezzi: List<PrezzoCarburante>,
    val online: Boolean = false,
    // Epoch (secondi) dell'ultimo aggiornamento prezzi, 0 se ignoto.
    val aggiornamentoEpoch: Long = 0L,
) {
    fun prezzoDi(carburante: Carburante): PrezzoCarburante? =
        prezzi.filter { it.carburante == carburante && isValidPrice(it.prezzo) }.minByOrNull { it.prezzo }
}

data class Rifornimento(
    val id: String,
    val dataEpochDay: Long,
    val stazione: String,
    val comune: String,
    val carburante: Carburante,
    val litri: Double,
    val importo: Double,
    val sconto: Double,
    val prezzoLitro: Double = 0.0,
    val targa: String = "",
    val stato: String = "",
) {
    val prezzoLitroEffettivo: Double
        get() = if (prezzoLitro > 0.0) prezzoLitro else if (litri > 0.0) importo / litri else 0.0
}

data class Comunicazione(
    val id: String,
    val titolo: String,
    val testo: String,
    val dataEpochDay: Long,
    val corrente: Boolean,
)

data class Beneficiario(
    val nome: String,
    val cognome: String,
    val codiceFiscale: String,
    val email: String,
    val telefono: String,
    val comune: String,
)
