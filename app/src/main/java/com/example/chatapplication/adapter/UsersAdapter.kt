package com.example.chatapplication.adapter

import android.content.Context
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
import com.example.chatapplication.databinding.ItemContainerUserBinding
import com.example.chatapplication.model.User

class UsersAdapter(
    private val dataset: List<User>
): RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val textName: TextView = view!!.findViewById(R.id.textName)
        val textEmail: TextView = view!!.findViewById(R.id.textEmail)
        val imageProfile: ImageView = view!!.findViewById(R.id.imageProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_container_user, parent, false)
        return UserViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = dataset[position]
        holder.textName.text = user.name
        holder.textEmail.text = user.email
        holder.imageProfile.setImageBitmap(getUserImage(user.image))
    }

    override fun getItemCount(): Int = dataset.size

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}