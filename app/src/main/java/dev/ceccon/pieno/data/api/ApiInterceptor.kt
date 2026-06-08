package dev.ceccon.pieno.data.api

import dev.ceccon.pieno.core.AppConfig
import okhttp3.Interceptor
import okhttp3.Response

// Inietta gli header dell'app ufficiale, il Bearer e X-USER-AUTHORIZATION
// (lo stesso token), tranne su punti-vendita-prezzi.
class ApiInterceptor(private val accessToken: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Accept-Language", "it")
            .header("X-BENZAPP-OS", AppConfig.OS)
            .header("X-BENZAPP-OSVersion", AppConfig.OS_VERSION)
            .header("X-BENZAPP-App", AppConfig.APP_NAME)
            .header("X-BENZAPP-AppVersion", AppConfig.APP_VERSION)

        val token = accessToken()
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
            if (!chain.request().url.encodedPath.contains("punti-vendita-prezzi")) {
                builder.header("X-USER-AUTHORIZATION", token)
            }
        }
        return chain.proceed(builder.build())
    }
}
