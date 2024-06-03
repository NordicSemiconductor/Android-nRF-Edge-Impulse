/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ei.account.AccountHelper
import no.nordicsemi.android.ei.di.UserComponentEntryPoint
import no.nordicsemi.android.ei.di.UserManager
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.repository.DashboardRepository
import no.nordicsemi.android.ei.repository.UserDataRepository
import no.nordicsemi.android.ei.util.guard
import no.nordicsemi.android.ei.viewmodels.state.DeleteState
import javax.inject.Inject

@HiltViewModel
class DeleteUserViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userManager: UserManager,
    private val dashboardRepository: DashboardRepository,
) : AndroidViewModel(context as Application) {
    private var _state = MutableStateFlow<DeleteState>(DeleteState.NotDeleted)
    val state = _state.asStateFlow()

    var user: User by mutableStateOf(userDataRepo.user)
        private set

    // TODO This needs to be fixed: Possible NPE when switching back to the app.
    private val userDataRepo: UserDataRepository
        get() = EntryPoints
            .get(userManager.userComponent!!, UserComponentEntryPoint::class.java)
            .userDataRepository()

    fun deleteUser(password: String, code: String?) {
        _state.value = DeleteState.Deleting
        val handler = CoroutineExceptionHandler { _, throwable ->
            viewModelScope
                .launch {
                    _state.value = DeleteState.Error(throwable)
                }
        }
        viewModelScope.launch(handler) {
            val response = dashboardRepository.deleteCurrentUser(
                token = userDataRepo.token,
                password = password,
                code = code
            )
            guard(response.success) {
                throw Throwable(response.error)
            }
            _state.value = DeleteState.Deleted
            handleLogout()
        }
    }
    private fun handleLogout() {
        AccountHelper.invalidateAuthToken(userDataRepo.token, getApplication())
        userManager.logout()
    }
}