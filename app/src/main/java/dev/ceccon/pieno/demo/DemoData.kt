package dev.ceccon.pieno.demo

import dev.ceccon.pieno.data.model.Beneficiario
import dev.ceccon.pieno.data.model.Carburante
import dev.ceccon.pieno.data.model.Comunicazione
import dev.ceccon.pieno.data.model.Erogazione
import dev.ceccon.pieno.data.model.PrezzoCarburante
import dev.ceccon.pieno.data.model.Rifornimento
import dev.ceccon.pieno.data.model.Stazione
import dev.ceccon.pieno.data.model.Tessera
import java.time.LocalDate

object DemoData {

    private fun today(): Long = LocalDate.now().toEpochDay()

    const val SAMPLE_QR =
        "HC1:NCFH90Y50/3WUWGVLK879HJ9MN5J0XK6JC RARI3Z104N5:U/3540 LSE.FKYNK0FD/Q4VW6DM6 PIENO DEMO"

    fun beneficiario() = Beneficiario(
        nome = "Marco",
        cognome = "Ceccon",
        codiceFiscale = "CCCMRC00A00B407D",
        email = "me@marco.win",
        telefono = "3331234567",
        comune = "Udine",
    )

    fun tessere() = listOf(
        Tessera("1", "Marco Ceccon", "AA123AA", SAMPLE_QR, today() + 8, carburante = Carburante.GASOLIO),
        Tessera("2", "Bepi Frico", "BB123BB", SAMPLE_QR, today() + 8, condivisa = true, carburante = Carburante.VERDE),
    )

    private fun p(c: Carburante, e: Erogazione, v: Double) = PrezzoCarburante(c, e, v)

    private fun nowSec(): Long = System.currentTimeMillis() / 1000
    private fun hoursAgo(h: Long): Long = nowSec() - h * 3600

    fun stazioni() = listOf(
        Stazione(
            "s1", "Esso", "Via Cividale 120", "Udine", 46.0710, 13.2470,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.819), p(Carburante.GASOLIO, Erogazione.SELF, 1.729), p(Carburante.GPL, Erogazione.SERVITO, 0.719), p(Carburante.METANO, Erogazione.SERVITO, 1.399)),
            online = true, aggiornamentoEpoch = hoursAgo(5),
        ),
        Stazione(
            "s2", "Q8", "Viale Venezia 60", "Udine", 46.0530, 13.2300,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.799), p(Carburante.GASOLIO, Erogazione.SELF, 1.709)),
            online = true, aggiornamentoEpoch = hoursAgo(20),
        ),
        Stazione(
            "s3", "Eni", "SS13 Pontebbana", "Tavagnacco", 46.1250, 13.2160,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.835), p(Carburante.GASOLIO, Erogazione.SELF, 1.745), p(Carburante.METANO, Erogazione.SERVITO, 1.399)),
            online = false, aggiornamentoEpoch = hoursAgo(240),
        ),
        Stazione(
            "s4", "IP", "Via Roma 14", "Cervignano del Friuli", 45.8230, 13.3360,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.789), p(Carburante.GASOLIO, Erogazione.SELF, 1.699)),
            online = true, aggiornamentoEpoch = hoursAgo(8),
        ),
        Stazione(
            "s5", "Tamoil", "Viale Trieste 200", "Pordenone", 45.9560, 12.6600,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.809), p(Carburante.GASOLIO, Erogazione.SELF, 1.719), p(Carburante.GPL, Erogazione.SERVITO, 0.709)),
            online = true, aggiornamentoEpoch = hoursAgo(30),
        ),
        Stazione(
            "s6", "Eni", "Via Flavia 30", "Trieste", 45.6360, 13.8040,
            listOf(p(Carburante.VERDE, Erogazione.SERVITO, 1.879), p(Carburante.GASOLIO, Erogazione.SERVITO, 1.789)),
            online = false, aggiornamentoEpoch = hoursAgo(72),
        ),
        Stazione(
            "s7", "Q8", "Corso Italia 5", "Gorizia", 45.9410, 13.6220,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.815), p(Carburante.GASOLIO, Erogazione.SELF, 1.725)),
            online = true, aggiornamentoEpoch = hoursAgo(12),
        ),
        Stazione(
            "s8", "Esso", "Via Buttrio 8", "Pradamano", 46.0220, 13.2900,
            listOf(p(Carburante.VERDE, Erogazione.SELF, 1.779), p(Carburante.GASOLIO, Erogazione.SELF, 1.689), p(Carburante.GNL, Erogazione.SERVITO, 1.299)),
            online = true, aggiornamentoEpoch = hoursAgo(3),
        ),
    )

    fun rifornimenti(): List<Rifornimento> {
        val t = today()
        return listOf(
            Rifornimento("r1", t - 2, "Esso", "Udine", Carburante.GASOLIO, 42.10, 72.80, 6.31),
            Rifornimento("r2", t - 9, "Q8", "Udine", Carburante.GASOLIO, 38.40, 65.60, 5.76),
            Rifornimento("r3", t - 18, "IP", "Cervignano del Friuli", Carburante.GASOLIO, 45.00, 76.45, 6.75),
            Rifornimento("r4", t - 27, "Tamoil", "Pordenone", Carburante.VERDE, 35.20, 63.70, 5.28),
            Rifornimento("r5", t - 35, "Eni", "Tavagnacco", Carburante.GASOLIO, 40.80, 71.20, 6.12),
            Rifornimento("r6", t - 44, "Esso", "Pradamano", Carburante.GASOLIO, 44.30, 74.80, 6.65),
            Rifornimento("r7", t - 51, "Q8", "Gorizia", Carburante.VERDE, 33.10, 60.20, 4.97),
            Rifornimento("r8", t - 60, "Esso", "Udine", Carburante.GASOLIO, 41.70, 72.10, 6.26),
            Rifornimento("r9", t - 68, "IP", "Cervignano del Friuli", Carburante.GASOLIO, 46.10, 78.30, 6.92),
            Rifornimento("r10", t - 77, "Eni", "Trieste", Carburante.VERDE, 30.50, 57.30, 4.58),
            Rifornimento("r11", t - 86, "Tamoil", "Pordenone", Carburante.GASOLIO, 39.90, 68.60, 5.99),
            Rifornimento("r12", t - 95, "Esso", "Udine", Carburante.GASOLIO, 43.20, 74.10, 6.48),
        )
    }

    fun comunicazioni(): List<Comunicazione> {
        val t = today()
        return listOf(
            Comunicazione(
                "c1", "Aggiornamento sconti carburante 2026",
                "Dal mese corrente gli importi dello sconto regionale sono stati aggiornati. " +
                    "Controlla i nuovi valori al momento del rifornimento.",
                t - 4, corrente = true,
            ),
            Comunicazione(
                "c2", "Nuovi distributori convenzionati",
                "Sono stati aggiunti nuovi punti vendita convenzionati nelle province di Udine e Pordenone.",
                t - 12, corrente = true,
            ),
            Comunicazione(
                "c3", "Manutenzione programmata",
                "Il sistema sarà in manutenzione nella notte tra sabato e domenica. Il QR resta valido offline.",
                t - 40, corrente = false,
            ),
            Comunicazione(
                "c4", "Rinnovo tessera digitale",
                "Ricordati di aggiornare la tessera digitale alla scadenza per avere un QR valido.",
                t - 70, corrente = false,
            ),
        )
    }
}
