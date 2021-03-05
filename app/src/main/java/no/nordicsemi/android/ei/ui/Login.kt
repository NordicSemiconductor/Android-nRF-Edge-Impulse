package no.nordicsemi.android.ei.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController

@Composable
fun Login(
    modifier: Modifier = Modifier,
    navigation: NavHostController,
) {
    WebPage(
        url = "https://studio.edgeimpulse.com/login",
        modifier = modifier
    )
}

@Composable
fun WebPage(
    url: String,
    modifier: Modifier = Modifier) {
    AndroidView(
        factory = { WebView(it) },
        modifier = modifier.fillMaxSize()
    ) { webView ->
        with(webView) {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }
}