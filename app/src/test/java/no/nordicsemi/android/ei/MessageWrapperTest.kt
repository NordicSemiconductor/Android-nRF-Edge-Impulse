package no.nordicsemi.android.ei

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import no.nordicsemi.android.ei.model.Hello
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.MessageWrapper
import org.junit.Test

class MessageWrapperTest {

    private val gson = Gson()

    @Test
    fun initialHandshakeValidator_ReturnsTrue() {
        val expectedResult = "{\n" +
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
                "            }]\n" +
                "            \"supportsSnapshotStreaming\": false\n" +
                "        }\n" +
                "    }\n" +
                "}"
        val actualResult = gson.toJson(MessageWrapper(message = Message(deviceMessage = Hello())))
        assertThat(expectedResult == actualResult).isTrue()
    }
}