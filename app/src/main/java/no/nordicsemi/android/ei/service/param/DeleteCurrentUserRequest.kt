/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Body parameters for "Delete current user" request.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference/edge-impulse-api/user/delete_current_user">Docs: Delete current user</a>
 */
data class DeleteCurrentUserRequest(
    /** Password. */
    val password: String,
    /** Two-factor authentication token. */
    val totpToken: String?
)
