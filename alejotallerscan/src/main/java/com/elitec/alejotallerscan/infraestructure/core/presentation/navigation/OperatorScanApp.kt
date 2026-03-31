package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorScanApp() {
    val authViewModel: OperatorAuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()
    var currentDestination by rememberSaveable(
        stateSaver = Saver(
            save = { destination -> destination.route },
            restore = { route -> OperatorDestination.fromRoute(route) }
        )
    ) {
        mutableStateOf<OperatorDestination>(OperatorDestination.Login)
    }

    LaunchedEffect(authState.currentUser?.id) {
        currentDestination = if (authState.currentUser != null) {
            OperatorDestination.Home
        } else {
            OperatorDestination.Login
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        OperatorNavHost(
            currentDestination = currentDestination,
            onNavigate = { currentDestination = it }
        )
    }
}
