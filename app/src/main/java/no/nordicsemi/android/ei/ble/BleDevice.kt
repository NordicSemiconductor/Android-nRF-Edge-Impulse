package no.nordicsemi.android.ei.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ei.util.guard
import java.util.*

class BleDevice(
    val device: BluetoothDevice,
    context: Context
): BleManager(context) {

    companion object {
        val serviceUuid: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val rxUuid: UUID      = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val txUuid: UUID      = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    }

    private var rx: BluetoothGattCharacteristic? = null
    private var tx: BluetoothGattCharacteristic? = null

    private var _message = MutableSharedFlow<ByteArray>()

    override fun getGattCallback(): BleManagerGattCallback = object: BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(serviceUuid)
                .guard { return false }
                .apply {
                    tx = getCharacteristic(txUuid, BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                        .guard { return false }
                    rx = getCharacteristic(rxUuid, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                        .guard { return false }
                }
            return true
        }

        override fun onServicesInvalidated() {
            tx = null
            rx = null
        }

        override fun initialize() {
            requestMtu(512).enqueue()
            setNotificationCallback(tx).with { _, data ->
                _message.tryEmit(data.value ?: byteArrayOf())
            }
            enableNotifications(tx).enqueue()
        }

    }

    // TODO should the API be suspendable, or just calling .enqueue()?
    suspend fun connect() {
        connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
            .suspend()
    }

    /**
     * Returns the shared flow with incoming data.
     */
    fun receiveAsFlow(): Flow<ByteArray> {
        return _message
    }

    /**
     * Sends the given data to the device.
     */
    suspend fun send(byteArray: ByteArray) {
        writeCharacteristic(rx, byteArray).suspend()
    }
}