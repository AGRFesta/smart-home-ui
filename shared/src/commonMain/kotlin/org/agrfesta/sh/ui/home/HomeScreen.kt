package org.agrfesta.sh.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.agrfesta.sh.ui.API_VERSION
import org.agrfesta.sh.ui.APP_VERSION
import org.agrfesta.sh.ui.api.Area
import org.agrfesta.sh.ui.api.FieldResult.Failure
import org.agrfesta.sh.ui.api.FieldResult.Success
import org.agrfesta.sh.ui.api.GlobalState

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    LaunchedEffect(viewModel) {
        viewModel.loadHome()
    }
    val uiState by viewModel.uiState.collectAsState()
    MaterialTheme {
        Surface {
            HomeContent(uiState = uiState)
        }
    }
}

@Composable
private fun GlobalStateBanner(globalState: GlobalState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val heatingActive = globalState.heatingActive
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "🔥", style = MaterialTheme.typography.bodyMedium)
            if (heatingActive is Failure) {
                Text(
                    text = "⚠",
                    modifier = Modifier.testTag("heating_active_warning"),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = if (heatingActive == Success(true)) "ATTIVO" else "INATTIVO",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (heatingActive == Success(true)) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val strategy = globalState.strategy
        if (heatingActive == Success(true) && strategy is Failure) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "📋", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "⚠",
                    modifier = Modifier.testTag("strategy_warning"),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (heatingActive == Success(true) && strategy is Success && strategy.value != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "📋", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = strategy.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun AreaCard(area: Area) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = area.name, style = MaterialTheme.typography.titleMedium)
            area.measurements.humidity?.let { humidity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "💧", style = MaterialTheme.typography.bodyMedium)
                    when (val relative = humidity.relative) {
                        is Failure -> Text(
                            text = "⚠",
                            modifier = Modifier.testTag("humidity_warning"),
                            color = MaterialTheme.colorScheme.error
                        )
                        is Success -> Text(
                            text = if (relative.value != null) "${(relative.value * 100).roundToInt()}%" else "─",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            area.measurements.heating?.let { heating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🌡", style = MaterialTheme.typography.bodyMedium)
                    when (val currentTemperature = heating.currentTemperature) {
                        is Failure -> Text(
                            text = "⚠",
                            modifier = Modifier.testTag("temperature_warning"),
                            color = MaterialTheme.colorScheme.error
                        )
                        is Success -> Text(
                            text = if (currentTemperature.value != null) "${currentTemperature.value}°C" else "─",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeContent(uiState: HomeUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                HomeUiState.Loading,
                HomeUiState.Unauthorized -> CircularProgressIndicator(
                    modifier = Modifier.testTag("home_loading_indicator")
                )
                is HomeUiState.Error -> Text(text = uiState.message)
                is HomeUiState.Success -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GlobalStateBanner(globalState = uiState.data.globalState)
                    }
                    items(uiState.data.areas, key = { it.id }) { area ->
                        AreaCard(area = area)
                    }
                }
            }
        }
        Text(
            text = "smart-home v$APP_VERSION  |  API v$API_VERSION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
                .testTag("version_footer")
        )
    }
}
