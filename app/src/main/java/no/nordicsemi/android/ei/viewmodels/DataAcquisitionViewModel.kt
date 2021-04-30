package no.nordicsemi.android.ei.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sensor
import javax.inject.Inject

@HiltViewModel
class DataAcquisitionViewModel @Inject constructor() : ViewModel() {

    val focusRequester = FocusRequester()
    var selectedDevice: Device? by mutableStateOf(null)
        private set
    var label: String by mutableStateOf("")
        private set
    var selectedSensor: Sensor? by mutableStateOf(null)
        private set
    var selectedFrequency: Number? by mutableStateOf(null)
        private set

    fun onDeviceSelected(device: Device) {
        selectedDevice = device
        device.sensors.takeIf { sensors -> sensors.isNotEmpty() }
            ?.let { sensors -> onSensorSelected(sensor = sensors[0]) }
    }

    fun onLabelChanged(label: String) {
        this.label = label
    }

    fun onSensorSelected(sensor: Sensor) {
        this.selectedSensor = sensor
        sensor.frequencies
            .takeIf { frequencies ->
                frequencies.isNotEmpty()
            }?.let { frequencies ->
                onFrequencySelected(frequency = frequencies[0])
            } ?: run { selectedFrequency = null }
    }

    fun onFrequencySelected(frequency: Number) {
        this.selectedFrequency = frequency
    }
}