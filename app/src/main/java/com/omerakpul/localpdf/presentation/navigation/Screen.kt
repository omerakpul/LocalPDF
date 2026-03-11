package com.omerakpul.localpdf.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable object Splash
@Serializable object Home
@Serializable object Files
@Serializable object Merge
@Serializable object Compress
@Serializable object Split
@Serializable object Convert
@Serializable object PhotoToPdf
@Serializable object Sign
@Serializable object Edit
@Serializable object Settings
@Serializable object LicenseDetails
@Serializable data class Detail(val pdfPath: String, val sourceType: String = "UNKNOWN")
@Serializable data class PdfViewer(val pdfPath: String)