package no.nordicsemi.android.ei.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.JsonMerger
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ei.util.guard
import java.util.*

class BleDevice(
    val device: BluetoothDevice,
    context: Context
) : BleManager(context) {

    companion object {
        val serviceUuid: UUID = UUID.fromString("E2A00001-EC31-4EC3-A97A-1C34D87E9878")
        val rxUuid: UUID = UUID.fromString("E2A00002-EC31-4EC3-A97A-1C34D87E9878")
        val txUuid: UUID = UUID.fromString("E2A00003-EC31-4EC3-A97A-1C34D87E9878")
    }

    private var rx: BluetoothGattCharacteristic? = null
    private var tx: BluetoothGattCharacteristic? = null

    /** The emitter is used to publish data to a flow, when a collector is registered. */
    private var emitter: ((String) -> Unit)? = null

    override fun log(priority: Int, message: String) {
        Log.println(priority, "BleDevice", message)
    }

    override fun getGattCallback(): BleManagerGattCallback = object : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(serviceUuid)
                .guard { return false }
                .apply {
                    rx = getCharacteristic(
                        rxUuid,
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                    )
                        .guard { return false }
                    tx = getCharacteristic(txUuid, BluetoothGattCharacteristic.PROPERTY_NOTIFY)
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
            setNotificationCallback(tx)
                .merge(JsonMerger())
                .with { _, data ->
                    emitter?.let { emit ->
                        data.getStringValue(0)?.also(emit)
                    }
                }
        }

    }

    // TODO should the API be suspendable, or just calling .enqueue()?
    fun connect() {
        connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
            .enqueue()
    }

    // TODO what should the api be?
    fun disconnectDevice() {
        disconnect().enqueue()
    }

    /**
     * Returns the shared flow with incoming data.
     */
    @ExperimentalCoroutinesApi
    fun messagesAsFlow(): Flow<String> = callbackFlow {
        emitter = { json -> trySend(json) }
        awaitClose { emitter = null }
    }

    /**
     * Enables notifications. The first message that should be received is the Hello message.
     */
    fun initialize() {
        enableNotifications(tx).enqueue()
    }

    /**
     * Sends the given data to the device.
     */
    fun send(string: String) {
        writeCharacteristic(rx, string.encodeToByteArray(), WRITE_TYPE_NO_RESPONSE)
            .split()
            .enqueue()
    }
}