package no.nordicsemi.android.ei.di

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.SocketToken

@ProjectScope
@DefineComponent(parent = UserComponent::class)
interface ProjectComponent {

    @DefineComponent.Builder
    interface Builder {
        fun setProject(@BindsInstance project: Project): Builder
        fun setKeys(@BindsInstance keys: DevelopmentKeys): Builder
        fun setSocketToken(@BindsInstance keys: SocketToken): Builder
        fun build(): ProjectComponent
    }
}