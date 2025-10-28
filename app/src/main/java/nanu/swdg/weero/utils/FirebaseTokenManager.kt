package nanu.swdg.weero.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseTokenManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseTokenManager"
    }

    private val secureStorage = SecureStorage(context)

    /**
     * 저장된 FCM 토큰을 반환하거나, 없으면 새로 발급받습니다.
     * 동기 방식이지만 저장된 토큰이 있으면 즉시 반환됩니다.
     */
    fun getToken(): String? {
        // 먼저 저장된 토큰 확인
        val savedToken = secureStorage.getFcmToken()
        if (savedToken != null) {
            Log.d(TAG, "Returning saved FCM token")
            return savedToken
        }

        // 저장된 토큰이 없으면 Firebase에서 가져오기 시도
        try {
            var token: String? = null
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    token = task.result
                    token?.let { secureStorage.saveFcmToken(it) }
                    Log.d(TAG, "New FCM token retrieved and saved")
                }
            }
            return token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            return null
        }
    }

    /**
     * 비동기로 FCM 토큰을 가져오고 저장합니다.
     * 저장된 토큰이 있어도 Firebase에서 최신 토큰을 가져옵니다.
     */
    suspend fun getTokenAsync(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                secureStorage.saveFcmToken(token)
                Log.d(TAG, "FCM token retrieved and saved (async)")
                continuation.resume(token)
            } else {
                Log.e(TAG, "Failed to get FCM token (async)", task.exception)
                continuation.resumeWithException(
                    task.exception ?: Exception("Firebase token retrieval failed")
                )
            }
        }
    }

    /**
     * FCM 토큰을 새로 발급받습니다 (기존 토큰 삭제 후 재발급)
     */
    suspend fun refreshToken(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { deleteTask ->
            if (deleteTask.isSuccessful) {
                Log.d(TAG, "Old FCM token deleted")
                // 토큰 삭제 성공 후 새 토큰 발급
                FirebaseMessaging.getInstance().token.addOnCompleteListener { getTask ->
                    if (getTask.isSuccessful) {
                        val newToken = getTask.result
                        secureStorage.saveFcmToken(newToken)
                        Log.d(TAG, "New FCM token generated and saved")
                        continuation.resume(newToken)
                    } else {
                        Log.e(TAG, "Failed to get new FCM token", getTask.exception)
                        continuation.resumeWithException(
                            getTask.exception ?: Exception("Failed to get new token")
                        )
                    }
                }
            } else {
                Log.e(TAG, "Failed to delete old FCM token", deleteTask.exception)
                continuation.resumeWithException(
                    deleteTask.exception ?: Exception("Failed to delete token")
                )
            }
        }
    }

    /**
     * 저장된 FCM 토큰을 삭제합니다 (로컬 저장소에서만 삭제)
     */
    fun deleteLocalToken() {
        secureStorage.deleteFcmToken()
        Log.d(TAG, "Local FCM token deleted")
    }

    /**
     * FCM 토큰을 완전히 삭제합니다 (Firebase 및 로컬 저장소)
     */
    suspend fun deleteToken(): Boolean = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                secureStorage.deleteFcmToken()
                Log.d(TAG, "FCM token completely deleted")
                continuation.resume(true)
            } else {
                Log.e(TAG, "Failed to delete FCM token", task.exception)
                continuation.resume(false)
            }
        }
    }
}