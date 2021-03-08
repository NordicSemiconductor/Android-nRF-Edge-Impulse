package no.nordicsemi.android.ei.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.ei.eiservice.LoginBody
import no.nordicsemi.android.ei.repository.NrfEiRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: NrfEiRepository
) : ViewModel() {

    fun login(username: String, password: String) {
        repo.eiService.login(loginBody = LoginBody(username = username, password = password))
            .enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.i("SS", "Response: $response")
                    Log.i("SS", "Response: $response")
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
    }

}