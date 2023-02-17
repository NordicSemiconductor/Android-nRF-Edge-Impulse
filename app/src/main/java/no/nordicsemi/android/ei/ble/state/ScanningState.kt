/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ble.state

/**
 * ScannerState that holds the current scanning state.
 */
sealed class ScanningState {
    object Initializing : ScanningState()
    object Started : ScanningState()
    object Stopped : ScanningState()
}