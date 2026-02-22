package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.RegistrationViewModel
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
    val deviceMode = LocalConfiguration.current.toDeviceMode() // Screen configuration

    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    when(deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait-> {

        }
        AppWindowType.MobileLandscape -> TODO()
        AppWindowType.TabletLandscape -> TODO()
        AppWindowType.Laptop -> TODO()
        AppWindowType.DesktopVertical -> TODO()
        AppWindowType.Expanded -> TODO()
    }
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
                    onValueChange = { name = it },
                    label = "Nombre completo",
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    visualTransformation = VisualTransformation.None
                )
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo",
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    visualTransformation = VisualTransformation.None
                )
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation()
                )
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar contraseña",
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation()
                )
                Button(
                    onClick = {
                        if(password != confirmPassword) {
                            toasterViewModel.showMessage(
                                message = "No coinciden las contrasenas",
                                type = ToastType.Warning
                            )
                        }
                        toasterViewModel.showMessage(
                            "Autenticando usuario",
                            ToastType.Normal,
                            "Custom Account Charge"
                        )
                        registerViewModel.customRegister(
                            email = email,
                            password = password,
                            name = name,
                            onUserRegister = {
                                toasterViewModel.dismissMessage("Custom Account Charge")
                                toasterViewModel.showMessage(
                                    "Bienvenido",
                                    ToastType.Success,
                                    "Custom Account Charge"
                                )
                                onRegisterReady("Usuario registrado")
                            },
                            onFail = {
                                toasterViewModel.dismissMessage("Custom Account Charge")
                                toasterViewModel.showMessage(
                                    "Error al registrar usuario",
                                    ToastType.Error,
                                    "Custom Account Charge"
                                )
                            }
                        )

                    },
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

/*@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)?,
    visualTransformation: VisualTransformation
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = visualTransformation
    )
}*/