package com.elitec.alejotaller.feature.auth.presentation.screen

import android.widget.Space
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.auth.presentation.components.ElitecSignatureBox
import com.elitec.alejotaller.feature.auth.presentation.components.LoginFormCard
import com.elitec.alejotaller.feature.auth.presentation.components.LoginTitle
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.AuthViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.MainRoutesKey
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateTo: (MainRoutesKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode() // Screen configuration
    val colors = MaterialTheme.colorScheme
    /*var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }*/
    var contentVisible by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "login")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loginGlow"
    )

    LaunchedEffect(null) {
        contentVisible = true
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
    ) {
        when(deviceMode) {
            AppWindowType.MobilePortrait,
            AppWindowType.TabletPortrait -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 4 }
                    ) {
                        LoginTitle(
                            glowAlpha = glowAlpha,
                            colors = colors
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    LoginFormCard(
                        onNavigateTo = onNavigateTo
                    )
                    /*Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
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
                            Button(
                                onClick = {
                                    toasterViewModel.showMessage(
                                        "Autenticando usuario",
                                        ToastType.Normal,
                                        "Custom Account Charge",
                                        isInfinite = true
                                    )
                                    loginViewModel.autUser(
                                        email = email,
                                        pass = password,
                                        onUserLogIn = { userId ->
                                            toasterViewModel.dismissMessage("Custom Account Charge")
                                            toasterViewModel.showMessage(
                                                "Bienvenido",
                                                ToastType.Success
                                            )
                                            onNavigateTo(MainRoutesKey.MainHome(userId))
                                        },
                                        onFail = { error ->
                                            toasterViewModel.dismissMessage("Custom Account Charge")
                                            toasterViewModel.showMessage(
                                                "No se pudo iniciar sesión : $error",
                                                ToastType.Error
                                            )
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyLarge,
                                        text = "Entrar"
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Google icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    toasterViewModel.showMessage(
                                        "Autenticando usuario en Google",
                                        ToastType.Normal,
                                        "Google Account Charge",
                                        isInfinite = true
                                    )
                                    loginViewModel.authWithGoogle(
                                        context = context ,
                                        onUserLogIn = { userIdLogged ->
                                            toasterViewModel.dismissMessage("Google Account Charge")
                                            toasterViewModel.showMessage(
                                                "Bienvenido",
                                                ToastType.Success
                                            )
                                            onNavigateTo(MainRoutesKey.MainHome(userIdLogged))
                                        },
                                        onFail = { error ->
                                            toasterViewModel.dismissMessage("Google Account Charge")
                                            toasterViewModel.showMessage(
                                                "No se pudo iniciar sesión en Google: $error",
                                                ToastType.Error
                                            )

                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        style = MaterialTheme.typography.bodyLarge,
                                        text = "Google"
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Image(
                                        painter = painterResource(R.drawable.googleicon),
                                        contentDescription = "Google icon",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            TextButton(
                                onClick = { onNavigateTo(MainRoutesKey.Register) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "¿No tienes cuenta? Regístrate")
                            }
                        }
                    }*/
                }
                ElitecSignatureBox()
            }
            AppWindowType.MobileLandscape,
            AppWindowType.TabletLandscape-> {
                Row(
                    modifier = Modifier.fillMaxSize()
                        .padding(15.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.weight(1f)
                            .fillMaxSize()
                    ) {
                        AnimatedVisibility(
                            visible = contentVisible,
                            enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 4 }
                        ) {
                            LoginTitle(
                                glowAlpha = glowAlpha,
                                colors = colors
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        LoginFormCard(
                            isPortrait = false,
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            onNavigateTo = onNavigateTo
                        )
                    }
                }
            }
            AppWindowType.Laptop -> TODO()
            AppWindowType.DesktopVertical -> TODO()
            AppWindowType.Expanded -> TODO()
        }

    }
}

@Composable
fun AuthTextField(
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
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateTo = {},
        modifier = Modifier.fillMaxSize()
    )
}
