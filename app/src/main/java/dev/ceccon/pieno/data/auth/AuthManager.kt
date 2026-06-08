package dev.ceccon.pieno.data.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import dev.ceccon.pieno.core.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

// OAuth2 Authorization Code + PKCE su LoginFVG (WSO2). Porta la stessa logica
// del client Python: il redirect a custom scheme e' catturato dall'app.
class AuthManager(context: Context, private val tokenStore: TokenStore) {

    private val service = AuthorizationService(context.applicationContext)
    private val config = AuthorizationServiceConfiguration(
        Uri.parse(AppConfig.AUTH_ENDPOINT),
        Uri.parse(AppConfig.TOKEN_ENDPOINT),
    )
    private val json = Json { ignoreUnknownKeys = true }

    fun isLoggedIn(): Flow<Boolean> = tokenStore.isLoggedIn()

    fun buildAuthIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            config,
            AppConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(AppConfig.REDIRECT_URI),
        )
            .setScope(AppConfig.SCOPE)
            .setPrompt("login")
            .build()
        return service.getAuthorizationRequestIntent(request)
    }

    suspend fun handleAuthResult(data: Intent): Boolean {
        val resp = AuthorizationResponse.fromIntent(data) ?: return false
        val ex = AuthorizationException.fromIntent(data)
        val tokenResp = performTokenRequest(resp.createTokenExchangeRequest()) ?: return false
        val state = AuthState(resp, ex).apply { update(tokenResp, null) }
        tokenStore.saveState(state.jsonSerializeString())
        return state.isAuthorized
    }

    private suspend fun performTokenRequest(request: TokenRequest): TokenResponse? =
        suspendCancellableCoroutine { cont ->
            service.performTokenRequest(request) { resp, _ -> cont.resume(resp) }
        }

    // Serializza il refresh: senza questo lock, interceptor e repository possono
    // rinfrescare in parallelo lo stesso refresh token (a rotazione), invalidandolo
    // a vicenda e causando finte "sessioni scadute".
    private val refreshLock = Any()

    // Token valido, rinfrescato se serve. Bloccante: chiamare fuori dal main thread.
    fun blockingFreshToken(): String? = synchronized(refreshLock) {
        val saved = runBlocking { tokenStore.loadState() } ?: return@synchronized null
        val state = runCatching { AuthState.jsonDeserialize(saved) }.getOrNull() ?: return@synchronized null
        var token: String? = null
        val latch = CountDownLatch(1)
        state.performActionWithFreshTokens(service) { accessToken, _, _ ->
            token = accessToken
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
        // Salva sempre: AppAuth puo' aver ruotato e aggiornato il refresh token.
        runCatching { runBlocking { tokenStore.saveState(state.jsonSerializeString()) } }
        token
    }

    suspend fun codiceFiscale(): String? {
        val token = withContext(Dispatchers.IO) { blockingFreshToken() } ?: return null
        return jwtSub(token)?.substringBefore("@")
    }

    // Email/telefono dal JWT (gli attributi SPID stanno nel token, non sempre nel
    // record beneficiario). Si cerca il claim che "assomiglia" a un'email/telefono,
    // cosi' non si dipende dal nome esatto del campo (che varia tra IdP).
    suspend fun email(): String? {
        val token = withContext(Dispatchers.IO) { blockingFreshToken() } ?: return null
        // Il `sub` e' "<codiceFiscale>@carbon.super": ha la forma di un'email ma NON
        // e' l'email. Si escludono il sub e il dominio interno WSO2.
        val sub = jwtSub(token)
        val re = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        return jwtMatch(token) { it != sub && !it.endsWith("@carbon.super") && re.matches(it) }
    }

    suspend fun telefono(): String? {
        val token = withContext(Dispatchers.IO) { blockingFreshToken() } ?: return null
        // Cellulare italiano (3xx), per evitare di pescare altri numeri.
        val re = Regex("^\\+?(39)?3\\d{8,9}$")
        return jwtMatch(token) { re.matches(it.replace(" ", "")) }
    }

    private fun jwtMatch(jwt: String, predicate: (String) -> Boolean): String? = runCatching {
        val payload = jwt.split(".")[1]
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        json.parseToJsonElement(decoded).jsonObject.values.firstNotNullOfOrNull { el ->
            (el as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf(predicate)
        }
    }.getOrNull()

    private fun jwtSub(jwt: String): String? = runCatching {
        val payload = jwt.split(".")[1]
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        json.parseToJsonElement(decoded).jsonObject["sub"]?.jsonPrimitive?.content
    }.getOrNull()

    suspend fun logout() {
        tokenStore.clear()
    }
}
