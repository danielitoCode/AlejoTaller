package com.elitec.alejotaller.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import org.koin.androidx.compose.koinViewModel

@Suppress("LambdaParameterInEffect")
@Composable
fun SplashScreen(
    onUserAuth: (String) -> Unit,
    onUserNotAuth: () -> Unit,
    profileViewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    var hasRequestedSessionCheck by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(hasRequestedSessionCheck) {
        if (hasRequestedSessionCheck) {
            return@LaunchedEffect
        }
        hasRequestedSessionCheck = true
        profileViewModel.getAccountInfo(
            onGetInfo = { userId ->
                onUserAuth(userId)
            },
            onFail = { onUserNotAuth() }
        )
    }
    SplashScreenContent(
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
private fun SplashScreenContent(
    modifier: Modifier = Modifier
) {
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


@Preview
@Composable
fun SplashScreenContentPreview() {
    AlejoTallerTheme() {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            SplashScreenContent(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}