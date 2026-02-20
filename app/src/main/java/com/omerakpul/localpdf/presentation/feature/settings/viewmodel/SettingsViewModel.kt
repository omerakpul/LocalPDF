package com.omerakpul.localpdf.presentation.feature.settings.viewmodel

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.lifecycle.ViewModel
import com.omerakpul.localpdf.data.preferences.PreferencesManager
import com.omerakpul.localpdf.presentation.feature.settings.ui.AppLanguage
import com.omerakpul.localpdf.presentation.feature.settings.ui.SettingsUiState
import com.omerakpul.localpdf.presentation.feature.settings.ui.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            themeMode = ThemeMode.fromKey(preferencesManager.themeMode),
            language = AppLanguage.fromCode(preferencesManager.language)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun showThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = true) }
    }

    fun hideThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.themeMode = mode.key
        _uiState.update { it.copy(themeMode = mode, showThemeDialog = false) }
    }

    fun showLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun hideLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun setLanguage(language: AppLanguage) {
        preferencesManager.language = language.code
        _uiState.update { it.copy(language = language, showLanguageDialog = false) }

        // Apply locale change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(language.code)
        } else {
            // For pre-Android 13: update configuration and restart
            val locale = Locale(language.code)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            // Restart the app to apply language change
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
