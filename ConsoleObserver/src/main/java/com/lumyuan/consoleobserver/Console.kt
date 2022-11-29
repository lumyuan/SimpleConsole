package com.lumyuan.consoleobserver

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
import com.lumyuan.consoleobserver.common.Permission
import com.lumyuan.consoleobserver.observer.LiveData
import com.lumyuan.consoleobserver.observer.Observer
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 默认无权限
 */
class Console(private val permission: Permission = Permission.SH()) {

    val CONSOLE_TYPE_SUCCESS = 0
    val CONSOLE_TYPE_ERROR = 1

    private val list: ArrayList<Logcat> = ArrayList()

    private var successReader: BufferedReader? = null
    private var errorReader: BufferedReader? = null
    private var writer: DataOutputStream? = null

    private var successInputStreamReader: InputStreamReader? = null
    private var errorInputStreamReader: InputStreamReader? = null

    private var logcat: Spanned = Html.fromHtml("")

    val successLiveData: LiveData<Logcat> = LiveData();
    val errorLiveData: LiveData<Logcat> = LiveData();

    init {
        try {
            initStream()
        }catch (e: Exception){
            e.printStackTrace()
            catchThrowable(e)
        }
    }

    private lateinit var process: Process
    private fun initStream(){
        process = Runtime.getRuntime().exec(permission.toString())

        val inputStream = process.inputStream
        val errorStream = process.errorStream
        val outputStream = process.outputStream

        writer = DataOutputStream(outputStream)

        successInputStreamReader = InputStreamReader(inputStream)
        errorInputStreamReader = InputStreamReader(errorStream)

        successReader = BufferedReader(successInputStreamReader)
        errorReader = BufferedReader(errorInputStreamReader)

        startObserver(CONSOLE_TYPE_SUCCESS, successReader)
        startObserver(CONSOLE_TYPE_ERROR, errorReader)
    }

    private fun catchThrowable(e: Throwable){
        if (e.toString().contains("read interrupted")) {
            errorLiveData.setValue(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                this.message = "程序已退出：$e"
            })
            list.add(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                this.message = "程序已退出：$e"
            })
        }else {
            errorLiveData.setValue(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                this.message = e.toString()
            })
            list.add(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                this.message = e.toString()
            })
        }
    }

    private fun startObserver(type: Int, reader: BufferedReader?) {
        Thread{
            var line: String?
            try {
                while (run { line = reader?.readLine(); line } != null){
                    if (type == CONSOLE_TYPE_SUCCESS){
                        successLiveData.setValue(Logcat().apply {
                            this.type = type
                            this.message = line
                        })
                    }else {
                        errorLiveData.setValue(Logcat().apply {
                            this.type = type
                            this.message = line
                        })
                    }
                    list.add(Logcat().apply {
                        this.type = type
                        this.message = line
                    })
                }
            }catch (e: Exception){
                e.printStackTrace()
                catchThrowable(e)
            }
        }.start()
    }

    fun execString(cmd: String){
        try {
            writer?.writeBytes(cmd)
            writer?.writeBytes("\n")
            writer?.flush()
        }catch (e: Exception){
            e.printStackTrace()
            catchThrowable(e)
        }
    }

    fun execBytes(cmd: ByteArray){
        try {
            writer?.write(cmd)
            writer?.write("\n".toByteArray())
            writer?.flush()
        }catch (e: Exception){
            e.printStackTrace()
            catchThrowable(e)
        }
    }

    /**
     * 关闭控制台，不会清除日志
     */
    fun destroy(){
        try {
            process.destroy()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 清除日志，不会关闭控制台
     */
    fun clear(){
        list.clear()
        logcat = Html.fromHtml("")
    }

    private fun getText(type: Int?, value: String?): String {
        return if (type == CONSOLE_TYPE_SUCCESS){
            "<font color='green'>$value</font><br />"
        }else {
            "<font color='red'>$value</font><br />"
        }
    }

    /**
     * 日志对象转高亮文字
     */
    fun getSpanned(logcat: Logcat?): Spanned{
        return Html.fromHtml(getText(logcat?.type, logcat?.message))
    }

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 获取日志
     */
    fun getLogText(): String{
        val stringBuilder = StringBuilder()
        list.forEach {
            stringBuilder.append(it).append("\n")
        }
        return stringBuilder.toString()
    }

    /**
     * 获取高亮日志
     */
    fun getLogSpanned(): Spanned {
        val stringBuilder = StringBuilder()
        list.forEach {
            val text = getText(it.type, it.message)
            stringBuilder.append(text)
        }
        return Html.fromHtml(stringBuilder.toString())
    }

    /**
     * 重启控制台，不清除日志
     */
    fun restart(){
        destroy()
        initStream()
    }

    class Logcat{
        var type = 0
        var message: String? = null
        override fun toString(): String {
            return "$message"
        }
    }
}