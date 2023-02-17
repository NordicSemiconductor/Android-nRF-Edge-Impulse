/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service.param

/**
 * Response for "Get JWT token" request.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#login-1">Docs: Get JWT token</a>
 */
data class LoginResponse(
    /** JWT token, to be used to log in in the future through JWTAuthentication. */
    val token: String?,
    /** Whether the operation succeeded. */
    val success: Boolean,
    /** Optional error description (set if [success] was false). */
    val error: String?
)
