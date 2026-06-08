package dev.ceccon.pieno.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoint fedeli al client Python. I filtri sono nello stile q=campo:valore.
interface PienoApi {
    @GET("api/beneficiari")
    suspend fun beneficiari(@Query("q") q: String): BeneficiariResp

    @GET("api/domande")
    suspend fun domande(@Query("q") q: String): DomandeResp

    @GET("api/rifornimenti")
    suspend fun rifornimenti(@Query("q") q: String): RifornimentiResp

    // Nota: per punti-vendita-prezzi NON va inviato X-USER-AUTHORIZATION.
    @GET("api/punti-vendita-prezzi")
    suspend fun puntiVendita(): PuntiVenditaResp

    @GET("api/beneficiari/{id}/comunicazioni")
    suspend fun comunicazioni(@Path("id") id: String, @Query("q") q: String): ComunicazioniResp
}
