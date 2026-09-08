package com.devpro.sound.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector
) {
    Discover(label = "Khám phá", icon = Icons.Default.MusicNote),
    Favorites(label = "Yêu thích", icon = Icons.Default.Favorite),
    Downloads(label = "Tải xuống", icon = Icons.Default.CloudDownload),
    Settings(label = "Cài đặt", icon = Icons.Default.Settings)
}

@Composable
fun SoundBottomNavigation(
    selectedItem: BottomNavItem,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(46.dp))
            .background(Color(0xE61B1B22))
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.entries.forEach { item ->
            BottomNavButton(
                item = item,
                selected = item == selectedItem,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) Color.White else Color(0xFF9B9BA3)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )

        Text(
            text = item.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(if (selected) 6.dp else 0.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF101014
)
@Composable
fun SoundBottomNavigationPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101014))
                .padding(24.dp)
        ) {
            SoundBottomNavigation(
                selectedItem = BottomNavItem.Discover
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF101014
)
@Composable
fun SoundBottomNavigationFavoritesPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101014))
                .padding(24.dp)
        ) {
            SoundBottomNavigation(
                selectedItem = BottomNavItem.Favorites
            )
        }
    }
}
