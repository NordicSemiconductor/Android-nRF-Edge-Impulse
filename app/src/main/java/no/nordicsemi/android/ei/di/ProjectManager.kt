package no.nordicsemi.android.ei.di

import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import javax.inject.Inject

@LoggedUserScope
class ProjectManager @Inject constructor(private val builder: ProjectComponent.Builder) {

    /**
     *  ProjectComponent is specific to a logged in user's projects. Holds an instance of
     *  ProjectComponent.
     */
    var projectComponent: ProjectComponent? = null
        private set

    fun projectSelected(project: Project, keys: DevelopmentKeys) {
        // a logged in user can call this to select a project.
        projectComponent = builder.setProject(project).setKeys(keys).build()
    }
}