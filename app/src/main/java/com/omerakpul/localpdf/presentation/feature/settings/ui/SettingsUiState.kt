package com.omerakpul.localpdf.presentation.feature.settings.ui

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false
)

enum class ThemeMode(val key: String, val displayName: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromKey(key: String): ThemeMode =
            entries.find { it.key == key } ?: SYSTEM
    }
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    TURKISH("tr", "Türkçe");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: ENGLISH
    }
}
