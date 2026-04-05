package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel.OperatorReservationSearchViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorLoadingToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OperatorScanApp(
    modifier: Modifier = Modifier
) {
    val authViewModel: OperatorAuthViewModel = koinViewModel()
    val scanViewModel: OperatorScanViewModel = koinViewModel()
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val reservationViewModel: OperatorReservationSearchViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val scanState by scanViewModel.uiState.collectAsState()
    val salesState by salesViewModel.uiState.collectAsState()
    val reservationState by reservationViewModel.uiState.collectAsState()
    var currentRoute by rememberSaveable { mutableStateOf(OperatorDestination.Login.route) }
    val currentDestination by remember(currentRoute) {
        derivedStateOf { OperatorDestination.fromRoute(currentRoute) }
    }
    val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    val loadingMessage = remember(
        authState.isLoading,
        authState.isSyncing,
        scanState.isLoading,
        salesState.isLoading,
        reservationState.isLoading
    ) {
        when {
            authState.isLoading -> "Restaurando sesion del operador..."
            authState.isSyncing -> "Sincronizando ventas pendientes..."
            scanState.isLoading -> "Cargando datos de la reserva..."
            salesState.isLoading -> "Actualizando venta y enviando notificacion..."
            reservationState.isLoading -> "Buscando ventas y reservaciones..."
            else -> null
        }
    }

    LaunchedEffect(authState.currentUser?.id) {
        currentRoute = if (authState.currentUser != null) {
            OperatorDestination.Home
        } else {
            OperatorDestination.Login
        }.route
    }

    LaunchedEffect(authState.currentUser?.id, notificationPermissionState.status) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
        if (authState.currentUser == null) return@LaunchedEffect
        if (notificationPermissionState.status is PermissionStatus.Denied) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            OperatorNavHost(
                currentDestination = currentDestination,
                onNavigate = { currentRoute = it.route }
            )
            OperatorLoadingToast(
                visible = loadingMessage != null,
                message = loadingMessage ?: ""
            )
        }
    }
}
