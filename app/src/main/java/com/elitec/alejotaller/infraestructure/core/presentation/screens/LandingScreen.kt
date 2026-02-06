package com.elitec.alejotaller.infraestructure.core.presentation.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val backgroundColor = Color(0xFF42549E)
    val darkBlue = Color(0xFF1E2856)
    val lightBlueGradient = Color(0xFF7A91D6)
    val white = Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(lightBlueGradient, darkBlue)
                )
            )
            
            drawPath(
                path = createBackgroundWavePath(size.width, size.height),
                color = Color(0xFF2C3C82).copy(alpha = 0.5f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1.2f))
                Text(
                    text = "Welcome Back!",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = white,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enter personal details to you\nemployee account",
                    fontSize = 18.sp,
                    color = white.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onSignInClick) {
                        Text(
                            text = "Sign in",
                            color = white,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 60.dp))
                        .background(white),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onSignUpClick) {
                        Text(
                            text = "Sign up",
                            color = backgroundColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun createBackgroundWavePath(width: Float, height: Float): Path {
    return Path().apply {
        moveTo(0f, height * 0.3f)
        cubicTo(
            width * 0.3f, height * 0.2f,
            width * 0.5f, height * 0.5f,
            width, height * 0.4f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun LandingScreenPreview() {
    AlejoTallerTheme {
        LandingScreen()
    }
}
