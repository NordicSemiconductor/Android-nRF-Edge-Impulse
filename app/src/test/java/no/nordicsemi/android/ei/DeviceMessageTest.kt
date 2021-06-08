package no.nordicsemi.android.ei

import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import no.nordicsemi.android.ei.model.*
import no.nordicsemi.android.ei.model.Direction.RECEIVE
import no.nordicsemi.android.ei.util.MessageTypeAdapter
import org.junit.Test

/**
 * WebSocketMessageTest
 *
 * Tests are based on the default values provided in the proposed https://gist.github.com/janjongboom/17a6a1036eb9639531752db1a7dce864#initial-handshake
 */
class DeviceMessageTest {

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
            JsonParser.parseString(
                gson.toJson(
                    WebSocketMessage(
                        message = Hello(
                            apiKey = "ei_1234",
                            deviceId = "01:23:45:67:89:AA",
                            deviceType = "NRF5340_DK",
                            connection = "ip",
                            sensors = listOf(
                                Sensor(
                                    name = "Accelerometer",
                                    maxSampleLengths = 60000,
                                    frequencies = listOf(62.5, 100)
                                ),
                                Sensor(
                                    name = "Microphone",
                                    maxSampleLengths = 4000,
                                    frequencies = listOf(16000)
                                )
                            )
                        )
                    )
                )
            )
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
                    WebSocketMessage(
                        direction = RECEIVE,
                        message = Success(hello = true)
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
                    WebSocketMessage(
                        direction = RECEIVE,
                        message = Error(
                            hello = false,
                            error = "API key is not correct, or a similar message"
                        )
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun configureMessageValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"type\": \"configure\",\n" +
                "    \"message\": {\n" +
                "        \"apiKey\": \"ei_123456\",\n" +
                "        \"address\": \"wss://studio.edgeimpulse.com\"\n" +
                "    }\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    ConfigureMessage(
                        message = Configure(
                            apiKey = "ei_123456",
                            address = "wss://studio.edgeimpulse.com"
                        )
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun sampleRequestMessageValidator_ReturnsTrue() {
        val jsonString = "{\n" +
                "    \"type\": \"ws\",\n" +
                "    \"direction\": \"rx\",\n" +
                "    \"address\": \"wss://studio.edgeimpulse.com\",\n" +
                "    \"message\": {\n" +
                "        \"sample\": {\n" +
                "            \"label\": \"wave\",\n" +
                "            \"length\": 10000,\n" +
                "            \"path\": \"/api/training/data\",\n" +
                "            \"hmacKey\": \"e561ff...\",\n" +
                "            \"interval\": 10,\n" +
                "            \"sensor\": \"Accelerometer\"\n" +
                "        }\n" +
                "    }\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    WebSocketMessage(
                        direction = RECEIVE,
                        message = SampleRequest(
                            label = "wave",
                            length = 10000,
                            path = "/api/training/data",
                            hmacKey = "e561ff...",
                            interval = 10,
                            sensor = "Accelerometer"
                        )
                    )
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()
    }

    @Test
    fun sendDataMessageValidator_ReturnsTrue() {
        //TODO need some sample data
        /*val jsonString = "{\n" +
                "    \"type\": \"http\",\n" +
                "    \"address\": \"https://ingestion.edgeimpulse.com/api/training/data\",\n" +
                "    \"method\": \"POST\",\n" +
                "    \"headers\": {\n" +
                "        \"x-api-key\": \"ei_12389211\",\n" +
                "        \"x-label\": \"wave\",\n" +
                "        \"x-allow-duplicates\": \"0\"\n" +
                "    },\n" +
                "    \"body\": \"base64 representation of the payload\"\n" +
                "}"
        val actualResult =
            JsonParser.parseString(
                gson.toJson(
                    SendDataMessage(body = "base64 representation of the payload".toByteArray())
                )
            )
        val expectedResult = JsonParser.parseString(jsonString).asJsonObject
        assertThat(expectedResult == actualResult).isTrue()*/
    }
}