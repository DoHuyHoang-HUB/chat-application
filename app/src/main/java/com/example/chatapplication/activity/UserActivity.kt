package com.example.chatapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatapplication.adapter.UsersAdapter
import com.example.chatapplication.databinding.ActivityUserBinding
import com.example.chatapplication.model.User
import com.example.chatapplication.utilities.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUser()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }

    private fun getUser() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager.getString(KEY_USER_ID)
                if(task.isSuccessful && task.result != null) {
                    val users = arrayListOf<User>()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result!!) {
                        if(currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }
                        val user = User(
                            queryDocumentSnapshot.getString(KEY_NAME)!!,
                            queryDocumentSnapshot.getString(KEY_EMAIL)!!,
                            queryDocumentSnapshot.getString(KEY_IMAGE)!!,
                            queryDocumentSnapshot.getString(KEY_FCM_TOKEN)!!
                        )
                        users.add(user)
                    }

                    if(users.size > 0) {
                        val usersAdapter = UsersAdapter(users)
                        binding.userRecyclerView.adapter = usersAdapter
                        binding.userRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressbar.visibility = View.VISIBLE
        } else {
            binding.progressbar.visibility = View.INVISIBLE
        }
    }
}