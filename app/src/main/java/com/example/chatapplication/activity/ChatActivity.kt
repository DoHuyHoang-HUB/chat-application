package com.example.chatapplication.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapplication.adapter.ChatAdapter
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.model.ChatMessage
import com.example.chatapplication.model.User
import com.example.chatapplication.network.Api
import com.example.chatapplication.utilities.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private var receiverUser: User? = null
    private val chatMessages = arrayListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversionId: String? = null
    private var isReceiverAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadReceiverDetail()
        init()
        setListeners()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitmapFromEncodedString(receiverUser!!.image),
            preferenceManager.getString(KEY_USER_ID)!!
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = hashMapOf<String, Any>()
        message.put(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID)!!)
        message.put(KEY_RECEIVER_ID, receiverUser?.id!!)
        message.put(KEY_MESSAGE, binding.inputMessage.text.toString())
        message.put(KEY_TIMESTAMP, Date())
        database.collection(KEY_COLLECTION_CHAT).add(message)
        if (conversionId != null) {
            updateConversion(binding.inputMessage.text.toString())
        } else {
            val conversion = hashMapOf<String, Any>()
            conversion.put(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID)!!)
            conversion.put(KEY_SENDER_NAME, preferenceManager.getString(KEY_NAME)!!)
            conversion.put(KEY_SENDER_IMAGE, preferenceManager.getString(KEY_IMAGE)!!)
            conversion.put(KEY_RECEIVER_ID, receiverUser?.id!!)
            conversion.put(KEY_RECEIVER_NAME, receiverUser?.name!!)
            conversion.put(KEY_RECEIVER_IMAGE, receiverUser?.image!!)
            conversion.put(KEY_LAST_MESSAGE, binding.inputMessage.text.toString())
            conversion.put(KEY_TIMESTAMP, Date())
            addConversion(conversion)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser?.token)

                val data = JSONObject()
                data.put(KEY_USER_ID, preferenceManager.getString(KEY_USER_ID))
                data.put(KEY_NAME, preferenceManager.getString(KEY_NAME))
                data.put(KEY_FCM_TOKEN, preferenceManager.getString(KEY_FCM_TOKEN))
                data.put(KEY_MESSAGE, binding.inputMessage.text.toString())

                val body = JSONObject()
                body.put(REMOTE_MSG_DATA, data)
                body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
        binding.inputMessage.text = null
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        val callback = object: Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body())
                            val results = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error = results.get(0) as JSONObject
                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfully")
                } else {
                    showToast("Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        }
        Api.retrofitService.sendMessage(
            Constants.getRemoteMsgHeaders(),
            messageBody
        ).enqueue(callback)
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(KEY_COLLECTION_USERS).document(
            receiverUser?.id!!
        ).addSnapshotListener(this, { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                if (value.getLong(KEY_AVAILABILITY) != null) {
                    val availability = Objects.requireNonNull(
                        value.getLong(KEY_AVAILABILITY)
                    )?.toInt()
                    isReceiverAvailable = availability == 1
                }
                receiverUser!!.token = value?.getString(KEY_FCM_TOKEN)
                if (receiverUser!!.image == null) {
                    receiverUser!!.image = value.getString(KEY_NAME).toString()
                    getBitmapFromEncodedString(receiverUser!!.image)?.let {
                        chatAdapter.setReceiverProfileImage(
                            it
                        )
                    }
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size)
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.visibility = View.VISIBLE
            } else {
                binding.textAvailability.visibility = View.GONE
            }

        })
    }

    private fun listenMessages() {
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .whereEqualTo(KEY_RECEIVER_ID, receiverUser?.id)
            .addSnapshotListener(eventListeners)
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, receiverUser?.id)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListeners)
    }

    private val eventListeners: EventListener<QuerySnapshot> = EventListener { value, error ->
        if(error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMessages.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage(
                        documentChange.document.getString(KEY_SENDER_ID)!!,
                        documentChange.document.getString(KEY_RECEIVER_ID)!!,
                        documentChange.document.getString(KEY_MESSAGE),
                        getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP)),
                        documentChange.document.getDate(KEY_TIMESTAMP)
                    )
                    chatMessages.add(chatMessage)
                }
            }
            Collections.sort(chatMessages, kotlin.Comparator { obj1, obj2 -> obj1.dateObject!!?.compareTo(obj2.dateObject) })
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeChanged(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressbar.visibility = View.GONE
        if (conversionId == null) {
            checkForConversion()
        }
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap? {
        if (encodedImage != null) {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            return null
        }
    }

    private fun loadReceiverDetail() {
        receiverUser = intent.getSerializableExtra(KEY_USER) as User?
        binding.textName.text = receiverUser?.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date?): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any>) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { documentReference ->
                conversionId = documentReference.id
            }
    }

    private fun updateConversion(message: String) {
        val documentReference =
            database.collection(KEY_COLLECTION_CONVERSATIONS).document(conversionId.toString())
        documentReference.update(
            KEY_LAST_MESSAGE, message,
            KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversion() {
        if(chatMessages.size != 0) {
            checkForConversionRemotely(
                preferenceManager.getString(KEY_USER_ID),
                receiverUser!!.id
            )
            checkForConversionRemotely(
                receiverUser!!.id,
                preferenceManager.getString(KEY_USER_ID)
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String?, receivedId: String?) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID, senderId)
            .whereEqualTo(KEY_RECEIVER_ID, receivedId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener: OnCompleteListener<QuerySnapshot> = OnCompleteListener { task ->
        if (task.isSuccessful && task.result != null && task.result?.documents!!.size > 0) {
            val documentSnapshot = task.result!!.documents.get(0)
            conversionId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}