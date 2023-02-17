/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import no.nordicsemi.android.ei.repository.UserDataRepository

@EntryPoint
@InstallIn(UserComponent::class)
interface UserComponentEntryPoint {
    fun userDataRepository(): UserDataRepository

    fun getProjectManager(): ProjectManager
}