package no.nordicsemi.android.ei.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.ei.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object Utils {

    fun isMarshMellowOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isBluetoothEnabled(): Boolean =
        BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false

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