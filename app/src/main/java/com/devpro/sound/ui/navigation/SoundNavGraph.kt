package com.devpro.sound.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devpro.sound.ui.components.BottomNavItem
import com.devpro.sound.ui.components.SoundBottomNavigation
import com.devpro.sound.ui.discover.DiscoverRoute
import com.devpro.sound.ui.downloads.DownloadsRoute
import com.devpro.sound.ui.favorites.FavoritesRoute
import com.devpro.sound.ui.nowplaying.NowPlayingRoute
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import com.devpro.sound.ui.search.SearchRoute
import com.devpro.sound.ui.settings.SettingRoute
import com.devpro.sound.ui.settings.SettingsViewModel

@Composable

fun SoundNavGraph(){
    val nowPlayingViewModel: NowPlayingViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val selectedBottomNavItem = (backStackEntry?.destination?.route ?: SoundRoute.DISCOVER).toBottomNavItem()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            selectedBottomNavItem?.let { item ->
                SoundBottomNavigation(
                    selectedItem = item,
                    onItemClick = { clickedItem ->
                        navController.navigateBottomNav(clickedItem)
                    },
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp)
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SoundRoute.DISCOVER,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = SoundRoute.DISCOVER) {
                DiscoverRoute(
                    viewModel = nowPlayingViewModel,
                    onMiniPlayerClick = { navController.navigate(SoundRoute.NOW_PLAYING) }
                )
            }
            composable(route = SoundRoute.SEARCH) {
                SearchRoute(
                    viewModel = nowPlayingViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(route = SoundRoute.FAVORITES) {
                FavoritesRoute(viewModel = nowPlayingViewModel)
            }
            composable(SoundRoute.DOWNLOADS) {
                DownloadsRoute(viewModel = nowPlayingViewModel)
            }

            composable(SoundRoute.SETTINGS) {
                SettingRoute(viewModel = settingsViewModel)
            }

            composable(SoundRoute.NOW_PLAYING) {
                NowPlayingRoute(viewModel = nowPlayingViewModel)
            }
        }
    }
}

private fun String.toBottomNavItem(): BottomNavItem? {
    return when (this) {
        SoundRoute.DISCOVER -> BottomNavItem.Discover
        SoundRoute.FAVORITES -> BottomNavItem.Favorites
        SoundRoute.DOWNLOADS -> BottomNavItem.Downloads
        SoundRoute.SETTINGS -> BottomNavItem.Settings
        else -> null
    }
}

private fun NavController.navigateBottomNav(item: BottomNavItem) {
    val route = when (item) {
        BottomNavItem.Discover -> SoundRoute.DISCOVER
        BottomNavItem.Favorites -> SoundRoute.FAVORITES
        BottomNavItem.Downloads -> SoundRoute.DOWNLOADS
        BottomNavItem.Settings -> SoundRoute.SETTINGS
    }

    if (currentDestination?.route == route) return

    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}
