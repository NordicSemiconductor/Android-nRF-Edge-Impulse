package no.nordicsemi.android.ei.di

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project

@ProjectScope
@DefineComponent(parent = UserComponent::class)
interface ProjectComponent {

    @DefineComponent.Builder
    interface Builder {
        fun setProject(@BindsInstance project: Project): Builder
        fun setKeys(@BindsInstance keys: DevelopmentKeys): Builder
        fun build(): ProjectComponent
    }
}