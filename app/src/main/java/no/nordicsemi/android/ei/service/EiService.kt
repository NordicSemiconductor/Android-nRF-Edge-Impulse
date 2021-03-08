package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.service.`object`.LoginBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EiService {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api-login")
    suspend fun login(@Body loginBody: LoginBody): String

}