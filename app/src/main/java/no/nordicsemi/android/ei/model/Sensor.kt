/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

data class Sensor(
    val name: String,
    @SerializedName("maxSampleLengthS")
    val maxSampleLengths: Int,
    val frequencies: List<Number>
)
