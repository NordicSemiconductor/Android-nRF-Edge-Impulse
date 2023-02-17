/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

/**
 *  Log model for logs when receiving build firmware logs.
 */
sealed class BuildLog {
    data class Data(val data: String) : BuildLog()
    data class Finished(val success: Boolean) : BuildLog()
}
