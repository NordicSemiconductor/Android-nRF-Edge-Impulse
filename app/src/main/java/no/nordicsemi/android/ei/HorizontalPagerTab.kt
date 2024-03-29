/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * HorizontalPagerTab
 *
 * @param title    String resource for the HorizontalPagerTab item title.
 * @param category Category of samples to be loaded.
 * @param icon     ImageVector to use when no data is available.
 */
enum class HorizontalPagerTab(
    @StringRes val title: Int,
    @StringRes val category: Int,
    val icon: ImageVector
) {

    TRAINING(
        title = R.string.title_training,
        category = R.string.param_category_training,
        icon = Icons.Filled.ModelTraining
    ),

    TESTING(
        title = R.string.title_testing,
        category = R.string.param_category_testing,
        icon = Icons.Filled.Science
    );

    companion object {
        fun indexed(index: Int) = when (index) {
            0 -> TRAINING
            else -> TESTING
        }
    }
}