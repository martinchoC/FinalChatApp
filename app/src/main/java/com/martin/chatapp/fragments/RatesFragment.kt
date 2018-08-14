package com.martin.chatapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

import com.martin.chatapp.R
import com.martin.chatapp.adapters.RatesAdapter
import com.martin.chatapp.dialogs.RateDialog
import com.martin.chatapp.extensions.toast
import com.martin.chatapp.models.NewRateEvent
import com.martin.chatapp.models.Rate
import com.martin.chatapp.utils.RxBus
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_rates.view.*
import java.util.EventListener

class RatesFragment : Fragment() {

    private lateinit var _view: View

    private lateinit var adapter: RatesAdapter
    private val ratesList: ArrayList<Rate> = ArrayList()
    private lateinit var scrollListener: RecyclerView.OnScrollListener

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var ratesDBRef: CollectionReference

    private var ratesSubscription: ListenerRegistration? = null
    private lateinit var rateBusListener: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _view = inflater.inflate(R.layout.fragment_rates, container, false)

        setUpRatesDB()
        setUpCurrentUser()

        setUpRecyclerView()
        setUpFloatingActionButton()

        subscribeToRatings()
        subscribeToNewRatings()

        return _view
    }

    private fun setUpRatesDB() {
        //if collection is not created, creates it
        ratesDBRef = store.collection("rates")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!! //!! is not null
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        adapter = RatesAdapter(ratesList)
        _view.recyclerView.setHasFixedSize(true)
        _view.recyclerView.layoutManager = layoutManager
        _view.recyclerView.adapter = adapter

        scrollListener = object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy>0 || dy<0 && _view.fabRating.isShown)
                    _view.fabRating.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    _view.fabRating.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }

        _view.recyclerView.addOnScrollListener(scrollListener)
    }

    private fun setUpFloatingActionButton() {                       //FloatingActionButton = Fab
        _view.fabRating.setOnClickListener { RateDialog().show(fragmentManager, "") }
    }

    private fun hasUserRated(rates: ArrayList<Rate>): Boolean {
        var result = false
        rates.forEach {
            if (it.userId == currentUser.uid) {
                result = true
            }
        }
        return result
    }

    private fun removeFABIfRated(rated: Boolean) {
        if(rated) {
            _view.fabRating.hide()
            _view.recyclerView.removeOnScrollListener(scrollListener)
        }
    }

    private fun saveRate(rate: Rate){
        val newRating = HashMap<String, Any>()
        newRating["userId"] = rate.userId
        newRating["text"] = rate.text
        newRating["rate"] = rate.rate
        newRating["createdAt"] = rate.createdAt
        newRating["profileImgURL"] = rate.profileImgURL

        ratesDBRef.add(newRating)
                .addOnCompleteListener {
                    activity!!.toast("Rating added!!!")
                }
                .addOnFailureListener {
                    activity!!.toast("Rating error, try again!")
                }
    }

    private fun subscribeToRatings() {
        ratesSubscription = ratesDBRef
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(object : EventListener, com.google.firebase.firestore.EventListener<QuerySnapshot> {
                    override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                        exception?.let {
                            activity!!.toast("Exception!")
                            return
                        }

                        snapshot?.let {
                            ratesList.clear()
                            val rates = it.toObjects(Rate::class.java)
                            ratesList.addAll(rates)
                            removeFABIfRated(hasUserRated(ratesList))
                            adapter.notifyDataSetChanged()
                            _view.recyclerView.smoothScrollToPosition(0)
                        }
                    }
                })
    }

    private fun subscribeToNewRatings() {
        rateBusListener = RxBus.listen(NewRateEvent ::class.java).subscribe{
                            saveRate(it.rate)
                        }
    }

    override fun onDestroyView() {
        _view.recyclerView.removeOnScrollListener(scrollListener)
        rateBusListener.dispose()
        ratesSubscription?.remove()
        super.onDestroyView()
    }
}