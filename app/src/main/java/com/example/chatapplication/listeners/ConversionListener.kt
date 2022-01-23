package com.example.chatapplication.listeners

import com.example.chatapplication.model.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}