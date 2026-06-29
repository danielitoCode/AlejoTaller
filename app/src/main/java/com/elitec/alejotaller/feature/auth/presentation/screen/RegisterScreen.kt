package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.RegistrationViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterReady: (String) -> Unit,
    modifier: Modifier = Modifier,
    registerViewModel: RegistrationViewModel = koinViewModel(),
    toasterViewModel: ToasterViewModel = koinViewModel()
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    val normalizedEmail = email.trim().lowercase()
    val sanitizedName = name.trim()
    val sanitizedPassword = password.trim()
    val sanitizedConfirmPassword = confirmPassword.trim()

    RegisterScreenContent(
        name = name,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        submitting = submitting,
        onCreateAccountButtonClick = {
            if (submitting) return@RegisterScreenContent
            if(sanitizedPassword != sanitizedConfirmPassword) {
                toasterViewModel.showMessage(
                    message = "No coinciden las contrasenas",
                    type = ToastType.Warning
                )
                return@RegisterScreenContent
            }
            if (sanitizedName.isBlank() || normalizedEmail.isBlank() || sanitizedPassword.isBlank()) {
                toasterViewModel.showMessage(
                    message = "Completa todos los campos requeridos",
                    type = ToastType.Warning
                )
                return@RegisterScreenContent
            }
            submitting = true
            toasterViewModel.showMessage(
                "Autenticando usuario",
                ToastType.Normal,
                "Custom Account Charge"
            )
            registerViewModel.customRegister(
                email = normalizedEmail,
                password = sanitizedPassword,
                name = sanitizedName,
                onUserRegister = { userId ->
                    submitting = false
                    toasterViewModel.dismissMessage("Custom Account Charge")
                    toasterViewModel.showMessage(
                        "Bienvenido",
                        ToastType.Success,
                        "Custom Account Charge"
                    )
                    onRegisterReady(userId)
                },
                onFail = {
                    submitting = false
                    toasterViewModel.dismissMessage("Custom Account Charge")
                    toasterViewModel.showMessage(
                        "Error al registrar usuario",
                        ToastType.Error,
                        "Custom Account Charge"
                    )
                }
            )

        },
        onNameChanged = { name = it },
        onEmailChanged = { email = it },
        onPasswordChanged = { password = it },
        onPasswordConfirmChanged = { confirmPassword = it },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun RegisterScreenContent(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    submitting: Boolean,
    onCreateAccountButtonClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode() // Screen configuration

    when(deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completa tus datos para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AuthTextField(
                            value = name,
                            onValueChange = onNameChanged,
                            label = "Nombre completo",
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            visualTransformation = VisualTransformation.None
                        )
                        AuthTextField(
                            value = email,
                            onValueChange = onEmailChanged,
                            label = "Correo",
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            visualTransformation = VisualTransformation.None
                        )
                        AuthTextField(
                            value = password,
                            onValueChange = onPasswordChanged,
                            label = "Contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = onPasswordConfirmChanged,
                            label = "Confirmar contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(
                            onClick = onCreateAccountButtonClick,
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Registrarse")
                        }
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "¿Ya tienes cuenta? Inicia sesión")
                        }
                    }
                }
            }
        }
        AppWindowType.MobileLandscape -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completa tus datos para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                AuthTextField(
                                    value = name,
                                    onValueChange = onNameChanged,
                                    label = "Nombre completo",
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    visualTransformation = VisualTransformation.None
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                AuthTextField(
                                    value = email,
                                    onValueChange = onEmailChanged,
                                    label = "Correo",
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    visualTransformation = VisualTransformation.None
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                AuthTextField(
                                    value = password,
                                    onValueChange = onPasswordChanged,
                                    label = "Contraseña",
                                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                                    visualTransformation = PasswordVisualTransformation()
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                AuthTextField(
                                    value = confirmPassword,
                                    onValueChange = onPasswordConfirmChanged,
                                    label = "Confirmar contraseña",
                                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                                    visualTransformation = PasswordVisualTransformation()
                                )
                            }
                        }

                        Button(
                            onClick = onCreateAccountButtonClick,
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Registrarse")
                        }
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "¿Ya tienes cuenta? Inicia sesión")
                        }
                    }
                }
            }
        }
        AppWindowType.TabletLandscape,
        AppWindowType.Laptop,
        AppWindowType.DesktopVertical,
        AppWindowType.Expanded -> {
            Column(
                modifier = modifier
                    .width(450.dp)
                    .fillMaxHeight()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completa tus datos para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AuthTextField(
                            value = name,
                            onValueChange = onNameChanged,
                            label = "Nombre completo",
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            visualTransformation = VisualTransformation.None
                        )
                        AuthTextField(
                            value = email,
                            onValueChange = onEmailChanged,
                            label = "Correo",
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            visualTransformation = VisualTransformation.None
                        )
                        AuthTextField(
                            value = password,
                            onValueChange = onPasswordChanged,
                            label = "Contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = onPasswordConfirmChanged,
                            label = "Confirmar contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(
                            onClick = onCreateAccountButtonClick,
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Registrarse")
                        }
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "¿Ya tienes cuenta? Inicia sesión")
                        }
                    }
                }
            }
        }
    }

}

@Preview(
    showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
fun RegisterScreenContentPreview() {
    AlejoTallerTheme() {

        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var submitting by remember { mutableStateOf(false) }

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                RegisterScreenContent(
                    modifier = Modifier,
                    name = name,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    submitting = submitting,
                    onCreateAccountButtonClick = { },
                    onNameChanged = { name = it },
                    onEmailChanged = { email = it },
                    onPasswordChanged = { password = it },
                    onPasswordConfirmChanged = { confirmPassword = it },
                    onNavigateBack = { }
                )
            }
        }
    }
}
