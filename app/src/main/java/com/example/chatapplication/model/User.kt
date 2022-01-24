package com.example.chatapplication.model

import java.io.Serializable

data class User(
    val name: String,
    var image: String,
    val email: String?,
    var token: String?,
    val id: String?
): Serializable
