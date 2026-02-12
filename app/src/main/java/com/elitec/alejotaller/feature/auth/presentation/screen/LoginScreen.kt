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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.AuthViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.MainRoutesKey
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateTo: (MainRoutesKey) -> Unit,
    loginViewModel: AuthViewModel = koinViewModel(),
    toasterViewModel: ToasterViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAnim"
    )

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 4 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SurfaceIcon(alpha = glowAlpha)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accede con tu cuenta para continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Card(
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
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                    text = "Diseñado por:"
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                    text = "ELITEC"
                )
                Spacer(Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.CodeOff,
                    contentDescription = "Elitec logo",
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(
                modifier = Modifier.padding(bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone logo",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                    text = "+5356628793"
                )

            }
        }
    }
}

@Composable
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
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SurfaceIcon(
    alpha: Float
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha),
        shape = MaterialShapes.Cookie9Sided.toShape(),
        modifier = Modifier.size(120.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.alejoicon_clean),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateTo = {},
        modifier = Modifier.fillMaxSize()
    )
}
