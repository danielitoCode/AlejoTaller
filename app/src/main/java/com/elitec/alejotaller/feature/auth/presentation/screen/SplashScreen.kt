package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.navigation.koinNavViewModel

@Suppress("LambdaParameterInEffect")
@Composable
fun SplashScreen(
    onUserAuth: (String) -> Unit,
    onUserNotAuth: () -> Unit,
    profileViewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(null) {
        delay(1000)
        profileViewModel.getAccountInfo(
            onGetInfo = { userId ->
                onUserAuth(userId)
            },
            onFail = { onUserNotAuth() }
        )
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Icon(
            painter = painterResource(R.drawable.alejoicon_clean),
            contentDescription = "App icon",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(180.dp)
        )
    }
}