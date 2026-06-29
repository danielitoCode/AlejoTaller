package com.elitec.alejotaller.feature.auth.presentation.screen

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elitec.alejotaller.feature.auth.presentation.components.ProfilePhotoBox
import com.elitec.alejotaller.feature.auth.presentation.uiStates.ProfileUiState
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val hasChanges = name != profileName || phone != profilePhone.orEmpty() || photoUrl?.toString() != profilePhotoUrl

    LaunchedEffect(uiState.errorMessage, uiState.saveMessage) {
        when {
            uiState.errorMessage != null -> {
                snackBarHostState.showSnackbar(uiState.errorMessage!!)
                profileViewModel.clearMessages()
            }
            uiState.saveMessage != null -> {
                snackBarHostState.showSnackbar(uiState.saveMessage!!)
                profileViewModel.clearMessages()
            }
        }
    }

    ProfileScreenContent(
        profileName = name,
        profilePhone = profilePhone ?: "",
        profileEmail = profileEmail,
        profilePhotoUrl = photoUrl,
        isGoogleUser = isGoogleUser,
        uiState = uiState,
        hasChanges = hasChanges,
        snackBarHostState = snackBarHostState,
        onNameChange = { newName -> name = newName },
        onPhoneChange = { newPhone -> phone = newPhone },
        onPhotoSelected = { newPhoto -> photoUrl = newPhoto },
        onSave = {
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
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun ProfileIdentityCard(
    photoUrl: Uri?,
    isGoogleUser: Boolean,
    onPhotoSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                onSelected = onPhotoSelected,
                onFailSelected = {},
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = if (isGoogleUser) "Cuenta vinculada con Google" else "Cuenta estandar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileFormCard(
    name: String,
    phone: String,
    profileEmail: String,
    uiStateSaving: Boolean,
    hasChanges: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
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
            onValueChange = onPhoneChange,
            label = { Text("Telefono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        if (uiStateSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = onSave,
            enabled = hasChanges && !uiStateSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Guardar cambios")
        }
    }
}

@Composable
private fun ProfileScreenContent(
    profileName: String,
    profilePhone: String,
    profileEmail: String,
    profilePhotoUrl: Uri?,
    isGoogleUser: Boolean,
    uiState: ProfileUiState,
    hasChanges: Boolean,
    snackBarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val deviceMode = LocalConfiguration.current.toDeviceMode() // Screen configuration

    when(deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait-> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account Circle",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = "Mi perfil",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                ProfileIdentityCard(
                    photoUrl = profilePhotoUrl,
                    isGoogleUser = isGoogleUser,
                    onPhotoSelected = onPhotoSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                ProfileFormCard(
                    name = profileName,
                    phone = profilePhone,
                    profileEmail = profileEmail,
                    uiStateSaving = uiState.isSaving,
                    hasChanges = hasChanges,
                    onNameChange = onNameChange,
                    onPhoneChange = onPhoneChange,
                    onSave = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(shape = CircleShape, tonalElevation = 0.dp) {
                SnackbarHost(hostState = snackBarHostState)
            }
        }
        AppWindowType.MobileLandscape,
        AppWindowType.TabletLandscape,
        AppWindowType.Laptop,
        AppWindowType.DesktopVertical,
        AppWindowType.Expanded -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account Circle",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = "Mi perfil",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ProfileIdentityCard(
                        photoUrl = profilePhotoUrl,
                        isGoogleUser = isGoogleUser,
                        onPhotoSelected = onPhotoSelected,
                        modifier = Modifier.fillMaxSize().weight(0.85f)
                    )
                    ProfileFormCard(
                        name = profileName,
                        phone = profilePhone,
                        profileEmail = profileEmail,
                        uiStateSaving = uiState.isSaving,
                        hasChanges = hasChanges,
                        onNameChange = onNameChange,
                        onPhoneChange = onPhoneChange,
                        onSave = onSave,
                        modifier = Modifier.weight(1.15f)
                    )
                }


                Surface(shape = CircleShape, tonalElevation = 0.dp) {
                    SnackbarHost(hostState = snackBarHostState)
                }
            }
        }
    }

    /*BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isWideLayout = maxWidth >= 900.dp
        val contentPadding = if (isWideLayout) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account Circle",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Mi perfil",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ProfileIdentityCard(
                        photoUrl = profilePhotoUrl,
                        isGoogleUser = isGoogleUser,
                        onPhotoSelected = onPhotoSelected,
                        modifier = Modifier.weight(0.85f)
                    )
                    ProfileFormCard(
                        name = profileName,
                        phone = profilePhone,
                        profileEmail = profileEmail,
                        uiStateSaving = uiState.isSaving,
                        hasChanges = hasChanges,
                        onNameChange = onNameChange,
                        onPhoneChange = onPhoneChange,
                        onSave = onSave,
                        modifier = Modifier.weight(1.15f)
                    )
                }
            } else {
                ProfileIdentityCard(
                    photoUrl = profilePhotoUrl,
                    isGoogleUser = isGoogleUser,
                    onPhotoSelected = onPhotoSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                ProfileFormCard(
                    name = profileName,
                    phone = profilePhone,
                    profileEmail = profileEmail,
                    uiStateSaving = uiState.isSaving,
                    hasChanges = hasChanges,
                    onNameChange = onNameChange,
                    onPhoneChange = onPhoneChange,
                    onSave = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Surface(shape = CircleShape, tonalElevation = 0.dp) {
                SnackbarHost(hostState = snackBarHostState)
            }
        }
    }*/
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun ProfileScreenContentPreview() {
    val profileName = "User Test 1"
    val profilePhone = "+5455992883"
    val profilePhotoUrl = ""
    val email = "email.test@elitec.com"

    var name by rememberSaveable(profileName) { mutableStateOf(profileName) }
    var phone by rememberSaveable(profilePhone) { mutableStateOf(profilePhone) }
    var photoUrl by rememberSaveable { mutableStateOf("") }

    var uiState = rememberSaveable { ProfileUiState(isSaving = false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val hasChanges = name != profileName || phone != profilePhone.orEmpty() || photoUrl!= profilePhotoUrl

    val onEditProfile = {
        uiState = ProfileUiState(isSaving = false, saveMessage = "Save OK")
    }

    AlejoTallerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreenContent(
                profileName = name,
                profilePhone = phone,
                profileEmail = email,
                profilePhotoUrl = photoUrl.toUri(),
                isGoogleUser = false,
                uiState = uiState,
                hasChanges = hasChanges,
                snackBarHostState = snackBarHostState,
                onNameChange = { newName -> name = newName },
                onPhoneChange ={ newPhone -> phone = newPhone },
                onPhotoSelected = { newPhoto -> photoUrl = newPhoto.toString() },
                onSave = {
                    scope.launch {
                        uiState = ProfileUiState(isSaving = true)
                        //delay(2000)
                        uiState = ProfileUiState(isSaving = false , errorMessage = "Error")
                        //delay(2000)
                        onEditProfile()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
