package com.lumyuan.consoleobserver.observer

@FunctionalInterface
interface Observer<T> {
    fun onChanged(t: T?)
}