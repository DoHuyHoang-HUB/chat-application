package com.example.chatapplication.model

import java.util.*

data class ChatMessage(
    var senderId: String,
    var receiverId: String,
    var message: String?,
    var dateTime: String?,
    var dateObject: Date?,
    var conversionId: String?,
    var conversionName: String?,
    var conversionImage: String?
) {
    constructor(senderId: String, receiverId: String, message: String?, dateTime: String, dateObject: Date?)
            : this(senderId, receiverId, message, dateTime, dateObject, null, null , null)

    constructor(senderId: String, receiverId: String): this(senderId, receiverId, null, null, null , null, null, null)
}