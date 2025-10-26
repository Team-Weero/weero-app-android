package nanu.swdg.weero.utils

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseTokenManager {

    private var cachedToken: String? = null

    fun getToken(): String? {
        if (cachedToken != null) {
            return cachedToken
        }

        try {
            var token: String? = null
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    token = task.result
                    cachedToken = token
                }
            }
            return token
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getTokenAsync(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cachedToken = task.result
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: Exception("Firebase token retrieval failed")
                )
            }
        }
    }
}