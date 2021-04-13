package no.nordicsemi.android.ei.model

data class Collaborator(
    val id: Int,
    val username: String,
    val photo: String,
    val isEdgeImpulseStaff: Boolean,
    val success: Boolean,
    val error: String
)