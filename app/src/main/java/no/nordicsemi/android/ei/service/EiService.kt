package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.service.param.DevelopmentKeysResponse
import no.nordicsemi.android.ei.service.param.LoginRequest
import no.nordicsemi.android.ei.service.param.LoginResponse
import no.nordicsemi.android.ei.service.param.ProjectResponse
import retrofit2.http.*

interface EiService {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api-login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    /**
     * Lists the active projects for a given user.
     * @param jwt Token received during the login.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/projects")
    suspend fun projects(@Header("cookie") jwt: String): ProjectResponse

    /**
     * Gets the development keys for a project
     * @param jwt       Token received during the login.
     * @param projectId Project ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/devkeys")
    suspend fun developmentKeys(
        @Header("cookie") jwt: String,
        @Path("projectId") projectId: Int
    ): DevelopmentKeysResponse

}