package com.example.whatsappclone

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val UID="uid"
const val NAME="name"
const val IMAGE="photo"

class ChatActivity : AppCompatActivity() {
    lateinit var nameTv:TextView
    lateinit var userImgViewChat:ShapeableImageView
    lateinit var currentUser:User
    lateinit var msgEdtv:EmojiEditText
    lateinit var sendBtn:ImageView
    lateinit var friendId: String
    lateinit var name:String
    lateinit var image:String
    lateinit var msgRv:RecyclerView
    lateinit var rootView:RelativeLayout
    lateinit var smileBtn:ImageView
    lateinit var swipeToLoad:SwipeRefreshLayout
    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private val mCurrentId:String=FirebaseAuth.getInstance().uid!!
    private val db:FirebaseDatabase =FirebaseDatabase.getInstance()
    private val messages= mutableListOf<ChatEvent>()
    lateinit var chatAdapter:ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)
        msgEdtv=findViewById(R.id.msgEdtv)
        sendBtn=findViewById(R.id.sendBtn)
        smileBtn=findViewById(R.id.smileBtn)
        msgRv=findViewById(R.id.msgRv)
        rootView=findViewById(R.id.rootView)
        swipeToLoad=findViewById(R.id.swipeToLoad)
        keyboardVisibilityHelper = KeyboardVisibilityUtil(rootView) {
            msgRv.scrollToPosition(mutableItems.size - 1)
        }
        FirebaseFirestore.getInstance().collection("users").document(mCurrentId).get()
            .addOnSuccessListener {
                currentUser=it.toObject(User::class.java)!!
            }
        chatAdapter=ChatAdapter(messages,mCurrentId)
        msgRv.apply{
            layoutManager=LinearLayoutManager(this@ChatActivity)
            adapter=chatAdapter
        }
        nameTv=findViewById(R.id.nameTv)
        userImgViewChat=findViewById(R.id.userImgView)
        friendId =  intent.getStringExtra(UID).toString()
        name = intent.getStringExtra(NAME).toString()
        image = intent.getStringExtra(IMAGE).toString()
        nameTv.text=name
        if (image.isNotEmpty()) {
            Picasso.get().load(image).into(userImgViewChat)
        } else {
            // Handle the case where the image path is empty or null
        }
        val emojiPopup =EmojiPopup(rootView,msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        swipeToLoad.setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                swipeToLoad.isRefreshing = false
            }
        }
        listenToMessages(){ msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }
        sendBtn.setOnClickListener {
            msgEdtv.text?.let{
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }

        markAsRead()
    }

    private fun updateMessage(msg: Message) {
        val position = messages.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }

        if (position != -1) {
            messages[position] = msg
            chatAdapter.notifyItemChanged(position)
        } else {
            // Message not found, it might be a new message
            addMessage(msg)
        }
    }


    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun listenToMessages(newMsg: (msg: Message, update: Boolean) -> Unit){
        getMessages(friendId).orderByKey().addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg=snapshot.getValue(Message::class.java)!!
                newMsg(msg,false)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val msg=snapshot.getValue(Message::class.java)!!
                newMsg(msg,true)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addMessage(msg: Message?) {
        val eventBefore=messages.lastOrNull()
        if((eventBefore!=null && !eventBefore.sentAt.isSameDayAs(msg!!.sentAt)) ||eventBefore==null){
            messages.add(
                DateHeader(msg!!.sentAt,context=this)
            )
        }
        messages.add(msg!!)
        chatAdapter.notifyItemInserted(messages.size-1)
        msgRv.scrollToPosition(messages.size-1)
    }

    private fun sendMessage(msg: String) {
        val id=getMessages(friendId = friendId).push().key
        checkNotNull(id)
        val msgMap=Message(msg,mCurrentId,id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {

        }
        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
        val inboxMap = Inbox(
            message.msg,
            friendId,
            name,
            image,
            message.sentAt,
            0
        )
        getInbox(mCurrentId,friendId).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId,mCurrentId).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count = 1
                    }
                    value?.let {
                        if(it.from==message.senderId){
                            inboxMap.count=value.count+1
                        }
                    }
                    getInbox(friendId,mCurrentId).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
    private fun markAsRead() {
        getInbox(mCurrentId, friendId).child("count").setValue(0)
    }


    private fun getMessages(friendId: String)=db.reference.child("messages/${getId(friendId)}")
    private fun getInbox(toUser:String,fromUser:String)=db.reference.child("chats/$toUser/$fromUser")
    private fun getId(friendId:String):String{
        //ID for messages
        return if(friendId>mCurrentId){
            mCurrentId+friendId
        }else{
            friendId+mCurrentId
        }
    }

}