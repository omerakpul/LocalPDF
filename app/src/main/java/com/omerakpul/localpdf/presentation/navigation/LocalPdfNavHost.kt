package com.omerakpul.localpdf.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.omerakpul.localpdf.presentation.feature.files.screen.FilesScreen
import com.omerakpul.localpdf.presentation.feature.home.screen.HomeScreen
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
                onNavigateToFiles = { navController.navigate(Files) },
                onNavigateToSettings = { navController.navigate(Settings) }
            )
        }

        composable<Files> {
            FilesScreen(
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToDetail = { pdfId ->
                    navController.navigate(Detail(pdfId = pdfId))
                }
            )
        }

        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            // TODO: DetailScreen(pdfId = detail.pdfId)
        }

        composable<Merge> {
            // TODO: MergeScreen()
        }

        composable<Split> {
            // TODO: SplitScreen()
        }

        composable<Compress> {
            // TODO: CompressScreen()
        }

        composable<Convert> {
            // TODO: ConvertScreen()
        }

        composable<PhotoToPdf> {
            // TODO: PhotoToPdfScreen()
        }

        composable<Sign> {
            // TODO: SignScreen()
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