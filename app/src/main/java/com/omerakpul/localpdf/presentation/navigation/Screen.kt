package com.omerakpul.localpdf.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable object Splash
@Serializable object Home
@Serializable object Files
/** [uris] are PDFs to pre-select (e.g. several PDFs shared from another app). */
@Serializable data class Merge(val uris: List<String> = emptyList())
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