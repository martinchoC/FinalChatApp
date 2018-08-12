package com.martin.chatapp.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object RxBus {

    private val publisher = PublishSubject.create<Any>()

    //who is subscribed or listening events will receive this value
    fun publish(event: Any) {
        publisher.onNext(event)
    }

    //we call listen with a class (totalMessagesEvent HERE)
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

}