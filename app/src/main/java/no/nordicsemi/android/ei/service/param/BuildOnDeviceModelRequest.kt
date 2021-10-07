package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.util.Engine

data class BuildOnDeviceModelRequest(
    val engine: String = Engine.TFLITE_EON.engine/*,
    Note: Removed optional parameter to void build failures on EI backend
    val modelType: String = ModelType.INT_8.modelType*/
)