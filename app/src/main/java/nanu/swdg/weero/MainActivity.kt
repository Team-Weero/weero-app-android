package nanu.swdg.weero

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import nanu.swdg.weero.ui.theme.WeeroTeacherApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeeroTeacherApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewScreen(
                        url = "file:///android_asset/index.html",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        activity = this
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String, modifier: Modifier = Modifier, activity: ComponentActivity) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                }

                val webAppInterface = WebAppInterface(activity, this)
                addJavascriptInterface(webAppInterface, "Android")

                loadUrl(url)
            }
        },
        modifier = modifier
    )
}