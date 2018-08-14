package com.martin.chatapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

import com.martin.chatapp.R
import com.martin.chatapp.extensions.toast
import com.martin.chatapp.models.TotalMessagesEvent
import com.martin.chatapp.utils.CircleTransform
import com.martin.chatapp.utils.RxBus
import com.squareup.picasso.Picasso
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_info.view.*
import java.util.EventListener

class InfoFragment : Fragment() {

    private lateinit var _view: View

    //Creation of an instance of a service
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    //Creation of a variable to set up the service
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var chatDBRef: CollectionReference

    private var chatSubscription: ListenerRegistration? = null
    private lateinit var infoBusListener: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _view = inflater.inflate(R.layout.fragment_info, container, false)

        setUpChatDB()
        setUpCurrentUser()
        setUpCurrentUserInfoUI()


        //Total messages Firebase style
        //subscribeToTotalMessagesFirebaseStyle()

        //Total messages Event Bus + Reactive Style
        subscribeToTotalMessagesEventBusReactiveStyle()

        return _view
    }

    private fun setUpChatDB() {
        //if collection is not created, it creates ir
        chatDBRef = store.collection("chat")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!! //!! is not null
    }

    private fun setUpCurrentUserInfoUI() {
        _view.textViewInfoEmail.text = currentUser.email
        //if user logs in with gmail, name = currentUser.displayName, ELSE(?: elvis operator) R.info_no_name
        _view.textViewInfoName.text = currentUser.displayName?.let { currentUser.displayName } ?: run { getString(R.string.info_no_name) }
        currentUser.photoUrl?. let {
                Picasso.get().load(currentUser.photoUrl).resize(200,200)
                        .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        } ?: run {
            Picasso.get().load(R.drawable.ic_person).resize(300,300)
                    .centerCrop().transform(CircleTransform()).into(_view.imageViewInfoAvatar)
        }
    }

    private fun subscribeToTotalMessagesFirebaseStyle() {
        chatSubscription = chatDBRef.addSnapshotListener(object: EventListener, com.google.firebase.firestore.EventListener<QuerySnapshot>{
                                override fun onEvent(querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                                    exception?.let { // is null, it enters here
                                        activity!!.toast("Exception!")
                                        return
                                    }
                                    querySnapshot?.let {
                                        _view.textViewInfoTotalMessages.text = "${it.size()}"
                                    }
                                }

                            })
    }

    private fun subscribeToTotalMessagesEventBusReactiveStyle() {
        infoBusListener = RxBus.listen(TotalMessagesEvent :: class.java).subscribe {
                            _view.textViewInfoTotalMessages.text = "${it.total}"
                        }
    }

    override fun onDestroyView() {
        infoBusListener.dispose()
        chatSubscription?.remove() //help performance
        super.onDestroyView()
    }

}