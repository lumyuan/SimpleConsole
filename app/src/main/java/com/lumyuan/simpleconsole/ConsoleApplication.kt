package com.lumyuan.simpleconsole

import android.app.Application
import com.lumyuan.consoleobserver.Console

class ConsoleApplication : Application() {

    companion object {
        lateinit var console: Console
    }

    override fun onCreate() {
        super.onCreate()
        //初始化控制台，控制台默认不打印完整错误调用栈，这里手动设置为true
        console = Console(isFullStackTrace = true)
        //直接获取SU权限
        //console = Console(permission = Permission.SU(), isFullStackTrace = true)
    }

}