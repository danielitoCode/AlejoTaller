package com.elitec.alejotaller.feature.auth.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.elitec.alejotaller.feature.auth.presentation.components.ProfilePhotoBox
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.navigation.koinNavViewModel

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
    onVerifyEmail: (() -> Unit)? = null
) {
    var isNameEditing by rememberSaveable { mutableStateOf(false) }
    var isPassEditing by rememberSaveable { mutableStateOf(false) }
    var isPassConfirmEditing by rememberSaveable { mutableStateOf(false) }
    var isPhoneEditing by rememberSaveable { mutableStateOf(false) }
    var isPassVisible by rememberSaveable { mutableStateOf(false) }
    var isPassConfirmVisible by rememberSaveable { mutableStateOf(false) }
    val borderColorName =  if(isNameEditing) MaterialTheme.colorScheme.onBackground else Color.Transparent
    val borderColorPhone = if(isNameEditing) MaterialTheme.colorScheme.onBackground else Color.Transparent

    var name by rememberSaveable { mutableStateOf(profileName) }
    var phone by rememberSaveable { mutableStateOf(profilePhone ?: "") }
    var photoUrl by rememberSaveable { mutableStateOf(profilePhotoUrl?.toUri()) }
    var pass by rememberSaveable { mutableStateOf("") }
    var passConfirm by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    val animateColorName = animateColorAsState(
        targetValue = borderColorName
    )
    val animateColorPhone = animateColorAsState(
        targetValue = borderColorPhone
    )

    val passEyeIcon = if(isPassVisible) Icons.Default.Close else Icons.Default.RemoveRedEye
    val passConfirmEyeIcon = if(isPassConfirmVisible) Icons.Default.Close else Icons.Default.RemoveRedEye

    val passVisualTransformation =  if(isPassVisible) VisualTransformation.None else PasswordVisualTransformation()
    val passConfirmVisualTransformation =  if(isPassVisible) VisualTransformation.None else PasswordVisualTransformation()

    LaunchedEffect(null) {

    }
    LaunchedEffect(isPassVisible) {
        if(isPassVisible) {
            delay(2000)
            isPassVisible = false
        }
    }
    LaunchedEffect(isPassConfirmVisible) {
        if(isPassConfirmVisible) {
            delay(2000)
            isPassConfirmVisible = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = navigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // Placeholder for back arrow
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        ProfilePhotoBox(
            photoUrl = photoUrl,
            onSelected = { photoSelected ->
                photoUrl = photoSelected
            },
            onFailSelected = {  },
            modifier = Modifier
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            enabled = isNameEditing,
            label = {
                Text(
                    text = "Nombre:"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                errorBorderColor = MaterialTheme.colorScheme.error,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                disabledBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.headlineSmall,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Logo"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        isNameEditing = true
                        isPhoneEditing = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp
                )
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            enabled = isPhoneEditing,
            label = {
                Text(
                    text = "Teléfono:"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                errorBorderColor = MaterialTheme.colorScheme.error,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                disabledBorderColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.headlineSmall,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone Logo"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        isNameEditing = false
                        isPhoneEditing = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !isGoogleUser
            ) {
                Button(
                    shape = RoundedCornerShape(15.dp),
                    onClick = { isPassEditing = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Key logo"
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            style = MaterialTheme.typography.titleLarge,
                            text = "Cambiar contraseña"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 5.dp))
            AnimatedVisibility(
                visible = true//isPassEditing
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(
                            top = 20.dp,
                            bottom = 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            visualTransformation = passVisualTransformation,
                            label = {
                                Text(
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    text = "Contraseña:"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onTertiary.copy(0.7f),
                                unfocusedTextColor = Color.Transparent,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onTertiary.copy(0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                            textStyle = MaterialTheme.typography.headlineSmall,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isPassVisible = true
                                    }
                                ) {
                                    AnimatedContent(
                                        targetState = passEyeIcon
                                    ) {
                                        Icon(
                                            imageVector = it,
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            contentDescription = "Edit"
                                        )
                                    }

                                }
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 20.dp,
                                    end = 20.dp
                                )
                        )
                        OutlinedTextField(
                            value = passConfirm,
                            onValueChange = { passConfirm = it },
                            visualTransformation = passConfirmVisualTransformation,
                            label = {
                                Text(
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    text = "Confirme su contraseña:"
                                )
                            },
                            isError = pass != passConfirm,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onTertiary.copy(0.7f),
                                unfocusedTextColor = Color.Transparent,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onTertiary.copy(0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                            textStyle = MaterialTheme.typography.headlineSmall,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isPassConfirmVisible = true
                                    }
                                ) {
                                    AnimatedContent(
                                        targetState = passConfirmEyeIcon
                                    ) {
                                        Icon(
                                            imageVector = it,
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            contentDescription = "Edit"
                                        )
                                    }

                                }
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 20.dp,
                                    end = 20.dp
                                )
                        )
                    }
                }
            }
        }
        Button(
            enabled = name != profileName || phone != profilePhone || photoUrl.toString() != profilePhotoUrl,
            onClick = {
                when {
                    name != profileName -> {
                        profileViewModel.updateName(
                            name,
                            onNameUpdate = {  },
                            onFail = {  }
                        )
                    }
                    photoUrl != null && photoUrl.toString() != profilePhotoUrl -> {
                        profileViewModel.updatePhotoUrl(
                            userId = userId,
                            context = context,
                            uri = photoUrl!!,
                            onPhotoUpload = {  },
                            onFail = {  }
                        )
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier.padding(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "Update profile",
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = "Actualizar perfil"
                )
            }
        }
    }
}

