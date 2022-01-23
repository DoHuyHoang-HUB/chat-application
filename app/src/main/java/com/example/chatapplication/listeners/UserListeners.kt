package com.example.chatapplication.listeners

import com.example.chatapplication.model.User

interface UserListeners {
    fun onUserClicked(user: User)
}