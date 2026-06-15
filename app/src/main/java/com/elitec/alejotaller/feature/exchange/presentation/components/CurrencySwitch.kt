package com.elitec.alejotaller.feature.exchange.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.exchange.presentation.viewmodel.ExchangeViewModel

@Composable
fun CurrencySwitch(
    viewModel: ExchangeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = modifier
            .clickable { viewModel.toggleCurrency() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = uiState.selectedCurrency,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Switch Currency",
            modifier = Modifier.size(20.dp).padding(start = 4.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
