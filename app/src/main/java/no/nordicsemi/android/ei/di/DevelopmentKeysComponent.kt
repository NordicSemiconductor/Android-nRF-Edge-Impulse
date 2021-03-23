package no.nordicsemi.android.ei.di

import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ViewModelComponent
import no.nordicsemi.android.ei.model.DevelopmentKeys

@DevelopmentKeysScope
@DefineComponent(parent = ViewModelComponent::class)
interface DevelopmentKeysComponent {

    @DefineComponent.Builder
    interface Builder {
        fun setDevelopmentKeys(@BindsInstance keys: DevelopmentKeys): Builder
        fun build(): DevelopmentKeysComponent
    }
}