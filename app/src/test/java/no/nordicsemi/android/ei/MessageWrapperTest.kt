package no.nordicsemi.android.ei

import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import no.nordicsemi.android.ei.model.*
import no.nordicsemi.android.ei.model.Direction.Receive
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import org.junit.Test

class MessageWrapperTest {

    private val gson =
        GsonBuilder()
            .registerTypeAdapter(Message::class.java, MessageTypeAdapter())
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun initialHandshakeValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"type\": \"ws\",\n" +
                "    \"direction\": \"tx\",\n" +
                "    \"address\": \"wss://studio.edgeimpulse.com\",\n" +
                "    \"message\": {\n" +
                "        \"hello\": {\n" +
                "            \"version\": 3,\n" +
                "            \"apiKey\": \"ei_1234\",\n" +
                "            \"deviceId\": \"01:23:45:67:89:AA\",\n" +
                "            \"deviceType\": \"NRF5340_DK\",\n" +
                "            \"connection\": \"ip\",\n" +
                "            \"sensors\": [{\n" +
                "                \"name\": \"Accelerometer\",\n" +
                "                \"maxSampleLengthS\": 60000,\n" +
                "                \"frequencies\": [ 62.5, 100 ]\n" +
                "            }, {\n" +
                "                \"name\": \"Microphone\",\n" +
                "                \"maxSampleLengthS\": 4000,\n" +
                "                \"frequencies\": [ 16000 ]\n" +
                "            }],\n" +
                "            \"supportsSnapshotStreaming\": false\n" +
                "        }\n" +
                "    }\n" +
                "}"
        val actualResult =
            JsonParser.parseString(gson.toJson(MessageWrapper(message = Message(deviceMessage = Hello()))))
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun initialHandshakeServerSuccessValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"hello\": true\n" +
                "}"
        val actualResult =
            JsonParser.parseString(gson.toJson(Success(hello = true)))
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun initialHandshakeServerSuccessDeviceMessageValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"type\": \"ws\",\n" +
                "    \"direction\": \"rx\",\n" +
                "    \"address\": \"wss://studio.edgeimpulse.com\",\n" +
                "    \"message\": {\n" +
                "        \"hello\": true\n" +
                "    }\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    MessageWrapper(
                        direction = Receive.direction,
                        message = Message(
                            deviceMessage = Success(
                                hello = true
                            )
                        )
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun initialHandshakeServerErrorValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"hello\": false,\n" +
                "    \"error\": \"API key is not correct, or a similar message\"\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    Error(
                        hello = false,
                        error = "API key is not correct, or a similar message"
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun initialHandshakeServerErrorDeviceMessageValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"type\": \"ws\",\n" +
                "    \"direction\": \"rx\",\n" +
                "    \"address\": \"wss://studio.edgeimpulse.com\",\n" +
                "    \"message\": {\n" +
                "    \"hello\": false,\n" +
                "    \"error\": \"API key is not correct, or a similar message\"\n" +
                "    }\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    MessageWrapper(
                        direction = Receive.direction,
                        message = Message(
                            deviceMessage = Error(
                                hello = false,
                                error = "API key is not correct, or a similar message"
                            )
                        )
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }
}