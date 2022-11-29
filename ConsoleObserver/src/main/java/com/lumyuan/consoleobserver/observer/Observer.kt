package com.lumyuan.consoleobserver.observer

interface Observer<T> {
    fun onChanged(t: T?)
}