package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    phone: String? = null,
    onVerifyEmail: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ListItem(
                    headlineContent = { Text(text = name) },
                    supportingContent = { Text(text = "Nombre completo") },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                ListItem(
                    headlineContent = { Text(text = email) },
                    supportingContent = { Text(text = "Correo") },
                    leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                )
                phone?.let {
                    ListItem(
                        headlineContent = { Text(text = it) },
                        supportingContent = { Text(text = "Teléfono") },
                        leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Editar")
                    }
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Salir")
                    }
                }
                if (onVerifyEmail != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onVerifyEmail,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Verificar correo")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AlejoTallerTheme {
        ProfileScreen(
            name = "Juan Pérez",
            email = "juan.perez@example.com",
            phone = "+57 300 123 4567",
            onEditProfile = {},
            onLogout = {},
            onVerifyEmail = {}
        )
    }
}