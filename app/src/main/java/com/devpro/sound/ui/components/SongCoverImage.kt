package com.devpro.sound.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.devpro.sound.R

@Composable
fun SongCoverImage(
    coverUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackCover = painterResource(id = R.drawable.ic_launcher_background)

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverUrl)
            .memoryCacheKey(coverUrl)
            .diskCacheKey(coverUrl)
            .build(),
        contentDescription = contentDescription,
        placeholder = fallbackCover,
        error = fallbackCover,
        contentScale = contentScale,
        modifier = modifier
    )
}
