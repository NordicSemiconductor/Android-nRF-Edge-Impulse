/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

data class User(
    val email: String,
    val activated: Boolean,
    val projects: List<Project>,
    val id: Int,
    val username: String,
    val name: String,
    val photo: String?,
    val created: String,
    val mfaConfigured: Boolean,
    val isEdgeImpulseStaff: Boolean,
    // TODO: move those outside: https://stackoverflow.com/questions/37098794/is-it-possible-to-flatten-the-json-hierarchy-with-gson
    val success: Boolean,
    val error: String
)
