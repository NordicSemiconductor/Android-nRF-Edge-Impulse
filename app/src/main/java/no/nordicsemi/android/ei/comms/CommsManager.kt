package no.nordicsemi.android.ei.comms

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import no.nordicsemi.android.ei.ble.BleDevice
import no.nordicsemi.android.ei.model.DeviceMessage
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import no.nordicsemi.android.ei.websocket.EiWebSocket

class CommsManager(
    private val bleDevice: BleDevice,
    private val eiWebSocket: EiWebSocket
) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Message::class.java, MessageTypeAdapter())
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    suspend fun connect() {
        bleDevice.connect()
    }

    suspend fun authenticate(deviceMessage: DeviceMessage) {
        val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject
        eiWebSocket.connect()
        //TODO remove this delay and we have to observe the messages emitted from the socket
        delay(5000)
        eiWebSocket.send(deviceMessageJson.get(MESSAGE).asJsonObject)
    }

    //TODO sending messages from the phone to the device
    fun send(deviceMessage: DeviceMessage) {
        val deviceMessageJson = JsonParser.parseString(gson.toJson(deviceMessage)).asJsonObject
        eiWebSocket.send(deviceMessageJson.get(MESSAGE).asJsonObject)
    }

    fun disconnect() {
        eiWebSocket.disconnect()
        bleDevice.disconnect()
    }
}

private const val MESSAGE = "message"