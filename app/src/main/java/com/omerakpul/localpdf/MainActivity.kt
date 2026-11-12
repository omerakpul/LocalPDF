package com.omerakpul.localpdf

import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import com.omerakpul.localpdf.presentation.theme.BackgroundColor
import com.omerakpul.localpdf.presentation.theme.BACKGROUND_LIGHT
import com.omerakpul.localpdf.presentation.theme.BACKGROUND_DARK
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.omerakpul.localpdf.data.preferences.PreferencesManager
import com.omerakpul.localpdf.presentation.navigation.LocalPdfNavHost
import com.omerakpul.localpdf.presentation.theme.LocalPDFTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    // Path of a PDF received from another app (VIEW / SEND), consumed by the nav host
    private var incomingPdfPath by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            var themeMode by remember { mutableStateOf(preferencesManager.themeMode) }

            // Listen for preference changes to update theme reactively
            DisposableEffect(Unit) {
                val prefs = getSharedPreferences("localpdf_prefs", MODE_PRIVATE)
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "theme_mode") {
                        themeMode = preferencesManager.themeMode
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val windowSizeClass = androidx.compose.material3.windowsizeclass.calculateWindowSizeClass(this)
            
            LocalPDFTheme(darkTheme = isDarkTheme) {
                // The window itself follows the in-app theme, so system UI, the keyboard and screen
                // transitions never reveal a mismatching (e.g. white) background.
                LaunchedEffect(isDarkTheme) {
                    window.setBackgroundDrawable(
                        ColorDrawable((if (isDarkTheme) BACKGROUND_DARK else BACKGROUND_LIGHT).toInt())
                    )
                }

                // Solid layer behind the NavHost: cross-fades blend against the app colour
                Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
                    ShadcnTheme(
                        preset = ShadcnStylePreset.Vega,
                        baseColor = ShadcnBaseColor.Stone,
                        accent = ShadcnAccent.Red,
                        isDark = isDarkTheme
                    ) {
                        LocalPdfNavHost(
                            windowSizeClass = windowSizeClass.widthSizeClass,
                            incomingPdfPath = incomingPdfPath,
                            onIncomingPdfHandled = { incomingPdfPath = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri: Uri? = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            else -> null
        }
        if (uri == null) return

        lifecycleScope.launch {
            val path = withContext(Dispatchers.IO) { copyToCache(uri) }
            if (path != null) {
                incomingPdfPath = path
            } else {
                Toast.makeText(this@MainActivity, R.string.error_cannot_open_file, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Copies a shared PDF into the app cache so the in-app viewer can open it by path. */
    private fun copyToCache(uri: Uri): String? {
        return try {
            val displayName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
            }
            val safeName = File(displayName ?: "shared.pdf").name.ifBlank { "shared.pdf" }
            val dir = File(cacheDir, "incoming").apply { mkdirs() }
            // Remove copies of previously shared files so they do not pile up in the cache
            dir.listFiles()?.forEach { it.delete() }
            val target = File(dir, if (safeName.endsWith(".pdf", ignoreCase = true)) safeName else "$safeName.pdf")
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (e: Exception) {
            Log.w("MainActivity", "Could not copy shared file to cache: $uri", e)
            null
        }
    }
}