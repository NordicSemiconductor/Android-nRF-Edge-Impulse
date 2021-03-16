package no.nordicsemi.android.ei.model

data class Project(
    val id: Int,
    val name: String,
    val description: String,
    val created: String,
    val owner: String,
    val logo: String?,
)