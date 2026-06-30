package com.elitec.alejotaller.feature.settigns.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import com.elitec.alejotaller.feature.settigns.presentation.viewmodel.SettingsViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.GlobalPreview
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode
import org.koin.androidx.compose.koinViewModel

private const val CHECK_DARK_THEME_TAG = "DARK_THEME_CHECK"
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    asDetailPane: Boolean = false,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val settings = settingsViewModel.settings.collectAsStateWithLifecycle()

    SettingsScreenContent(
        settings = settings.value,
        onDarkModeChange = settingsViewModel::updateDarkMode,
        onNotificationsChange = settingsViewModel::updateNotifications,
        onHapticsChange = settingsViewModel::updateHapticFeedback,
        asDetailPanel = asDetailPane,
        modifier = modifier
    )
}

@Composable
private fun SettingsScreenContent(
    settings: AppSettings,
    onDarkModeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    asDetailPanel: Boolean,
    modifier: Modifier = Modifier
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode()

    when(deviceMode) {
        AppWindowType.MobilePortrait -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = "Ajustes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Estos cambios se guardan automáticamente en caché local.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SettingItem(
                    title = "Tema oscuro",
                    description = "Controla el tema general de toda la app.",
                    icon = Icons.Default.DarkMode,
                    checked = settings.darkMode,
                    onCheckedChange = onDarkModeChange
                )

                SettingItem(
                    title = "Notificaciones",
                    description = "Recibe alertas sobre pedidos y novedades.",
                    icon = Icons.Default.Notifications,
                    checked = settings.notificationsEnabled,
                    onCheckedChange = onNotificationsChange
                )

                SettingItem(
                    title = "Vibración",
                    description = "Activa respuesta háptica en interacciones.",
                    icon = Icons.Default.Vibration,
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = onHapticsChange
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Apariencia",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "El tema se aplica de forma global desde este panel.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(text = "Privacidad", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tus datos locales de ajustes se guardan en caché segura.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        AppWindowType.MobileLandscape,
        AppWindowType.TabletPortrait,
        AppWindowType.Laptop -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = "Ajustes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Estos cambios se guardan automáticamente en caché local.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        SettingItem(
                            title = "Tema oscuro",
                            description = "Controla el tema general de toda la app.",
                            icon = Icons.Default.DarkMode,
                            checked = settings.darkMode,
                            onCheckedChange = onDarkModeChange
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        SettingItem(
                            title = "Notificaciones",
                            description = "Recibe alertas sobre pedidos y novedades.",
                            icon = Icons.Default.Notifications,
                            checked = settings.notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        SettingItem(
                            title = "Vibración",
                            description = "Activa respuesta háptica en interacciones.",
                            icon = Icons.Default.Vibration,
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = onHapticsChange
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Apariencia",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Text(
                                    text = "El tema se aplica de forma global desde este panel.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(text = "Privacidad", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Tus datos locales de ajustes se guardan en caché segura.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        AppWindowType.Expanded,
        AppWindowType.TabletLandscape,
        AppWindowType.DesktopVertical-> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            color = MaterialTheme.colorScheme.onBackground,
                            text = "Ajustes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Estos cambios se guardan automáticamente en caché local.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            SettingItem(
                                title = "Tema oscuro",
                                description = "Controla el tema general de toda la app.",
                                icon = Icons.Default.DarkMode,
                                checked = settings.darkMode,
                                onCheckedChange = onDarkModeChange
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            SettingItem(
                                title = "Notificaciones",
                                description = "Recibe alertas sobre pedidos y novedades.",
                                icon = Icons.Default.Notifications,
                                checked = settings.notificationsEnabled,
                                onCheckedChange = onNotificationsChange
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        SettingItem(
                            title = "Vibración",
                            description = "Activa respuesta háptica en interacciones.",
                            icon = Icons.Default.Vibration,
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = onHapticsChange
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Apariencia",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Text(
                                        text = "El tema se aplica de forma global desde este panel.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.9f
                                        )
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Privacidad",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Tus datos locales de ajustes se guardan en caché segura.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Composable
fun SettingsScreenPreview() {
    val settingsValues = AppSettings(
        darkMode = isSystemInDarkTheme(),
        notificationsEnabled = true,
        hapticFeedbackEnabled = true
    )
    GlobalPreview {
        SettingsScreenContent(
            settings = settingsValues,
            onDarkModeChange = {},
            onNotificationsChange = {},
            onHapticsChange = { },
            asDetailPanel = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

