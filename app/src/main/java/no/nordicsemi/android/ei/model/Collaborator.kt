/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

data class Collaborator(
    val id: Int,
    val username: String,
    val photo: String,
    val isEdgeImpulseStaff: Boolean,
    val success: Boolean,
    val error: String
)