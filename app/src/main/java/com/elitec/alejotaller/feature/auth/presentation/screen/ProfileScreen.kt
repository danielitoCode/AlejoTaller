package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elitec.alejotaller.feature.auth.presentation.components.ProfilePhotoBox
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun ProfileScreen(
    profileName: String,
    userId: String,
    profileEmail: String,
    profilePhone: String? = null,
    profilePhotoUrl: String? = null,
    isGoogleUser: Boolean,
    onEditProfile: () -> Unit,
    navigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    onVerifyEmail: (() -> Unit)? = null,
) {
    var name by rememberSaveable(profileName) { mutableStateOf(profileName) }
    var phone by rememberSaveable(profilePhone) { mutableStateOf(profilePhone.orEmpty()) }
    var photoUrl by rememberSaveable(profilePhotoUrl) { mutableStateOf(profilePhotoUrl?.toUri()) }

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val hasChanges = name != profileName || phone != profilePhone.orEmpty() || photoUrl?.toString() != profilePhotoUrl

    LaunchedEffect(uiState.errorMessage, uiState.saveMessage) {
        when {
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage!!)
                profileViewModel.clearMessages()
            }
            uiState.saveMessage != null -> {
                snackbarHostState.showSnackbar(uiState.saveMessage!!)
                profileViewModel.clearMessages()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = navigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text("Mi perfil", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(40.dp))
        }
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfilePhotoBox(
                    photoUrl = photoUrl,
                    onSelected = { photoSelected -> photoUrl = photoSelected },
                    onFailSelected = {},
                    modifier = Modifier
                )

                Text(
                    text = if (isGoogleUser) "Cuenta vinculada con Google" else "Cuenta estándar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        OutlinedTextField(
            value = profileEmail,
            onValueChange = {},
            enabled = false,
            label = { Text("Correo") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        if (uiState.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = {
                profileViewModel.updateProfile(
                    userId = userId,
                    newName = name,
                    currentName = profileName,
                    newPhone = phone,
                    currentPhone = profilePhone.orEmpty(),
                    photoUri = photoUrl,
                    currentPhotoUrl = profilePhotoUrl,
                    context = context,
                    onSuccess = onEditProfile
                )
            },
            enabled = hasChanges && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Guardar cambios")
        }
        Surface(shape = CircleShape, tonalElevation = 0.dp) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

