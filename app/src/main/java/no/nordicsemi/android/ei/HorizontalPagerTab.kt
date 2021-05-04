package no.nordicsemi.android.ei

import androidx.annotation.StringRes

/**
 * HorizontalPagerTab
 *
 * @param title String resource for the HorizontalPagerTab item title
 */
sealed class HorizontalPagerTab(
    @StringRes val title: Int
) {
    object Training : HorizontalPagerTab(
        title = R.string.title_training
    )

    object Testing : HorizontalPagerTab(
        title = R.string.title_testing
    )

    object Anomaly : HorizontalPagerTab(
        title = R.string.title_anomaly
    )
}