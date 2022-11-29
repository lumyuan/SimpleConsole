package com.lumyuan.simpleconsole

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * 自定义函数，在Activity中快速创建ViewBinding
 */
inline fun <VB : ViewBinding> AppCompatActivity.binding(
    crossinline inflate: (LayoutInflater) -> VB
) = lazy{
    inflate(layoutInflater).apply {
        setContentView(this.root)
    }
}