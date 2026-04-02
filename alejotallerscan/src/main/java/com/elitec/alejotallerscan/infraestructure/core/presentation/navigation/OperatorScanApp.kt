package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

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
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorScanApp(
    modifier: Modifier = Modifier
) {
    val authViewModel: OperatorAuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()
    var currentRoute by rememberSaveable { mutableStateOf(OperatorDestination.Login.route) }
    val currentDestination by remember(currentRoute) {
        derivedStateOf { OperatorDestination.fromRoute(currentRoute) }
    }

    LaunchedEffect(authState.currentUser?.id) {
        currentRoute = if (authState.currentUser != null) {
            OperatorDestination.Home
        } else {
            OperatorDestination.Login
        }.route
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        OperatorNavHost(
            currentDestination = currentDestination,
            onNavigate = { currentRoute = it.route }
        )
    }
}
