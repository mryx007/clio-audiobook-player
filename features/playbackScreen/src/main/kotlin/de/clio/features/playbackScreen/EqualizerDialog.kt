package de.clio.features.playbackScreen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.clio.core.data.EqualizerPreset
import de.clio.core.data.EqualizerSetting
import kotlin.math.roundToInt
import de.clio.core.strings.R as StringsR

@Composable
internal fun EqualizerDialog(
  dialogState: BookPlayDialogViewState.EqualizerDialog,
  viewModel: BookPlayViewModel,
) {
  AlertDialog(
    onDismissRequest = { viewModel.dismissDialog() },
    title = {
      Text(stringResource(id = StringsR.string.playback_equalizer_title))
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
      ) {
        // Presets Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          EqualizerPreset.entries.forEach { preset ->
            val isSelected = preset.setting.bands == dialogState.bands
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.onEqualizerPresetSelected(preset.setting) },
              label = { Text(preset.label()) },
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 10 Frequency Band Sliders
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState()),
        ) {
          dialogState.bands.forEachIndexed { index, gainDb ->
            val freqLabel = EqualizerSetting.Frequencies.getOrElse(index) { "${index + 1}" }
            val gainText = if (gainDb > 0) "+$gainDb dB" else "$gainDb dB"

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = freqLabel,
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = gainText,
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (gainDb != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Slider(
                value = gainDb.toFloat(),
                valueRange = -12f..12f,
                steps = 23,
                onValueChange = {
                  viewModel.onEqualizerBandChanged(index, it.roundToInt())
                },
              )
            }
          }
        }
      }
    },
    dismissButton = {
      TextButton(onClick = { viewModel.onEqualizerReset() }) {
        Text(stringResource(id = StringsR.string.playback_equalizer_reset))
      }
    },
    confirmButton = {
      TextButton(onClick = { viewModel.dismissDialog() }) {
        Text(stringResource(id = StringsR.string.common_action_close))
      }
    },
  )
}

@Composable
private fun EqualizerPreset.label(): String = when (this) {
  EqualizerPreset.Flat -> stringResource(StringsR.string.playback_equalizer_preset_flat)
  EqualizerPreset.VocalClarity -> stringResource(StringsR.string.playback_equalizer_preset_vocal_clarity)
  EqualizerPreset.TrebleBoost -> stringResource(StringsR.string.playback_equalizer_preset_treble_boost)
  EqualizerPreset.BassCut -> stringResource(StringsR.string.playback_equalizer_preset_bass_cut)
  EqualizerPreset.DeEsser -> stringResource(StringsR.string.playback_equalizer_preset_de_esser)
}
