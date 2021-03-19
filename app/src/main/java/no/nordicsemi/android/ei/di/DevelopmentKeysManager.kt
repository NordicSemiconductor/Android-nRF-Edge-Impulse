package no.nordicsemi.android.ei.di

import no.nordicsemi.android.ei.model.DevelopmentKeys
import javax.inject.Inject
import javax.inject.Provider

class DevelopmentKeysManager @Inject constructor(
    // Since DevelopmentKeysManager will be in charge of managing the DevelopmentKeyComponent's
    // lifecycle, it needs to know how to create an instances of it. We use the provider (i.e. factory)
    // Dagger generates for us to create instances of ProjectManager.
    private val developmentKeysComponentProvider: Provider<DevelopmentKeysComponent.Builder>,
) {
    /**
     *  DevelopmentKeysComponent is specific to a project selected. Each project has an api key and a hmac key.
     *  These keys will be fetched when the user selects a particular project.
     */
    private lateinit var developmentKeysComponent: DevelopmentKeysComponent

    fun onProjectSelected(developmentKeys: DevelopmentKeys) {
        // When the user selects a project, we create a new instance of the DevelopmentKeysComponent.
        developmentKeysComponent =
            developmentKeysComponentProvider.get()
                .setDevelopmentKeys(keys = developmentKeys).build()
    }

}