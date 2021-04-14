package no.nordicsemi.android.ei

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * BottomNavigationScreen
 *
 * @param route Route
 * @param resourceId String resource for the bottom navigation bar item name
 * @param icon ImageVector for the icon
 */
sealed class BottomNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    object Devices : BottomNavigationScreen(
        route = Route.devices,
        resourceId = R.string.label_devices,
        icon = Icons.Default.CheckCircle
    )

    object DataAcquisition : BottomNavigationScreen(
        route = Route.dataAcquisition,
        resourceId = R.string.label_data_acquisition,
        icon = Icons.Default.CheckCircle
    )

    object Deployment : BottomNavigationScreen(
        route = Route.deployment,
        resourceId = R.string.label_deployment,
        icon = Icons.Default.CheckCircle
    )
}