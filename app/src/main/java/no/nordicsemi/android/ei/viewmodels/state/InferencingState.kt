/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels.state

/** InferencingState to notify the UI **/
sealed class InferencingState {
    /** Inferencing started state **/
    object Started : InferencingState()

    /** Inferencing stopped state **/
    object Stopped : InferencingState()
}
