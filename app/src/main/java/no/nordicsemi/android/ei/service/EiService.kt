package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.service.param.*
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
    suspend fun getCurrentUser(@Header("x-jwt-token") jwt: String): User

    /**
     * Create a new project.
     * @param jwt Token received during the login.
     * @see <a href="https://docs.edgeimpulse.com/reference#createproject">Docs: Create new project</a>
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/projects/create")
    suspend fun createProject(
        @Header("x-jwt-token") jwt: String,
        @Body createProjectRequest: CreateProjectRequest
    ): CreateProjectResponse

    /**
     * Retrieve the development API and HMAC keys for a project. These keys are specifically marked
     * to be used during development. These keys can be undefined if no development keys are set.
     *
     * @param jwt       Token received during the login.
     * @param projectId Project ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/devkeys")
    suspend fun developmentKeys(
        @Header("x-jwt-token") jwt: String,
        @Path("projectId") projectId: Int
    ): DevelopmentKeysResponse

    /**
     * Retrieve all the devices for a project. Devices get included
     * here if they connect to the remote management API or if they have sent data to the ingestion API
     * and had the device_id field set.
     *
     * @param apiKey       Token received during the login.
     * @param projectId Project ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/devices")
    suspend fun listDevices(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int
    ): ListDevicesResponse

    /**
     * Retrieve all the samples for a project.
     *
     * @param apiKey       Token received during the login.
     * @param projectId Project ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/raw-data")
    suspend fun listSamples(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Query("category") category: String
    ): ListSamplesResponse

    /**
     * Retrieve all the samples for a project.
     *
     * @param apiKey       Token received during the login.
     * @param projectId Project ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/raw-data")
    suspend fun listSamples(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Query("category") category: String = "training",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ListSamplesResponse

}