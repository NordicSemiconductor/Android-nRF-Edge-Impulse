/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.util

enum class Engine(val engine: String) {
    TFLITE("tflite"),
    TFLITE_EON("tflite-eon"),
    REQUIRES_RETRAIN("requiresRetrain")
}