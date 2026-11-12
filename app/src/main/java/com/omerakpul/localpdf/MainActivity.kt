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
    // PDFs shared together (SEND_MULTIPLE): opened in the merge screen
    private var incomingMergePaths by mutableStateOf<List<String>>(emptyList())

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
                            onIncomingPdfHandled = { incomingPdfPath = null },
                            incomingMergePaths = incomingMergePaths,
                            onIncomingMergeHandled = { incomingMergePaths = emptyList() }
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
        val uris: List<Uri> = when (intent?.action) {
            Intent.ACTION_VIEW -> listOfNotNull(intent.data)
            Intent.ACTION_SEND -> listOfNotNull(
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            )
            Intent.ACTION_SEND_MULTIPLE ->
                IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
            else -> emptyList()
        }
        if (uris.isEmpty()) return
        val openMerge = intent?.action == Intent.ACTION_SEND_MULTIPLE && uris.size > 1

        lifecycleScope.launch {
            val paths = withContext(Dispatchers.IO) { copyAllToCache(uris) }
            when {
                paths.isEmpty() ->
                    Toast.makeText(this@MainActivity, R.string.error_cannot_open_file, Toast.LENGTH_LONG).show()
                openMerge -> incomingMergePaths = paths
                else -> incomingPdfPath = paths.first()
            }
        }
    }

    /**
     * Copies shared PDFs into the app cache so they stay readable after the sender's temporary
     * permission ends. Copies from earlier shares are removed first so they do not pile up.
     */
    private fun copyAllToCache(uris: List<Uri>): List<String> {
        val dir = File(cacheDir, "incoming").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        return uris.mapIndexedNotNull { index, uri -> copyToCache(uri, dir, index) }
    }

    private fun copyToCache(uri: Uri, dir: File, index: Int): String? {
        return try {
            val displayName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && column >= 0) cursor.getString(column) else null
            }
            val safeName = File(displayName ?: "shared.pdf").name.ifBlank { "shared.pdf" }
            val withExtension = if (safeName.endsWith(".pdf", ignoreCase = true)) safeName else "$safeName.pdf"
            var target = File(dir, withExtension)
            if (target.exists()) target = File(dir, "${index}_$withExtension")
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
