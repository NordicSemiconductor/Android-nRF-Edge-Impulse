package no.nordicsemi.android.ei.util

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.location.LocationManagerCompat
import no.nordicsemi.android.ei.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Utils {

    fun isMarshMellowOrAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun isBetweenMarshmallowAndS(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S

    fun isSorAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun isBluetoothEnabled(context: Context): Boolean =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled

    fun isLocationEnabled(context: Context) = LocationManagerCompat.isLocationEnabled(
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    )
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

fun Double.round(decimalPoints: Int = 4): BigDecimal {
    return BigDecimal(this).setScale(
        decimalPoints,
        RoundingMode.HALF_EVEN
    )
}