package com.lumyuan.consoleobserver.observer

open class LiveData<T> {
    private var vaule: T? = null
    private var observer: Observer<T>? = null
    constructor(value: T){
        this.vaule = value
    }
    constructor()

    fun getValue(): T? {
        return this.vaule
    }

    fun setValue(value: T?){
        this.vaule = value
        observer?.onChanged(value)
    }

    fun observe(observer: Observer<T>?){
        this.observer = observer
    }
}