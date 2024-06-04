/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels.state

sealed class DeleteState {
    data object NotDeleted : DeleteState()
    data object Deleting : DeleteState()
    data object Deleted : DeleteState()
    data class Error(val throwable: Throwable) : DeleteState()
}