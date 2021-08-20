package no.nordicsemi.android.ei.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import no.nordicsemi.android.ei.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Utils {

    fun isMarshMellowOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isAndroidS(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.S
    }

    fun isBluetoothEnabled(): Boolean =
        BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

    @RequiresApi(Build.VERSION_CODES.S)
    fun isBluetoothPermissionsGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

    fun isLocationEnabled(context: Context) =
        !isMarshMellowOrAbove() || LocationManagerCompat.isLocationEnabled(
            context.getSystemService(LocationManager::class.java)
        )

    fun isLocationPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

}

val <T> T.exhaustive: T
    get() = this

@Composable
fun Throwable.asMessage(
    defaultMessage: String = stringResource(id = R.string.error_unknown)
): String = when (this) {
    is UnknownHostException -> stringResource(id = R.string.error_no_internet)
    is SocketTimeoutException -> stringResource(id = R.string.error_timeout)
    else -> localizedMessage ?: defaultMessage
}

fun Throwable.asMessage(
    context: Context,
    defaultMessage: String = context.getString(R.string.error_unknown)
): String = when (this) {
    is UnknownHostException -> context.getString(R.string.error_no_internet)
    is SocketTimeoutException -> context.getString(R.string.error_timeout)
    else -> localizedMessage ?: defaultMessage
}