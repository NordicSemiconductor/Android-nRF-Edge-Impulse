package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.util.Engine
import no.nordicsemi.android.ei.util.ModelType

data class BuildOnDeviceModelRequest(
    val engine: String = Engine.TFLITE_EON.engine,
    val modelType: String = ModelType.INT_8.modelType
)