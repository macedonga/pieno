package dev.ceccon.pieno.core

// Configurazione di produzione, dal reverse engineering (vedi docs/come-funziona.md).
object AppConfig {
    const val API_BASE_URL = "https://api.regione.fvg.it/fuel-api/"

    // Endpoint OAuth (WSO2 Identity Server, LoginFVG). Base coerente con la
    // discovery: https://is.regione.fvg.it/oauth2/oidcdiscovery/.well-known/openid-configuration
    const val AUTH_ENDPOINT = "https://is.regione.fvg.it/oauth2/authorize"
    const val TOKEN_ENDPOINT = "https://is.regione.fvg.it/oauth2/token"

    const val CLIENT_ID = "ptYKj1tU3VhEBMGF6Wqz_QttpaEa"
    const val REDIRECT_URI = "it.insiel.benzapp.cittadino:/oauth2redirect"
    const val SCOPE = "openid"

    // Header dell'app ufficiale, replicati.
    const val APP_NAME = "QRfvg Carburanti"
    const val APP_VERSION = "1.0.16"
    const val OS = "Android"
    const val OS_VERSION = "15, SDK 35"

    // Validita' della tessera digitale (qrcode_pos_duration_in_days).
    const val QRCODE_DURATION_DAYS = 10
}
