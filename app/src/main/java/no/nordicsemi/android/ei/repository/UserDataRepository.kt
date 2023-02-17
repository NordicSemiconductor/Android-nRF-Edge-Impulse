/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.repository

import no.nordicsemi.android.ei.di.LoggedUserScope
import no.nordicsemi.android.ei.di.ProjectComponent
import no.nordicsemi.android.ei.model.User
import javax.inject.Inject

@LoggedUserScope
class UserDataRepository @Inject constructor(
    val builder: ProjectComponent.Builder,
    val user: User,
    val token: String
)