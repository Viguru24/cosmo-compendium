package com.example.data.model

sealed class CookbookError(
    open val userMessage: String,
    open val technicalMessage: String? = null,
    override val cause: Throwable? = null
) : Exception(userMessage, cause) {

    data class NetworkError(
        override val userMessage: String = "Unable to connect to the network. Please check your internet connection.",
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class GeminiAiError(
        override val userMessage: String = "AI recipe processing encountered an issue. Please try again.",
        val model: String? = null,
        val statusCode: Int? = null,
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class ComfyUiError(
        override val userMessage: String = "Photo generation server error. Please ensure ComfyUI is running.",
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class DatabaseError(
        override val userMessage: String = "Failed to save or retrieve recipe data.",
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class SyncError(
        override val userMessage: String = "Cloud sync was unable to complete.",
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class ImageProcessingError(
        override val userMessage: String = "Unable to process recipe image.",
        override val technicalMessage: String? = null,
        override val cause: Throwable? = null
    ) : CookbookError(userMessage, technicalMessage, cause)

    data class ValidationError(
        override val userMessage: String,
        override val technicalMessage: String? = null
    ) : CookbookError(userMessage, technicalMessage, null)
}
