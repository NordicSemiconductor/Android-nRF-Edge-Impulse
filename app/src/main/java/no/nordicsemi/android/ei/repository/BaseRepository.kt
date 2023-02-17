/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ei.di.IODispatcher
import no.nordicsemi.android.ei.service.EiService

abstract class BaseRepository(
    protected val service: EiService,
    @IODispatcher protected val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getCurrentUser(token: String) = withContext(ioDispatcher) {
        service.getCurrentUser(jwt = token)
    }
}