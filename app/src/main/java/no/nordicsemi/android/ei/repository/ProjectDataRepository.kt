package no.nordicsemi.android.ei.repository


import no.nordicsemi.android.ei.di.ProjectScope
import no.nordicsemi.android.ei.model.Project
import javax.inject.Inject

@ProjectScope
class ProjectDataRepository @Inject constructor(
    val project: Project
)