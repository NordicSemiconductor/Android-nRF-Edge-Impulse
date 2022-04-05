package no.nordicsemi.android.ei.di

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ei.model.User

@LoggedUserScope
@DefineComponent(parent = SingletonComponent::class)
interface UserComponent {

    @DefineComponent.Builder
    interface Builder {
        fun setUser(@BindsInstance user: User): Builder
        fun setToken(@BindsInstance string: String): Builder
        fun build(): UserComponent
    }
}
