package com.martin.chatapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.martin.chatapp.R
import com.martin.chatapp.adapters.ChatAdapter
import com.martin.chatapp.extensions.toast
import com.martin.chatapp.models.Message
import com.martin.chatapp.models.TotalMessagesEvent
import com.martin.chatapp.utils.RxBus
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.*
import java.util.EventListener
import kotlin.collections.HashMap

class ChatFragment : Fragment() {

    private lateinit var _view:View
    private lateinit var adapter: ChatAdapter
    private val messageList: ArrayList<Message> = ArrayList()

    //Creation of an instance of a service
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    //Creation of a variable to set up the service
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var chatDBRef: CollectionReference

    private var chatSubscription: ListenerRegistration? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        _view = inflater.inflate(R.layout.fragment_chat, container, false)

        setUpChatDB()
        setUpCurrentUser()
        setUpRecyclerView()
        setUpChatButton()
        subscribeToChatMessage()

        return _view
    }

    private fun setUpChatDB() {
        //if collection is not created, creates it
        chatDBRef = store.collection("chat")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!! //!! is not null
    }

    private fun setUpRecyclerView() {
        val layourManager = LinearLayoutManager(context)
        adapter = ChatAdapter(messageList, currentUser.uid)

        _view.recyclerView.setHasFixedSize(true)
        _view.recyclerView.layoutManager = layourManager
        _view.recyclerView.itemAnimator = DefaultItemAnimator()
        _view.recyclerView.adapter = adapter
    }

    private fun setUpChatButton() {
        _view.buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            if(messageText.isNotEmpty()){
                val photo = currentUser.photoUrl?.let { currentUser.photoUrl.toString() } ?: run {""}
                val message = Message(currentUser.uid, messageText, photo, Date())
                //Save message in firebase
                saveMessage(message)
                _view.editTextMessage.setText("")
            }
        }

    }

    private fun saveMessage(message: Message){
        val newMessage = HashMap<String, Any>()
        newMessage["authorId"] = message.authorId
        newMessage["message"] = message.message
        newMessage["profileImageURL"] = message.profileImageURL
        newMessage["sentAt"] = message.sentAt

        chatDBRef.add(newMessage)
                .addOnCompleteListener {
                   activity!!.toast("Message added!")
                }
                .addOnFailureListener{
                    activity!!.toast("Message error, try again!")
                }
    }

    private fun subscribeToChatMessage() {
        chatSubscription = chatDBRef
                        .orderBy("sentAt", Query.Direction.DESCENDING) //Order chat by date & time
                        .limit(100)
                        .addSnapshotListener(object: EventListener, com.google.firebase.firestore.EventListener<QuerySnapshot>{
                            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                                exception?.let { // is null, it enters here
                                    activity!!.toast("Exception!")
                                    return
                                }

                                snapshot?.let {
                                    messageList.clear()
                                    val messages = it.toObjects(Message::class.java) //it=snapshot
                                    messageList.addAll(messages.asReversed())
                                    adapter.notifyDataSetChanged()
                                    _view.recyclerView.smoothScrollToPosition(messageList.size)
                                    RxBus.publish(TotalMessagesEvent(messageList.size))
                                }
                            }
                        })
    }

    override fun onDestroyView() {
        chatSubscription?.remove() //help performance
        super.onDestroyView()
    }
}