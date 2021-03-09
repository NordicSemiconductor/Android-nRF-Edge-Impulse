package no.nordicsemi.android.ei.service.param

data class LoginResponse(
    val token: String,
    val success: Boolean,
    val error: String?
)
