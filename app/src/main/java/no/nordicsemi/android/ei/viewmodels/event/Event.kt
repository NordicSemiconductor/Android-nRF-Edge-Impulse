/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels.event

sealed class Event {

    sealed class Project {
        data class Created(val projectName: String): Event()
        data class Selected(val project: no.nordicsemi.android.ei.model.Project): Event()
    }

    data class Error(val throwable: Throwable): Event()
}