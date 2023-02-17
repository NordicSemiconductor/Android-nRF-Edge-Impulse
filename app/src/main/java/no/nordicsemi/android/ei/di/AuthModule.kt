/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.di

import android.accounts.AbstractAccountAuthenticator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import no.nordicsemi.android.ei.account.AccountAuthenticator

@Module
@InstallIn(ServiceComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthenticator(
        authenticator: AccountAuthenticator
    ): AbstractAccountAuthenticator

}