package no.nordicsemi.android.ei.eiservice

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface EiService {
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api-login")
    fun login(@Body loginBody: LoginBody): Call<String>

}