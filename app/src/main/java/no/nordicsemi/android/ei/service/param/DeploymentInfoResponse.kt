package no.nordicsemi.android.ei.service.param

/**
 * Response to get deployment info
 * @see https://docs.edgeimpulse.com/reference#getdeployment
 */
data class DeploymentInfoResponse(
    val success: Boolean,
    val error: String?,
    val hasDeployment: Boolean,
    val version: Int
)