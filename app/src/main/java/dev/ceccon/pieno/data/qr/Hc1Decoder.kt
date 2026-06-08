package dev.ceccon.pieno.data.qr

import com.upokecenter.cbor.CBORObject
import java.io.ByteArrayOutputStream
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.Inflater

// Decodifica e verifica un QR HC1 (EUDCC/CWT), porting di client/decode_qr.py.
// Struttura: "HC1:" + Base45 + zlib + COSE_Sign1(CBOR) firma ES256 (P-256).
data class DecodedHc1(
    val id: String?,
    val targa: String?,
    val issueSeconds: Long?,
    val endSeconds: Long?,
    val protectedBytes: ByteArray,
    val payloadBytes: ByteArray,
    val signature: ByteArray,
) {
    // claim 4 (exp) e' un timestamp: a volte in millisecondi (valore enorme), a
    // volte in secondi. Si normalizza a secondi prima di passare a giorni.
    fun endEpochDay(): Long? = endSeconds?.let {
        val secs = if (it > 10_000_000_000L) it / 1000L else it
        secs / 86400L
    }
}

object Hc1Decoder {

    private const val B45 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ \$%*+-./:"

    fun decode(raw: String): DecodedHc1 {
        val body = raw.trim().let { if (it.startsWith("HC1:")) it.substring(4) else it }
        val compressed = base45Decode(body)
        val cbor = inflate(compressed)
        val root = CBORObject.DecodeFromBytes(cbor)
        val protectedBytes = root.get(0).GetByteString()
        val payloadBytes = root.get(2).GetByteString()
        val signature = root.get(3).GetByteString()

        val payload = CBORObject.DecodeFromBytes(payloadBytes)
        val hcert = payload.get(CBORObject.FromObject(-260))
        val inner = hcert?.get(CBORObject.FromObject(1))
        val id = inner?.get(CBORObject.FromObject("i"))?.AsString()
        val targa = inner?.get(CBORObject.FromObject("p"))?.AsString()?.uppercase()
        val iat = longOrNull(payload.get(CBORObject.FromObject(6)))
        val exp = longOrNull(payload.get(CBORObject.FromObject(4)))

        return DecodedHc1(id, targa, iat, exp, protectedBytes, payloadBytes, signature)
    }

    // true = firma valida, false = non valida, null = certificato/errore.
    fun verify(d: DecodedHc1, pem: String = Certs.QR_CERT_PEM): Boolean? = try {
        val cert = CertificateFactory.getInstance("X.509")
            .generateCertificate(pem.byteInputStream()) as X509Certificate
        val sigStructure = CBORObject.NewArray()
            .Add("Signature1")
            .Add(d.protectedBytes)
            .Add(ByteArray(0))
            .Add(d.payloadBytes)
            .EncodeToBytes()
        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(cert.publicKey)
        verifier.update(sigStructure)
        verifier.verify(rawToDer(d.signature))
    } catch (e: Exception) {
        null
    }

    private fun longOrNull(o: CBORObject?): Long? =
        if (o == null) null else runCatching { o.AsInt64Value() }.getOrNull()

    private fun base45Decode(input: String): ByteArray {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < input.length) {
            when (val remaining = input.length - i) {
                else -> if (remaining >= 3) {
                    val v = idx(input[i]) + idx(input[i + 1]) * 45 + idx(input[i + 2]) * 45 * 45
                    require(v in 0..0xFFFF) { "gruppo base45 fuori range" }
                    out.write(v / 256)
                    out.write(v % 256)
                    i += 3
                } else if (remaining == 2) {
                    val v = idx(input[i]) + idx(input[i + 1]) * 45
                    require(v in 0..0xFF) { "gruppo base45 fuori range" }
                    out.write(v)
                    i += 2
                } else {
                    error("lunghezza base45 non valida")
                }
            }
        }
        return out.toByteArray()
    }

    private fun idx(c: Char): Int {
        val k = B45.indexOf(c)
        require(k >= 0) { "carattere base45 non valido: $c" }
        return k
    }

    private fun inflate(data: ByteArray): ByteArray {
        for (nowrap in listOf(false, true)) {
            try {
                val inf = Inflater(nowrap)
                inf.setInput(data)
                val out = ByteArrayOutputStream()
                val buf = ByteArray(16384)
                while (!inf.finished()) {
                    val n = inf.inflate(buf)
                    if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break
                    out.write(buf, 0, n)
                }
                inf.end()
                val res = out.toByteArray()
                if (res.isNotEmpty()) return res
            } catch (_: Exception) {
            }
        }
        error("inflate fallito")
    }

    // Converte la firma COSE grezza (r||s) in DER per java.security.
    private fun rawToDer(raw: ByteArray): ByteArray {
        val half = raw.size / 2
        val r = encodeInt(raw.copyOfRange(0, half))
        val s = encodeInt(raw.copyOfRange(half, raw.size))
        val body = r + s
        val out = ByteArrayOutputStream()
        out.write(0x30)
        writeLength(out, body.size)
        out.write(body)
        return out.toByteArray()
    }

    private fun encodeInt(value: ByteArray): ByteArray {
        var v = trimLeadingZeros(value)
        if (v.isEmpty()) v = byteArrayOf(0)
        if (v[0].toInt() and 0x80 != 0) v = byteArrayOf(0) + v
        val out = ByteArrayOutputStream()
        out.write(0x02)
        out.write(v.size)
        out.write(v)
        return out.toByteArray()
    }

    private fun trimLeadingZeros(b: ByteArray): ByteArray {
        var i = 0
        while (i < b.size - 1 && b[i] == 0.toByte()) i++
        return b.copyOfRange(i, b.size)
    }

    private fun writeLength(out: ByteArrayOutputStream, len: Int) {
        if (len < 0x80) {
            out.write(len)
        } else {
            val bytes = mutableListOf<Int>()
            var n = len
            while (n > 0) { bytes.add(0, n and 0xFF); n = n ushr 8 }
            out.write(0x80 or bytes.size)
            bytes.forEach { out.write(it) }
        }
    }
}
