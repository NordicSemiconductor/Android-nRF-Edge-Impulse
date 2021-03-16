package no.nordicsemi.android.ei.model

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val email: String,
    val photo: String?,
    val created: String,

    // TODO: move those outside: https://stackoverflow.com/questions/37098794/is-it-possible-to-flatten-the-json-hierarchy-with-gson
    val success: Boolean,
    val error: String?
)
