package com.lumyuan.consoleobserver

import android.text.Html
import android.text.Spanned
import com.lumyuan.consoleobserver.common.Permission
import com.lumyuan.consoleobserver.observer.LiveData
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import kotlin.collections.ArrayList

/**
 * 默认无权限
 */
class Console(private val permission: Permission = Permission.SH(), private val isFullStackTrace: Boolean = false) {

    private val CONSOLE_TYPE_SUCCESS = 0
    private val CONSOLE_TYPE_ERROR = 1

    private val list: ArrayList<Logcat> = ArrayList()
    private var isSu = permission is Permission.SU

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
                if (isFullStackTrace) {
                    this.message = "程序已退出：" + e.stackTraceToString() + "\n"
                } else {
                    this.message = "程序已退出：$e\n"
                }
            })
            list.add(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                if (isFullStackTrace) {
                    this.message = "程序已退出：" + e.stackTraceToString() + "\n"
                } else {
                    this.message = "程序已退出：$e\n"
                }
            })
        }else {
            errorLiveData.setValue(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                if (isFullStackTrace) {
                    this.message = e.stackTraceToString() + "\n"
                } else {
                    this.message = "$e\n"
                }
            })
            list.add(Logcat().apply {
                this.type = CONSOLE_TYPE_ERROR
                if (isFullStackTrace) {
                    this.message = e.stackTraceToString() + "\n"
                } else {
                    this.message = "$e\n"
                }
            })
        }
    }

    private fun startObserver(type: Int, reader: BufferedReader?) {
        Thread{
            var line: String?
            try {
                while (run { line = reader?.readLine(); line } != null){
                    try{
                        Thread.sleep(10)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    if (type == CONSOLE_TYPE_SUCCESS){
                        successLiveData.setValue(Logcat().apply {
                            this.type = type
                            this.message = "$line\n"
                        })
                    }else {
                        errorLiveData.setValue(Logcat().apply {
                            this.type = type
                            this.message = "$line\n"
                        })
                    }
                    list.add(Logcat().apply {
                        this.type = type
                        this.message = "$line\n"
                    })
                }
            }catch (e: Exception){
                e.printStackTrace()
                if (type == CONSOLE_TYPE_ERROR){
                    catchThrowable(e)
                }else {
                    if (!e.toString().contains("read interrupted")){
                        catchThrowable(e)
                    }
                }
            }
        }.start()
    }

    private fun isSu(cmd: String){
        val split = cmd.split("\n")
        for (it in split){
            if (it.trim() == "su" || it.trim().startsWith("su") || it.contains("su ")){
                this.isSu = true
                break
            }
        }
        for (it in split){
            if (it.trim() == "exit" || it.trim().startsWith("exit") || it.contains(" exit")){
                this.isSu = false
                break
            }
        }
    }

    /**
     * @return is running
     */
    fun execString(cmd: String): Boolean{
        return try {
            writer?.writeBytes(cmd)
            writer?.writeBytes("\n")
            writer?.flush()
            isSu(cmd)
            true
        }catch (e: Exception){
            e.printStackTrace()
            catchThrowable(e)
            false
        }
    }

    /**
     * @return is running
     */
    fun execBytes(cmd: ByteArray): Boolean{
        return try {
            writer?.write(cmd)
            writer?.write("\n".toByteArray())
            writer?.flush()
            isSu(String(cmd))
            true
        }catch (e: Exception){
            e.printStackTrace()
            catchThrowable(e)
            false
        }
    }

    /**
     * 关闭控制台，不会清除日志
     */
    fun destroy(){
        isSu = false
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

    /**
     * 获取控制台权限
     * @return 控制台权限
     */
    fun isSu(): Boolean {
        return isSu
    }

    class Logcat{
        var type = 0
        var message: String? = null
        override fun toString(): String {
            return "$message"
        }
    }
}