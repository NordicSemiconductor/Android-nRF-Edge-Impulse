package no.nordicsemi.android.ei.repository

import no.nordicsemi.android.ei.di.JwtToken
import no.nordicsemi.android.ei.di.LoggedUserScope
import no.nordicsemi.android.ei.model.User
import javax.inject.Inject

@LoggedUserScope
class UserDataRepository @Inject constructor(
    @JwtToken val token: String,
    val user: User,
)