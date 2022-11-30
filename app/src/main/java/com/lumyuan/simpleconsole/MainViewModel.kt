package com.lumyuan.simpleconsole

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    val log = MutableLiveData<String>()

}