package nanu.swdg.weero

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nanu.swdg.weero.utils.FirebaseTokenManager
import nanu.swdg.weero.utils.SecureStorage

class WebAppInterface(
    private val context: Context,
    private val webView: WebView
) {
    companion object {
        private const val TAG = "WebAppInterface"
    }

    private val secureStorage = SecureStorage(context)
    private val firebaseTokenManager = FirebaseTokenManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    // ========== Refresh Token 관련 ==========

    @JavascriptInterface
    fun saveRefreshToken(token: String) {
        secureStorage.saveRefreshToken(token)
        Log.d(TAG, "Refresh token saved")
    }

    @JavascriptInterface
    fun getRefreshToken(): String {
        return secureStorage.getRefreshToken() ?: ""
    }

    // ========== FCM Token 관련 ==========

    /**
     * 저장된 FCM 토큰을 반환합니다 (동기)
     */
    @JavascriptInterface
    fun getFirebaseToken(): String {
        val token = firebaseTokenManager.getToken() ?: ""
        Log.d(TAG, "Firebase token retrieved: ${if (token.isEmpty()) "empty" else "exists"}")
        return token
    }

    /**
     * Firebase에서 최신 FCM 토큰을 가져옵니다 (비동기)
     * 완료 시 JavaScript 콜백을 호출합니다
     *
     * 사용법 (JavaScript):
     * Android.refreshFirebaseToken("onTokenRefreshed");
     *
     * function onTokenRefreshed(token) {
     *   console.log("New token:", token);
     * }
     */
    @JavascriptInterface
    fun refreshFirebaseToken(callbackName: String) {
        Log.d(TAG, "Refreshing Firebase token...")
        scope.launch {
            try {
                val newToken = firebaseTokenManager.refreshToken()
                Log.d(TAG, "Firebase token refreshed successfully")
                // JavaScript 콜백 호출
                webView.post {
                    webView.evaluateJavascript("$callbackName('$newToken');", null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh Firebase token", e)
                webView.post {
                    webView.evaluateJavascript("$callbackName('');", null)
                }
            }
        }
    }

    /**
     * FCM 토큰을 완전히 삭제합니다 (비동기)
     * 완료 시 JavaScript 콜백을 호출합니다
     *
     * 사용법 (JavaScript):
     * Android.deleteFirebaseToken("onTokenDeleted");
     *
     * function onTokenDeleted(success) {
     *   console.log("Token deleted:", success);
     * }
     */
    @JavascriptInterface
    fun deleteFirebaseToken(callbackName: String) {
        Log.d(TAG, "Deleting Firebase token...")
        scope.launch {
            try {
                val success = firebaseTokenManager.deleteToken()
                Log.d(TAG, "Firebase token deletion result: $success")
                webView.post {
                    webView.evaluateJavascript("$callbackName($success);", null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete Firebase token", e)
                webView.post {
                    webView.evaluateJavascript("$callbackName(false);", null)
                }
            }
        }
    }

    /**
     * Firebase에서 최신 토큰을 비동기로 가져옵니다 (저장된 토큰 무시)
     * 완료 시 JavaScript 콜백을 호출합니다
     *
     * 사용법 (JavaScript):
     * Android.getFirebaseTokenAsync("onTokenReceived");
     *
     * function onTokenReceived(token) {
     *   console.log("Token:", token);
     * }
     */
    @JavascriptInterface
    fun getFirebaseTokenAsync(callbackName: String) {
        Log.d(TAG, "Getting Firebase token asynchronously...")
        scope.launch {
            try {
                val token = firebaseTokenManager.getTokenAsync()
                Log.d(TAG, "Firebase token retrieved successfully (async)")
                webView.post {
                    webView.evaluateJavascript("$callbackName('$token');", null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get Firebase token (async)", e)
                webView.post {
                    webView.evaluateJavascript("$callbackName('');", null)
                }
            }
        }
    }
}