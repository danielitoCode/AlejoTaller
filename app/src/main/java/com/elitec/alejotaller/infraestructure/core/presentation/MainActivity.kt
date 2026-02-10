package com.elitec.alejotaller.infraestructure.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterDefaults
import com.dokar.sonner.rememberToasterState
import com.elitec.alejotaller.R
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.MainNavigationWrapper
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToastAction
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlejoTallerTheme {
                val toasterViewModel: ToasterViewModel = koinViewModel()
                val toasterState = rememberToasterState()

                LaunchedEffect(toasterViewModel) {
                    toasterViewModel.toastActions.collect { action ->
                        when (action) {
                            is ToastAction.Show -> toasterState.show(
                                message = action.event.message,
                                type = action.event.type,
                                id = action.event.id
                            )
                            is ToastAction.Dismiss -> toasterState.dismiss(action.id)
                        }
                    }
                }
                val image = if(isSystemInDarkTheme())
                    painterResource(R.drawable.bcb)
                else
                    painterResource(R.drawable.bcv)
                val verticalBrush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background.copy(0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = image,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(brush = verticalBrush)
                    )
                    Scaffold(
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        MainNavigationWrapper(
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        )
                    }
                    Toaster(
                        richColors = true,
                        state = toasterState
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlejoTallerTheme {
        Greeting("Android")
    }
}