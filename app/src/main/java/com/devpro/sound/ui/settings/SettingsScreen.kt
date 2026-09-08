package com.devpro.sound.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpro.sound.data.model.User
import com.devpro.sound.ui.components.BottomNavItem
import com.devpro.sound.ui.components.SoundBottomNavigation

private val ScreenBackground = Color(0xFF101014)
private val PanelBackground = Color(0xFF1A1A22)
private val MutedText = Color(0xFF9B9BA3)
private val AccentYellow = Color(0xFFFFDF34)


@Composable
fun SettingRoute(
    viewModel: SettingsViewModel
){
    val user = viewModel.user

    if (user == null) {
        SettingsLoadingScreen()
    } else {
        SettingsScreen(
            user = user
        )
    }
}

@Composable
fun SettingsScreen(
    user: User
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            item {
                SettingsHeader()
                ProfileCard(user = user)
                SettingsSectionTitle(title = "Playback")
                SettingsRow(
                    id = "audio_quality",
                    title = "Audio quality",
                    subtitle = user.audioQuality
                )
                SettingsRow(
                    id = "wifi_only",
                    title = "Stream only on Wi-Fi",
                    subtitle = "Save mobile data",
                    checked = user.streamOnlyOnWifi
                )
                SettingsSectionTitle(title = "App")
                SettingsRow(
                    id = "dark_mode",
                    title = "Dark mode",
                    subtitle = "System default",
                    checked = user.darkModeEnabled
                )
                SettingsRow(
                    id = "cache_storage",
                    title = "Cache and storage",
                    subtitle = user.cacheSubtitle
                )
                SettingsRow(
                    id = "about",
                    title = "About Lyra",
                    subtitle = user.appVersion
                )
            }
        }
    }
}

@Composable
private fun SettingsLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading user...", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Settings", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text("Make Lyra feel yours", color = MutedText, fontSize = 16.sp)
        }
        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun ProfileCard(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(PanelBackground)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(58.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(user.accountSubtitle, color = MutedText, fontSize = 14.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MutedText, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(text = title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 24.dp, top = 30.dp, bottom = 10.dp))
}

@Composable
private fun SettingsRow(
    id: String,
    title: String,
    subtitle: String? = null,
    checked: Boolean? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PanelBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(settingsIconOf(id), contentDescription = null, tint = AccentYellow, modifier = Modifier.size(25.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            subtitle?.let { subtitle ->
                Text(subtitle, color = MutedText, fontSize = 14.sp)
            }
        }
        if (checked != null) {
            Switch(checked = checked, onCheckedChange = {})
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MutedText, modifier = Modifier.size(28.dp))
        }
    }
}

private fun settingsIconOf(id: String): ImageVector {
    return when (id) {
        "audio_quality" -> Icons.Default.AudioFile
        "wifi_only" -> Icons.Default.Wifi
        "dark_mode" -> Icons.Default.DarkMode
        "cache_storage" -> Icons.Default.Storage
        "about" -> Icons.Default.Info
        else -> Icons.Default.Settings
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            user = previewUser()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsWithBottomNavPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(user = previewUser())

            SoundBottomNavigation(
                selectedItem = BottomNavItem.Settings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 28.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
            )
        }
    }
}

private fun previewUser(): User {
    return User(
        id = "user_001",
        name = "Minh Huan",
        accountSubtitle = "Free listener account",
        audioQuality = "High quality",
        streamOnlyOnWifi = true,
        darkModeEnabled = true,
        cacheSubtitle = "Manage offline files",
        appVersion = "Version 1.0"
    )
}
