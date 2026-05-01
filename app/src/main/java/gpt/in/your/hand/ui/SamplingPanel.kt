package gpt.`in`.your.hand.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gpt.`in`.your.hand.inference.SamplingConfig

@Composable
fun SamplingPanel(
    config: SamplingConfig,
    onChange: (SamplingConfig) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("샘플링 옵션")

            SliderRow(
                label = "Temperature",
                value = config.temperature,
                valueRange = 0.0f..1.5f,
                display = "%.2f".format(config.temperature),
                enabled = enabled,
                onValueChange = { onChange(config.copy(temperature = it)) },
            )
            SliderRow(
                label = "Top-K",
                value = config.topK.toFloat(),
                valueRange = 1f..100f,
                display = config.topK.toString(),
                enabled = enabled,
                onValueChange = { onChange(config.copy(topK = it.toInt())) },
            )
            SliderRow(
                label = "Top-P",
                value = config.topP,
                valueRange = 0.1f..1.0f,
                display = "%.2f".format(config.topP),
                enabled = enabled,
                onValueChange = { onChange(config.copy(topP = it)) },
            )
            SliderRow(
                label = "Repeat penalty",
                value = config.repeatPenalty,
                valueRange = 1.0f..2.0f,
                display = "%.2f".format(config.repeatPenalty),
                enabled = enabled,
                onValueChange = { onChange(config.copy(repeatPenalty = it)) },
            )
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Text("$label: $display")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
        )
    }
}
