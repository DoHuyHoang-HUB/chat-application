package com.example.chatapplication.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.utilities.KEY_AVAILABILITY
import com.example.chatapplication.utilities.KEY_COLLECTION_USERS
import com.example.chatapplication.utilities.KEY_USER_ID
import com.example.chatapplication.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity: AppCompatActivity() {

    private lateinit var documentReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(applicationContext)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(KEY_USER_ID)!!)
    }

    override fun onPause() {
        super.onPause()
        documentReference.update(KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference.update(KEY_AVAILABILITY, 1)
    }
}