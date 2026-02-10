package com.elitec.alejotaller.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R

@Preview
@Composable
fun ProfilePhotoBox(
    photoUrl: String = "",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profileBoxBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(0.5f),
            MaterialTheme.colorScheme.onSurface.copy(0.2f),
            MaterialTheme.colorScheme.onSurface.copy(0.5f)
        )
    )
    Surface(
        onClick = onClick,
        shadowElevation = 5.dp,
        tonalElevation = 5.dp,
        modifier = modifier.size(200.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.empty_user),
            contentDescription = "Profile photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(profileBoxBrush)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Camera",
                modifier = Modifier.fillMaxSize().padding(30.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
        }
    }
}