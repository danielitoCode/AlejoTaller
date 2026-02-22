package com.elitec.alejotaller.infraestructure.core.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.infraestructure.core.presentation.components.AutoScrollRows
import com.elitec.alejotaller.infraestructure.core.presentation.components.LandingButtons
import com.elitec.alejotaller.infraestructure.core.presentation.components.LandingTittle
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode

@Composable
fun LandScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode()


    val transition = rememberInfiniteTransition(label = "landing")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(2800)),
        label = "glowAlpha"
    )
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    when (deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 3 }
                ) {
                    LandingTittle()
                }
                AutoScrollRows(
                    rowCount = 1,
                    modifier = Modifier.height(350.dp)
                )
                LandingButtons(
                    onSignInClick = onSignInClick,
                    onSignUpClick = onSignUpClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        AppWindowType.MobileLandscape -> {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                LandingTittle(modifier = Modifier.fillMaxWidth())
                LandingButtons(
                    onSignInClick = onSignInClick,
                    onSignUpClick = onSignUpClick,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
        AppWindowType.TabletLandscape -> TODO()
        AppWindowType.Laptop -> TODO()
        AppWindowType.DesktopVertical -> TODO()
        AppWindowType.Expanded -> TODO()
    }

}

@Preview(
    showBackground = true, device = "spec:width=411dp,height=891dp"
)

@Composable
private fun LandingScreenPreview() {
    AlejoTallerTheme {
        LandScreen()
    }
}