package com.omerakpul.localpdf.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.omerakpul.localpdf.presentation.feature.home.screen.HomeScreen
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
            // TODO: FilesScreen()
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
            // TODO: SettingsScreen()
        }
    }
}