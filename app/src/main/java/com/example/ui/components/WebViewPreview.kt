package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.LogLevel
import com.example.data.model.Project
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ViewportMode {
    RESPONSIVE, MOBILE, TABLET
}

class AndroidConsoleBridge(private val onLog: (LogLevel, String) -> Unit) {
    @JavascriptInterface
    fun log(level: String, message: String) {
        val logLevel = when (level.uppercase()) {
            "ERROR" -> LogLevel.ERROR
            "WARN" -> LogLevel.WARN
            "INFO" -> LogLevel.INFO
            else -> LogLevel.LOG
        }
        onLog(logLevel, message)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPreview(
    project: Project,
    reloadTrigger: Int,
    errorCount: Int,
    onLogReceived: (LogLevel, String) -> Unit,
    onToggleConsole: () -> Unit,
    onForceReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewportMode by remember { mutableStateOf(ViewportMode.RESPONSIVE) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val consoleBridge = remember {
        AndroidConsoleBridge { level, msg ->
            onLogReceived(level, msg)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Preview Header Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Status indicator & Live badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLoading) NeonAmber else NeonGreen)
                    )
                    Text(
                        text = if (isLoading) "RENDERING..." else "LIVE PREVIEW",
                        color = if (isLoading) NeonAmber else CyanBrand,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Middle: Viewport switchers
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ViewportButton(
                        icon = Icons.Default.DesktopWindows,
                        selected = viewportMode == ViewportMode.RESPONSIVE,
                        contentDesc = "Full Viewport",
                        onClick = { viewportMode = ViewportMode.RESPONSIVE }
                    )
                    ViewportButton(
                        icon = Icons.Default.Tablet,
                        selected = viewportMode == ViewportMode.TABLET,
                        contentDesc = "Tablet Viewport",
                        onClick = { viewportMode = ViewportMode.TABLET }
                    )
                    ViewportButton(
                        icon = Icons.Default.PhoneAndroid,
                        selected = viewportMode == ViewportMode.MOBILE,
                        contentDesc = "Mobile Viewport",
                        onClick = { viewportMode = ViewportMode.MOBILE }
                    )
                }

                // Right: Console Toggle & Reload
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Console button with error badge
                    BadgedBox(
                        badge = {
                            if (errorCount > 0) {
                                Badge(
                                    containerColor = NeonRed,
                                    contentColor = Color.White
                                ) {
                                    Text(text = errorCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onToggleConsole,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("toggle_console_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperMode,
                                contentDescription = "Toggle Console",
                                tint = if (errorCount > 0) NeonRed else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Reload Button
                    IconButton(
                        onClick = onForceReload,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("reload_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Preview",
                            tint = CyanBrand,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Web Canvas Container with Viewport Sizing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(if (viewportMode != ViewportMode.RESPONSIVE) 12.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            val viewportModifier = when (viewportMode) {
                ViewportMode.RESPONSIVE -> Modifier.fillMaxSize()
                ViewportMode.MOBILE -> Modifier
                    .width(360.dp)
                    .fillMaxSize()
                    .border(2.dp, BorderDark, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                ViewportMode.TABLET -> Modifier
                    .width(540.dp)
                    .fillMaxSize()
                    .border(2.dp, BorderDark, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            }

            Box(modifier = viewportModifier) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().testTag("webview_preview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }

                            // Add JavascriptInterface bridge for console logging
                            addJavascriptInterface(consoleBridge, "AndroidConsole")

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    if (consoleMessage != null) {
                                        val level = when (consoleMessage.messageLevel()) {
                                            ConsoleMessage.MessageLevel.ERROR -> LogLevel.ERROR
                                            ConsoleMessage.MessageLevel.WARNING -> LogLevel.WARN
                                            else -> LogLevel.LOG
                                        }
                                        onLogReceived(
                                            level,
                                            "${consoleMessage.message()} (line ${consoleMessage.lineNumber()})"
                                        )
                                    }
                                    return super.onConsoleMessage(consoleMessage)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    isLoading = false
                                    onLogReceived(
                                        LogLevel.ERROR,
                                        "Resource Error: ${error?.description}"
                                    )
                                }
                            }

                            val html = project.buildRenderHtml()
                            loadDataWithBaseURL("https://local.sandbox/", html, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        // Reload trigger causes reload of current synthesized HTML
                        val html = project.buildRenderHtml()
                        webView.loadDataWithBaseURL("https://local.sandbox/", html, "text/html", "UTF-8", null)
                    }
                )

                // Loading overlay spinner
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = CyanBrand,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    contentDesc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) CyanBrand else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = if (selected) Color(0xFF0D1117) else TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
