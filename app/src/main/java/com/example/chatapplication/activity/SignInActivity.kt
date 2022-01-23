package com.example.chatapplication.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatapplication.R
import com.example.chatapplication.databinding.ActivitySignInBinding
import com.example.chatapplication.utilities.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(KEY_IS_SIGN_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textCreateNewAccount.setOnClickListener { startActivity(Intent(applicationContext, SignUpActivity::class.java)) }
        binding.buttonSignIn.setOnClickListener { if (isValidSignInDetails()) signIn() }
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .whereEqualTo(KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.getResult() != null
                    && task.result?.documents!!.size > 0) {
                    val documentSnapshot = task.result?.documents?.get(0)
                    preferenceManager.putBoolean(KEY_IS_SIGN_IN, true)
                    documentSnapshot?.id?.let { preferenceManager.putString(KEY_USER_ID, it) }
                    documentSnapshot?.getString(KEY_NAME)
                        ?.let { preferenceManager.putString(KEY_NAME, it) }
                    documentSnapshot?.getString(KEY_IMAGE)
                        ?.let { preferenceManager.putString(KEY_IMAGE, it) }

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressbar.visibility = View.VISIBLE
        } else {
            binding.progressbar.visibility = View.INVISIBLE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails(): Boolean {
        if(binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        }
        return true
    }

}

