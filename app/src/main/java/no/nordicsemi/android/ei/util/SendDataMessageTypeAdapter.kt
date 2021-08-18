package no.nordicsemi.android.ei.util

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import no.nordicsemi.android.ei.model.DataSample
import java.lang.reflect.Type
import java.security.InvalidParameterException

class SendDataMessageTypeAdapter : JsonDeserializer<DataSample> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DataSample {
        json?.asJsonObject?.let { root ->
            try {
                // Check if the element exists before checking if it's a primitive
                Log.d("AAAA", "Trying to deserialize sample data")
                when {
                    root.has("type") -> {
                        return context!!.deserialize(root, DataSample::class.java)
                    }
                    else -> {
                        throw InvalidParameterException("Type not supported: $typeOfT")
                    }
                }
            } catch (ex: Exception) {
                Log.e("AAAA", "Something went wrong: ${ex.message}")
            }
        }
        throw InvalidParameterException("Type not supported: $typeOfT")
    }
}