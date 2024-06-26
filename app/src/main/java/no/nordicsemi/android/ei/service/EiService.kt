/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.service

import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.service.param.BuildOnDeviceModelRequest
import no.nordicsemi.android.ei.service.param.BuildOnDeviceModelResponse
import no.nordicsemi.android.ei.service.param.CreateProjectRequest
import no.nordicsemi.android.ei.service.param.CreateProjectResponse
import no.nordicsemi.android.ei.service.param.DeleteCurrentUserRequest
import no.nordicsemi.android.ei.service.param.DeleteCurrentUserResponse
import no.nordicsemi.android.ei.service.param.DeleteDeviceResponse
import no.nordicsemi.android.ei.service.param.DeploymentInfoResponse
import no.nordicsemi.android.ei.service.param.DevelopmentKeysResponse
import no.nordicsemi.android.ei.service.param.GetSocketTokenResponse
import no.nordicsemi.android.ei.service.param.ListDevicesResponse
import no.nordicsemi.android.ei.service.param.ListSamplesResponse
import no.nordicsemi.android.ei.service.param.LoginRequest
import no.nordicsemi.android.ei.service.param.LoginResponse
import no.nordicsemi.android.ei.service.param.RenameDeviceRequest
import no.nordicsemi.android.ei.service.param.RenameDeviceResponse
import no.nordicsemi.android.ei.service.param.StartSamplingRequest
import no.nordicsemi.android.ei.service.param.StartSamplingResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
     * Deletes the current user information about the current user. This function is only available through a JWT token.
     *
     * @see <a href="https://docs.edgeimpulse.com/reference#createuser">Docs: Get current user</a>
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @HTTP(method = "DELETE", path = "api/user", hasBody = true)
    suspend fun deleteCurrentUser(
        @Header("x-jwt-token") jwt: String,
        @Body deleteCurrentUserRequest: DeleteCurrentUserRequest
    ): DeleteCurrentUserResponse

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
     * Sets the current name for a device.
     *
     * @param apiKey                Token received during the login.
     * @param projectId             Project ID.
     * @param deviceId              Device ID
     * @param renameDeviceRequest   Device rename request parameters
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/{projectId}/devices/{deviceId}/rename")
    suspend fun renameDevice(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        //Set value as encoded already as retrofit seem to encode colons with %253A instead of %3A
        @Path("deviceId", encoded = true) deviceId: String,
        @Body renameDeviceRequest: RenameDeviceRequest
    ): RenameDeviceResponse

    /**
     * Deletes a device
     *
     * @param apiKey                Token received during the login.
     * @param projectId             Project ID.
     * @param deviceId              Device ID.
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @DELETE("api/{projectId}/device/{deviceId}")
    suspend fun deleteDevice(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        //Set value as encoded already as retrofit seem to encode colons with %253A instead of %3A
        @Path("deviceId", encoded = true) deviceId: String
    ): DeleteDeviceResponse

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

    /**
     * Retrieve socket token for a project
     *
     * @param apiKey    Token received during the login.
     * @param projectId Project ID
     */
    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/socket-token")
    suspend fun getSocketToken(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
    ): GetSocketTokenResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/{projectId}/device/{deviceId}/start-sampling")
    suspend fun startSampling(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Path("deviceId") deviceId: String,
        @Body startSamplingRequest: StartSamplingRequest,
    ): StartSamplingResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/{projectId}/jobs/build-ondevice-model")
    suspend fun buildOnDevice(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Query("type") type: String = "nordic-thingy53",
        @Body buildOnDeviceModels: BuildOnDeviceModelRequest,
    ): BuildOnDeviceModelResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
    @GET("api/{projectId}/deployment")
    suspend fun deploymentInfo(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Query("type") type: String = "nordic-thingy53"
    ): DeploymentInfoResponse

    //@Streaming
    @Headers("Accept: application/zip")
    @GET("api/{projectId}/deployment/download")
    suspend fun downloadBuild(
        @Header("x-api-key") apiKey: String,
        @Path("projectId") projectId: Int,
        @Query("type") type: String = "nordic-thingy53"
    ): Response<ResponseBody>
}