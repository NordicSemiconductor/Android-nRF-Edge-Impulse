package no.nordicsemi.android.ei

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Science
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * HorizontalPagerTab
 *
 * @param title         String resource for the HorizontalPagerTab item title
 * @param category      Category of samples to be loaded
 * @param rowIcon       ImageVector to use for rows
 * @param emptyListIcon ImageVector to use when no data is available
 */
sealed class HorizontalPagerTab(
    @StringRes val title: Int,
    @StringRes val category: Int,
    val rowIcon: ImageVector,
    val emptyListIcon: ImageVector
) {
    object Training : HorizontalPagerTab(
        title = R.string.title_training,
        category = R.string.param_category_training,
        rowIcon = Icons.Outlined.ModelTraining,
        emptyListIcon = Icons.Filled.ModelTraining
    )

    object Testing : HorizontalPagerTab(
        title = R.string.title_testing,
        category = R.string.param_category_testing,
        rowIcon = Icons.Outlined.Science,
        emptyListIcon = Icons.Filled.Science
    )

    object Anomaly : HorizontalPagerTab(
        title = R.string.title_anomaly,
        category = R.string.param_category_anomaly,
        rowIcon = Icons.Outlined.Psychology,
        emptyListIcon = Icons.Filled.Psychology
    )
}