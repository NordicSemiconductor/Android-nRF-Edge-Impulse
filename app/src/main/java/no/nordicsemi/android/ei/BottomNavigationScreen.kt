package no.nordicsemi.android.ei

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * BottomNavigationScreen
 *
 * @param route Route
 * @param resourceId String resource for the bottom navigation bar item name
 * @param drawableRes ImageVector for the icon
 */
sealed class BottomNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val drawableRes: Int
) {
    object Devices : BottomNavigationScreen(
        route = Route.devices,
        resourceId = R.string.label_devices,
        drawableRes = R.drawable.ic_devices
    )

    object DataAcquisition : BottomNavigationScreen(
        route = Route.dataAcquisition,
        resourceId = R.string.label_data_acquisition,
        drawableRes = R.drawable.ic_database
    )

    object Deployment : BottomNavigationScreen(
        route = Route.deployment,
        resourceId = R.string.label_deployment,
        //TODO find the correct edge impulse icon
        drawableRes = R.drawable.ic_devices
    )
}