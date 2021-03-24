package no.nordicsemi.android.ei.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import no.nordicsemi.android.ei.repository.ProjectRepository

@EntryPoint
@InstallIn(DevelopmentKeysComponent::class)
interface DevelopmentKeysEntryPoint {
    fun projectRepository(): ProjectRepository
}