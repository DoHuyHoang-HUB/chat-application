package com.example.chatapplication.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.model.ChatMessage

const val VIEW_TYPE_SENT = 1
const val VIEW_TYPE_RECEIVED = 2

class ChatAdapter(
    val chatMessage: List<ChatMessage>,
    var receivedProfileImage: Bitmap?,
    val senderId: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SendMessageViewHolder(view: View?): RecyclerView.ViewHolder(view!!) {
        val textMessage: TextView = view!!.findViewById(R.id.textMessage)
        val textDateTime: TextView = view!!.findViewById(R.id.textDateTime)
    }

    class ReceivedMessageViewHolder(view: View?): RecyclerView.ViewHolder(view!!) {
        val textMessage: TextView = view!!.findViewById(R.id.textMessage)
        val textDateTime: TextView = view!!.findViewById(R.id.textDateTime)
        val receivedProfileImage: ImageView = view!!.findViewById(R.id.imageProfile)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            return SendMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_container_send_message, parent, false))
        } else {
            return ReceivedMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_container_received_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessage[position]
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            val holderNew = holder as SendMessageViewHolder
            holderNew.textMessage.text = message.message
            holderNew.textDateTime.text = message.dateTime
        } else {
            val holderNew = holder as ReceivedMessageViewHolder
            holderNew.textMessage.text = message.message
            holderNew.textDateTime.text = message.dateTime
            if (receivedProfileImage != null) {
                holderNew.receivedProfileImage.setImageBitmap(receivedProfileImage)
            }
        }
    }

    override fun getItemCount(): Int = chatMessage.size

    override fun getItemViewType(position: Int): Int {
        if (chatMessage.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }

    fun setReceiverProfileImage(bitmap: Bitmap) {
        receivedProfileImage = bitmap
    }

}