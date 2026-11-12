package com.omerakpul.localpdf.presentation.navigation
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavBackStackEntry
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import com.omerakpul.localpdf.presentation.feature.viewer.screen.PdfViewerScreen

@Composable
fun LocalPdfNavHost(
    windowSizeClass: WindowWidthSizeClass,
    incomingPdfPath: String? = null,
    onIncomingPdfHandled: () -> Unit = {},
    incomingMergePaths: List<String> = emptyList(),
    onIncomingMergeHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Open a PDF received from another app ("Open with" / "Share")
    LaunchedEffect(incomingPdfPath) {
        incomingPdfPath?.let { path ->
            navController.navigate(PdfViewer(pdfPath = path))
            onIncomingPdfHandled()
        }
    }

    // Several PDFs shared from another app: open merge with them selected
    LaunchedEffect(incomingMergePaths) {
        if (incomingMergePaths.isNotEmpty()) {
            navController.navigate(
                Merge(uris = incomingMergePaths.map { android.net.Uri.fromFile(java.io.File(it)).toString() })
            )
            onIncomingMergeHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Home,
        // No screen transitions: every destination (tabs and sub-screens, forward and back)
        // appears instantly.
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // --- Splash: no enter, fade out ---
        composable<Splash>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // --- Bottom Nav Tabs: cross-fade only ---
        composable<Home> {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onOpenPdf = { path ->
                    if (path.lowercase().endsWith(".pdf")) {
                        navController.navigate(PdfViewer(pdfPath = path))
                    } else {
                        navController.navigate(Detail(pdfPath = path, sourceType = "UNKNOWN"))
                    }
                },
                onNavigateToMerge = { navController.navigate(Merge()) },
                onNavigateToSplit = { navController.navigate(Split) },
                onNavigateToCompress = { navController.navigate(Compress) },
                onNavigateToConvert = { navController.navigate(Convert) },
                onNavigateToPhotoToPdf = { navController.navigate(PhotoToPdf) },
                onNavigateToSign = { navController.navigate(Sign) },
                onNavigateToEdit = { navController.navigate(Edit) },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Settings) {
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<Files> {
            val context = androidx.compose.ui.platform.LocalContext.current
            FilesScreen(
                windowSizeClass = windowSizeClass,
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Settings) {
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenPdf = { pdfPath ->
                    navController.navigate(PdfViewer(pdfPath = pdfPath))
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

        composable<Settings> {
            SettingsScreen(
                windowSizeClass = windowSizeClass,
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLicenseDetails = { navController.navigate(LicenseDetails) }
            )
        }

        // --- Sub-screens ---
        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            PdfDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFiles = {
                    navController.navigate(Files) {
                        popUpTo(Home) { inclusive = false }
                    }
                },
                onViewPdf = {
                    navController.navigate(PdfViewer(pdfPath = detail.pdfPath))
                }
            )
        }

        composable<Merge> { backStackEntry ->
            val merge: Merge = backStackEntry.toRoute()
            MergeScreen(
                initialUris = merge.uris,
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

        composable<LicenseDetails> {
            LicenseDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<PdfViewer> { backStackEntry ->
            val viewer: PdfViewer = backStackEntry.toRoute()
            PdfViewerScreen(
                pdfPath = viewer.pdfPath,
                onBack = { navController.popBackStack() }
            )
        }
    }
}