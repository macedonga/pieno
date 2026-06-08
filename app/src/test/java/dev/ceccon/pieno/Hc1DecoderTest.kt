package dev.ceccon.pieno

import dev.ceccon.pieno.data.qr.Hc1Decoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

// Valida il porting Kotlin di decode_qr.py su un payload HC1 reale.
class Hc1DecoderTest {

    private val payload =
        "HC1:NCFH90:10FFWTWGSLKC 4369Q\$KQQI J3B/MJ7B0XK6JC/RARI3Z104N5:UJ824RI3100H4JZOEE34Y50.FKYNK0FD/Q4VW6DM6I-C+/6XX6  C7W5H%69%6:DC2:6CW5G%6+CC+CCLA7A562VCK9E4N8F462X6P69C880HIUFS75VGUIB%I%9M//I *NOAVO%E%.C/SUWO04N4XO50VKGPM-6V2+E BJJN7*PLZ6EW25*\$7C28FCG8-M.QNKAHC9K5*2KMJR5"

    @Test
    fun decodes_payload() {
        val d = Hc1Decoder.decode(payload)
        println("targa=${d.targa} id=${d.id} endSeconds=${d.endSeconds} issueSeconds=${d.issueSeconds}")
        assertNotNull("targa estratta", d.targa)
    }

    @Test
    fun verifies_signature() {
        val d = Hc1Decoder.decode(payload)
        val v = Hc1Decoder.verify(d)
        println("verify=$v")
        assertEquals(true, v)
    }
}
