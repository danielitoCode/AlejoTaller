package com.elitec.alejotaller.feature.exchange.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.exchange.presentation.viewmodel.ExchangeViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.shared.data.feature.sale.data.dto.toCurrency
import com.elitec.shared.sale.feature.sale.domain.entity.Currency

@Composable
private fun CurrencySwitchContent(
    selectedCurrency: String,
    onOptionSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cupButtonElevation = animateDpAsState(
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        targetValue = if(selectedCurrency.toCurrency() == Currency.CUP) 2.dp else 10.dp
    )
    var usdButtonElevation = animateDpAsState(
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        targetValue = if(selectedCurrency.toCurrency() == Currency.USD) 2.dp else 10.dp
    )
    var cupButtonColor = animateColorAsState(
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        targetValue = if(selectedCurrency.toCurrency() == Currency.CUP)
            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(0.5f)
    )
    var usdButtonColor = animateColorAsState(
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        targetValue = if(selectedCurrency.toCurrency() == Currency.USD)
            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(0.5f)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = usdButtonColor.value
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = usdButtonElevation.value
            ),
            shape = RoundedCornerShape(
                topStart = 15.dp,
                bottomStart = 15.dp,
                topEnd = 5.dp,
                bottomEnd = 5.dp
            ),
            onClick = {
                if(selectedCurrency != Currency.USD.name) onOptionSelected()
            },
        ) {
            Text(
                style = MaterialTheme.typography.headlineSmall,
                text = "USD"
            )
        }
        Spacer(
            modifier = Modifier.width(3.dp)
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = cupButtonColor.value
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = cupButtonElevation.value
            ),
            shape = RoundedCornerShape(
                topStart = 5.dp,
                bottomStart = 5.dp,
                topEnd = 15.dp,
                bottomEnd = 15.dp
            ),
            onClick = { if(selectedCurrency != Currency.CUP.name) onOptionSelected() }
        ) {
            Text(
                style = MaterialTheme.typography.headlineSmall,
                text = "CUP"
            )
        }
    }

}


@Composable
fun CurrencySwitch(
    viewModel: ExchangeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    CurrencySwitchContent(
        selectedCurrency = uiState.selectedCurrency,
        onOptionSelected = { viewModel.toggleCurrency() }
    )
}

@Preview(
    showBackground = true
)
@Composable
fun CurrencySwitchPreview() {
    var selectedCurrency by remember { mutableStateOf(Currency.USD.name) }
    AlejoTallerTheme() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = selectedCurrency,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            CurrencySwitchContent(
                selectedCurrency = selectedCurrency,
                onOptionSelected = {
                    if(selectedCurrency == Currency.USD.name)
                        selectedCurrency = Currency.CUP.name
                    else
                        selectedCurrency = Currency.USD.name
                }
            )
        }
    }
}

