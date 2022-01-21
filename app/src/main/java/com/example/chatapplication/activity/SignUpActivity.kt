package com.example.chatapplication.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatapplication.databinding.ActivitySignUpBinding
import com.example.chatapplication.utilities.*
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener { onBackPressed() }
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetail()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val users = hashMapOf<String, Any>()
        users.put(KEY_NAME, binding.inputName.text.toString())
        users.put(KEY_EMAIL, binding.inputEmail.text.toString())
        users.put(KEY_PASSWORD, binding.inputPassword.text.toString())
        encodedImage?.let { users.put(KEY_IMAGE, it) }
        database.collection(KEY_COLLECION_USERS)
            .add(users)
            .addOnSuccessListener {
                loading(false)
                preferenceManager.putBoolean(KEY_IS_SIGN_IN, true)
                preferenceManager.putString(KEY_USER_ID, it.id)
                preferenceManager.putString(KEY_NAME, binding.inputName.text.toString())
                encodedImage?.let { it1 -> preferenceManager.putString(KEY_IMAGE, it1) }
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                loading(false)
                e.message?.let { showToast(it) }
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150;
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                if (it.data != null) {
                    val imageUri: Uri? = it.data!!.data
                    try {
                        val inputStream = contentResolver.openInputStream(imageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        encodedImage = encodeImage(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    )

    private fun isValidSignUpDetail(): Boolean {
        if (encodedImage == null) {
            showToast("Select profile image")
            return false
        } else if(binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid image")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.inputCofirmPassword.text.toString().trim().isEmpty()) {
            showToast("Cofirm your password")
            return false
        } else if (!binding.inputPassword.text.toString().equals(binding.inputCofirmPassword.text.toString())) {
            showToast("Password & comfirm password must be same")
            return false
        }
        return true
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressbar.visibility = View.VISIBLE
        } else {
            binding.progressbar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}