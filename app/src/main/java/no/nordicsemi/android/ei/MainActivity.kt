/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            NordicTheme {
                // Loading indicator is shown behind the splash screen. Otherwise, in case the
                // user is not logged in, LoginActivity would be displayed immediately.
                /*if (splashScreenVisible) {
                    LoadingProgressIndicator()
                } else {
                }*/
                Navigation(onCancelled = { finish() })
            }
        }
    }
}