package com.omerakpul.localpdf.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.omerakpul.localpdf.presentation.feature.files.screen.FilesScreen
import com.omerakpul.localpdf.presentation.feature.home.screen.HomeScreen
import com.omerakpul.localpdf.presentation.feature.merge.screen.MergeScreen
import com.omerakpul.localpdf.presentation.feature.detail.screen.PdfDetailScreen
import com.omerakpul.localpdf.presentation.feature.settings.screen.LicenseDetailsScreen
import com.omerakpul.localpdf.presentation.feature.settings.screen.SettingsScreen
import com.omerakpul.localpdf.presentation.feature.splash.screen.SplashScreen

@Composable
fun LocalPdfNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                onNavigateToMerge = { navController.navigate(Merge) },
                onNavigateToSplit = { navController.navigate(Split) },
                onNavigateToCompress = { navController.navigate(Compress) },
                onNavigateToConvert = { navController.navigate(Convert) },
                onNavigateToPhotoToPdf = { navController.navigate(PhotoToPdf) },
                onNavigateToSign = { navController.navigate(Sign) },
                onNavigateToEdit = { navController.navigate(Edit) },
                onNavigateToFiles = { navController.navigate(Files) },
                onNavigateToSettings = { navController.navigate(Settings) }
            )
        }

        composable<Files> {
            val context = androidx.compose.ui.platform.LocalContext.current
            FilesScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateToSettings = { navController.navigate(Settings) },
                onOpenPdf = { pdfPath ->
                    try {
                        val file = java.io.File(pdfPath)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onSharePdf = { pdfPath ->
                    try {
                        val file = java.io.File(pdfPath)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            PdfDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { inclusive = false }
                    }
                }
            )
        }

        composable<Merge> {
            MergeScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "MERGED"))
                }
            )
        }

        composable<Split> {
            com.omerakpul.localpdf.presentation.feature.split.screen.SplitPdfScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { inclusive = false }
                    }
                }
            )
        }

        composable<Compress> {
            com.omerakpul.localpdf.presentation.feature.compress.screen.CompressPdfScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "COMPRESSED"))
                }
            )
        }

        composable<Convert> {
            com.omerakpul.localpdf.presentation.feature.convert.screen.ConvertScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "CONVERTED"))
                }
            )
        }

        composable<PhotoToPdf> {
            com.omerakpul.localpdf.presentation.feature.phototopdf.screen.PhotoToPdfScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "SCANNED"))
                }
            )
        }

        composable<Sign> {
            com.omerakpul.localpdf.presentation.feature.sign.screen.SignPdfScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "SIGNED"))
                }
            )
        }

        composable<Edit> {
            com.omerakpul.localpdf.presentation.feature.edit.screen.EditPdfScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { pdfPath ->
                    navController.navigate(Detail(pdfPath = pdfPath, sourceType = "EDITED"))
                }
            )
        }

        composable<Settings> {
            SettingsScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateToLicenseDetails = { navController.navigate(LicenseDetails) }
            )
        }

        composable<LicenseDetails> {
            LicenseDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}