package nanu.swdg.weero

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import nanu.swdg.weero.utils.FirebaseTokenManager
import nanu.swdg.weero.utils.SecureStorage

class WebAppInterface(
    private val context: Context,
    private val webView: WebView
) {
    private val secureStorage = SecureStorage(context)
    private val firebaseTokenManager = FirebaseTokenManager()

    @JavascriptInterface
    fun saveRefreshToken(token: String) {
        secureStorage.saveRefreshToken(token)
    }

    @JavascriptInterface
    fun getRefreshToken(): String {
        return secureStorage.getRefreshToken() ?: ""
    }

    @JavascriptInterface
    fun getFirebaseToken(): String {
        return firebaseTokenManager.getToken() ?: ""
    }
}