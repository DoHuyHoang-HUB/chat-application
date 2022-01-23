package com.example.chatapplication.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.R
import com.example.chatapplication.listeners.ConversionListener
import com.example.chatapplication.model.ChatMessage
import com.example.chatapplication.model.User

class RecentConversationsAdapter(val chatMessages: List<ChatMessage>, val conversionListener: ConversionListener): RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>() {

    class ConversionViewHolder(view: View?): RecyclerView.ViewHolder(view!!) {
        val imageProfile: ImageView = view!!.findViewById(R.id.imageProfile)
        val textName: TextView = view!!.findViewById(R.id.textName)
        val textRecentMessage: TextView = view!!.findViewById(R.id.textRecentMessage)
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val adapter = LayoutInflater.from(parent.context).inflate(R.layout.item_container_recent_conversion, parent, false)
        return ConversionViewHolder(adapter)
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage.toString()))
        holder.textName.text = chatMessage.conversionName
        holder.textRecentMessage.text = chatMessage.message
        holder.itemView.setOnClickListener {
            val user = User(
                chatMessage.conversionName.toString(),
                chatMessage.conversionImage.toString(),
                null,
                null,
                chatMessage.conversionId.toString()
            )
            conversionListener.onConversionClicked(user)
        }
    }

    override fun getItemCount(): Int = chatMessages.size
}