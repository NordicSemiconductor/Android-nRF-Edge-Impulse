package no.nordicsemi.android.ei.repository


import no.nordicsemi.android.ei.di.ProjectScope
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.SocketToken
import javax.inject.Inject

@ProjectScope
class ProjectDataRepository @Inject constructor(
    val project: Project,
    val developmentKeys: DevelopmentKeys,
    val socketToken: SocketToken
)