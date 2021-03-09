package no.nordicsemi.android.ei.service.param

/**
 * Body parameters for "Get JWT token" request.
 *
 * @see <a href="https://docs.edgeimpulse.com/reference#login-1">Docs: Get JWT token</a>
 */
data class LoginRequest(
    /** Username or e-mail address. */
    val username: String,
    /** Password. */
    val password: String,
    /** Evaluation user UUID. */
    val uuid: String?
)
