package com.example.chatapplication.activity

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapplication.adapter.RecentConversationsAdapter
import com.example.chatapplication.databinding.ActivityMainBinding
import com.example.chatapplication.model.ChatMessage
import com.example.chatapplication.utilities.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private val conversations = arrayListOf<ChatMessage>()
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenerConversations()
    }

    private fun init() {
        conversationsAdapter = RecentConversationsAdapter(conversations)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener { signOut() }
        binding.fabNewChat.setOnClickListener { startActivity(Intent(applicationContext, UserActivity::class.java)) }
    }

    private fun loadUserDetails() {
        binding.textName.text = preferenceManager.getString(KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.profileImage.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun listenerConversations() {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener: EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null) {
            return@EventListener
        }
        if(value != null) {
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                    val chatMessage = ChatMessage(senderId!!, receiverId!!)
                    if (preferenceManager.getString(KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.document.getString(
                            KEY_RECEIVER_IMAGE)
                        chatMessage.conversionName = documentChange.document.getString(
                            KEY_RECEIVER_NAME)
                        chatMessage.conversionId = documentChange.document.getString(
                            KEY_RECEIVER_ID
                        )
                    } else {
                        chatMessage.conversionImage = documentChange.document.getString(
                            KEY_SENDER_IMAGE)
                        chatMessage.conversionName = documentChange.document.getString(
                            KEY_SENDER_NAME)
                        chatMessage.conversionId = documentChange.document.getString(KEY_SENDER_ID)
                    }
                    chatMessage.message = documentChange.document.getString(KEY_LAST_MESSAGE)
                    chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                    conversations.add(chatMessage)
                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    for (i in conversations) {
                        val senderId = documentChange.document.getString(KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                        if (i.senderId.equals(senderId) && i.receiverId.equals(receiverId)) {
                            i.message = documentChange.document.getString(KEY_LAST_MESSAGE)
                            i.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                            break
                        }
                    }
                }
            }
            Collections.sort(conversations, { obj1, obj2 -> obj1.dateObject!!?.compareTo(obj2.dateObject) })
            conversationsAdapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.conversationsRecyclerView.visibility = View.VISIBLE
            binding.progressbar.visibility = View.GONE
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { updateToken(it) }
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager.getString(KEY_USER_ID).toString()
        )
        documentReference.update(KEY_FCM_TOKEN, token)
            .addOnFailureListener { showToast("Unable to update token") }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference =
            database.collection(KEY_COLLECTION_USERS).document(
                preferenceManager.getString(KEY_USER_ID).toString()
            )
        val updates = hashMapOf<String, Any>()
        updates.put(KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(application, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { showToast("Unable to sign out") }
    }
}