/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

import com.google.gson.annotations.SerializedName

/**
 * Direction
 */
enum class Direction {
    @SerializedName("rx")
    RECEIVE,

    @SerializedName("tx")
    SEND
}
