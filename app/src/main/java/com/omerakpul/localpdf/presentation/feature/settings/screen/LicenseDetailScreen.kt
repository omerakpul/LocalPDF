package com.omerakpul.localpdf.presentation.feature.settings.screen

import com.omerakpul.localpdf.presentation.components.AppTopBar
import com.omerakpul.localpdf.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omerakpul.localpdf.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseDetailsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val licenseText = remember {
        context.assets.open("licenses/pdfbox_license.txt").bufferedReader().use { it.readText() }
    }

    val noticeText = remember {
        context.assets.open("licenses/pdfbox_notice.txt").bufferedReader().use { it.readText() }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.settings_licenses), onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "PDFBOX NOTICE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = noticeText,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.licenses_third_party),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            thirdPartyComponents.forEach { (name, owner) ->
                Text(
                    text = "$name — $owner",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.licenses_apache_note),
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "APACHE LICENSE 2.0",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = licenseText,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Libraries bundled in the app (all under the Apache License 2.0). */
private val thirdPartyComponents = listOf(
    "Kotlin, kotlinx.coroutines, kotlinx.serialization" to "JetBrains",
    "AndroidX, Jetpack Compose, Navigation, Room, Material Icons" to "Google",
    "Dagger / Hilt" to "Google",
    "Coil" to "Coil Contributors",
    "Apache PDFBox (Android port)" to "Apache Software Foundation, Tom Roush",
    "Apache POI" to "Apache Software Foundation",
    "shadcn-compose" to "ronjunevaldoz and contributors"
)
