package com.elitec.alejotallerscan.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthUiState
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.alejotallerscan.infraestructure.presentation.util.AppWindowType
import com.elitec.alejotallerscan.infraestructure.presentation.util.GlobalPreview
import com.elitec.alejotallerscan.infraestructure.presentation.util.toDeviceMode
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorLoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel: OperatorAuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.currentUser?.id) {
        if (uiState.currentUser != null) onLoginSuccess()
    }

    OperatorScreen(
        title = "Acceso operador",
        subtitle = "Solo personal de caja y recepcion. La sesion valida exige rol operator o admin."
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Entrar con cuenta del operador", style = MaterialTheme.typography.titleLarge)
                        Text("Usa el usuario/correo registrado en Appwrite y su contrasena.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usuario o correo") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contrasena") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation()
                )

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = { viewModel.login(email, password, onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 2.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Entrar al panel")
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorLoginScreenContent(
    email: String,
    password: String,
    uiState: OperatorAuthUiState,
    onPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode()

    val adaptativeModifier = when(deviceMode) {
        AppWindowType.MobilePortrait -> Modifier.fillMaxSize()
        AppWindowType.TabletPortrait,
        AppWindowType.TabletLandscape,
        AppWindowType.Laptop,
        AppWindowType.DesktopVertical,
        AppWindowType.Expanded,
        AppWindowType.MobileLandscape -> Modifier.fillMaxHeight().width(480.dp)
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OperatorScreen(
            modifier = adaptativeModifier
                .align(Alignment.Center),
            title = "Acceso operador",
            subtitle = "Solo personal de caja y recepcion. La sesion valida exige rol operator o admin."
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Entrar con cuenta del operador", style = MaterialTheme.typography.titleLarge)
                            Text("Usa el usuario/correo registrado en Appwrite y su contrasena.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Usuario o correo") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contrasena") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(vertical = 2.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Entrar al panel")
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true, device = "spec:parent=pixel_5,orientation=landscape"
)
@Composable
fun OperatorLoginScreenContentPreview() {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var uiState by remember { mutableStateOf(OperatorAuthUiState(isLoading = false )) }

    LaunchedEffect(uiState) {
        delay(2000)
        if(uiState.isLoading) uiState = uiState.copy(isLoading = false)
    }

    GlobalPreview {
        OperatorLoginScreenContent(
            email = "email@test.com",
            password = "password test",
            uiState = uiState,
            onPasswordChange = { password = it },
            onEmailChange = { email = it },
            onLoginClick = { uiState = uiState.copy(isLoading = true) },
            modifier = Modifier.fillMaxSize()
        )
    }
}