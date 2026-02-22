package com.elitec.alejotaller.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.auth.presentation.screen.AuthTextField
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.AuthViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.MainRoutesKey
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginFormCard(
    isPortrait: Boolean = true,
    toasterViewModel: ToasterViewModel = koinViewModel(),
    loginViewModel: AuthViewModel = koinViewModel(),
    onNavigateTo: (MainRoutesKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
            Spacer(Modifier.height(5.dp))
            when(isPortrait) {
                true -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                        Spacer(
                            Modifier.height(10.dp)
                        )
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
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                    .weight(1f)
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
                                    .weight(1f)
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
        }
    }
}