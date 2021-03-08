package no.nordicsemi.android.ei.eiservice

import android.telecom.Call
import retrofit2.http.POST

interface EiService {

    @POST("api-login")
    fun login()

}