package dev.ceccon.pieno.data.repo

sealed interface ImportResult {
    data class Success(val targa: String, val verified: Boolean) : ImportResult
    data object InvalidSignature : ImportResult
    data class Error(val message: String) : ImportResult
}
