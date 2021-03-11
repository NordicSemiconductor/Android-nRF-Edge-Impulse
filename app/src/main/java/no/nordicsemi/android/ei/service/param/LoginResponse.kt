package no.nordicsemi.android.ei.service.param

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    val token: String? = null,
    val success: Boolean = false,
    val error: String? = null
) : Parcelable
