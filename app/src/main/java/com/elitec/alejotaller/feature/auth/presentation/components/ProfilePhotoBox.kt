package com.elitec.alejotaller.feature.auth.presentation.components

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.elitec.alejotaller.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfilePhotoBox(
    photoUrl: Uri? = null,
    onSelected: (Uri) -> Unit,
    onFailSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var currentImage by remember { mutableStateOf(photoUrl) }

    // ✅ Lanzador del selector de fotos (galería)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                currentImage = uri
                onSelected(uri)
            } else {
                onFailSelected()
            }
        }
    )

    // ✅ Permiso adaptativo según versión de Android
    val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionState = rememberPermissionState(readImagePermission)

    val profileBoxBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(0.5f),
            MaterialTheme.colorScheme.onSurface.copy(0.2f),
            MaterialTheme.colorScheme.onSurface.copy(0.5f)
        )
    )
    Surface(
        onClick =  {
            when {
                // Android 14+: sin permisos
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                // Permiso ya concedido
                permissionState.status.isGranted -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                else -> {
                    // Solicitar permiso manualmente
                    permissionState.launchPermissionRequest()
                }
            }
        },
        shadowElevation = 5.dp,
        tonalElevation = 5.dp,
        modifier = modifier.size(200.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentImage)
                .crossfade(true)
                .build(),
            contentDescription = null,
            onLoading = { },
            placeholder = painterResource(R.drawable.image),
            error = painterResource(R.drawable.empty_user),
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