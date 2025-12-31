package com.omerakpul.localpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.omerakpul.localpdf.presentation.navigation.LocalPdfNavHost
import com.omerakpul.localpdf.presentation.theme.LocalPDFTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalPDFTheme {
                LocalPdfNavHost()
            }
        }
    }
}