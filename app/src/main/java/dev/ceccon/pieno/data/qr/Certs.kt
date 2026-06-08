package dev.ceccon.pieno.data.qr

// Certificato pubblico regionale (CN "Sistema Carburanti Agevolati FVG"),
// usato solo per VERIFICARE la firma dei QR importati. EC P-256.
object Certs {
    const val QR_CERT_PEM: String =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIBdjCCARygAwIBAgIGAYSqEmKuMAoGCCqGSM49BAMCMDgxCzAJBgNVBAYTAklU\n" +
        "MSkwJwYDVQQDDCBTaXN0ZW1hIENhcmJ1cmFudGkgQWdldm9sYXRpIEZWRzAgFw0y\n" +
        "MjExMjQxNDM3MjJaGA8yMTIyMTAzMTE0MzcyMlowODELMAkGA1UEBhMCSVQxKTAn\n" +
        "BgNVBAMMIFNpc3RlbWEgQ2FyYnVyYW50aSBBZ2V2b2xhdGkgRlZHMFkwEwYHKoZI\n" +
        "zj0CAQYIKoZIzj0DAQcDQgAE06iuHw0UGpomC69VDjYvmj6/La5SX8t3d40NRqwL\n" +
        "+YKphlqwatXaUwu4Y829m8PGiXa7JblVWvmRQ/eao1ZmnqMQMA4wDAYDVR0TAQH/\n" +
        "BAIwADAKBggqhkjOPQQDAgNIADBFAiASaYl1O0i/I1hIF+83TUzjUxnCJm4kDFA+\n" +
        "q5H81AJjwAIhANO+tdxqFKk14V4/jKYADLng4bvk8rX9fX2UaMEl9Bfj\n" +
        "-----END CERTIFICATE-----\n"
}
