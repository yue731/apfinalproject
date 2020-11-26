package edu.utap.sharein.model

data class User (
        var name: String = "",
        var email: String = "",
        var uid: String = "",
        var postsList: List<String> = listOf(),
        var likedPosts: List<String> = listOf(),
        var following: List<String> = listOf(),
        var isUserNameSet: Boolean = false,
        var profilePhotoUUID: String = ""
)