package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BackgroundMediaType

@Composable
fun TvBackgroundMediaPlayer(
    mediaType: BackgroundMediaType,
    customUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val streamEmbedUrl = when (mediaType) {
        BackgroundMediaType.LOFI_BARBER_BEATS ->
            "https://www.youtube.com/embed/jfKfPfyJRdk?autoplay=1&mute=0&controls=0&loop=1&playlist=jfKfPfyJRdk&playsinline=1"
        BackgroundMediaType.REGGAETON_HITS ->
            "https://www.youtube.com/embed/5qap5aO4i9A?autoplay=1&mute=0&controls=0&loop=1&playlist=5qap5aO4i9A&playsinline=1"
        BackgroundMediaType.POP_ENGLISH_RADIO ->
            "https://www.youtube.com/embed/2g811Eo7K8U?autoplay=1&mute=0&controls=0&loop=1&playlist=2g811Eo7K8U&playsinline=1"
        BackgroundMediaType.CUSTOM_YOUTUBE_URL -> {
            if (customUrl.isNotBlank()) {
                val videoId = extractYouTubeId(customUrl)
                if (videoId != null) {
                    "https://www.youtube.com/embed/$videoId?autoplay=1&mute=0&controls=0&loop=1&playlist=$videoId&playsinline=1"
                } else {
                    customUrl
                }
            } else {
                "https://www.youtube.com/embed/jfKfPfyJRdk?autoplay=1&mute=0&controls=0&loop=1&playlist=jfKfPfyJRdk&playsinline=1"
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (isPlaying) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {}
                        loadUrl(streamEmbedUrl)
                    }
                },
                update = { webView ->
                    webView.loadUrl(streamEmbedUrl)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun extractYouTubeId(url: String): String? {
    return try {
        if (url.contains("v=")) {
            url.substringAfter("v=").substringBefore("&")
        } else if (url.contains("youtu.be/")) {
            url.substringAfter("youtu.be/").substringBefore("?")
        } else if (url.contains("embed/")) {
            url.substringAfter("embed/").substringBefore("?")
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
