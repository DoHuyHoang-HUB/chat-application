package com.example.chatapplication.activity

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.example.chatapplication.databinding.ActivityMainBinding
import com.example.chatapplication.utilities.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        getToken()
        setListeners()
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

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { updateToken(it) }
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECION_USERS).document(
            preferenceManager.getString(KEY_USER_ID).toString()
        )
        documentReference.update(KEY_FCM_TOKEN, token)
            .addOnFailureListener { showToast("Unable to update token") }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference =
            database.collection(KEY_COLLECION_USERS).document(
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