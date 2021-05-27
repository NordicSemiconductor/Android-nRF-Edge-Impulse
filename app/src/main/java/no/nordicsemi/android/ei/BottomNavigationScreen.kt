package no.nordicsemi.android.ei

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDestination

/**
 * BottomNavigationScreen
 *
 * @param route Route
 * @param resourceId String resource for the bottom navigation bar item name
 * @param drawableRes ImageVector for the icon
 */
enum class BottomNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val drawableRes: Int,
    val shouldFabBeVisible: Boolean,
) {
    Devices(
        route = Route.devices,
        resourceId = R.string.label_devices,
        drawableRes = R.drawable.ic_devices,
        shouldFabBeVisible = false
    ),

    DataAcquisition(
        route = Route.dataAcquisition,
        resourceId = R.string.label_data_acquisition,
        drawableRes = R.drawable.ic_database,
        shouldFabBeVisible = true
    ),

    Deployment(
        route = Route.deployment,
        resourceId = R.string.label_deployment,
        //TODO find the correct edge impulse icon
        drawableRes = R.drawable.ic_devices,
        shouldFabBeVisible = false
    );

    companion object {
        fun fromNav(navDestination: NavDestination) = when (navDestination.route) {
            Devices.route -> Devices
            DataAcquisition.route -> DataAcquisition
            Deployment.route -> Deployment
            else -> throw IllegalArgumentException("$navDestination.route is not a valid route")
        }
    }
}