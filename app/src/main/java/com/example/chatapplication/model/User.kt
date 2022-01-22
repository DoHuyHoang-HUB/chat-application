package com.example.chatapplication.model

import java.io.Serializable

data class User(
    val name: String,
    val image: String,
    val email: String,
    val token: String?
): Serializable