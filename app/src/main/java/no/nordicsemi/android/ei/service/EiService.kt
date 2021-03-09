package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.service.param.LoginRequest
import no.nordicsemi.android.ei.service.param.LoginResponse
import no.nordicsemi.android.ei.service.param.ProjectResponse
import retrofit2.http.*

interface EiService {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api-login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/projects")
    suspend fun projects(@Header("cookie") jwt: String): ProjectResponse

}