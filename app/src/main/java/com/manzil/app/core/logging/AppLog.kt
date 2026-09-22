package com.manzil.app.core.logging

import android.util.Log

/**
 * Thin logging wrapper. Debug builds log to logcat; release builds stay quiet.
 * Secrets are never passed here — the API key is never logged anywhere.
 */
object AppLog {
    private const val ENABLED = true

    fun d(tag: String, message: String) {
        if (ENABLED) runCatching { Log.d("Manzil/$tag", message) }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (ENABLED) runCatching { Log.e("Manzil/$tag", message, throwable) }
    }
}
