package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.service.param.LoginRequest
import no.nordicsemi.android.ei.service.param.LoginResponse
import no.nordicsemi.android.ei.service.param.ProjectResponse
import no.nordicsemi.android.ei.service.param.DevelopmentKeysResponse
import retrofit2.Response
import retrofit2.http.*

interface EiService {

    /**
     * Get a JWT token to authenticate with the API.
     *
     * @see <a href="https://docs.edgeimpulse.com/reference#login-1">Docs: Get JWT token</a>
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api-login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    /**
     * Get information about the current user. This function is only available through a JWT token.
     *
     * @see <a href="https://docs.edgeimpulse.com/reference#createuser">Docs: Get current user</a>
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/user")
    suspend fun getCurrentUser(@Header("x-jwt-token") jwt: String): Response<User>

    /**
     * Lists the active projects for a given user.
     * @param jwt Token received during the login.
     * @see <a href="https://docs.edgeimpulse.com/reference#listprojects">Docs: List active projects</a>
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