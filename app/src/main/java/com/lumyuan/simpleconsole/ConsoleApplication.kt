package com.lumyuan.simpleconsole

import android.app.Application
import com.lumyuan.consoleobserver.Console

class ConsoleApplication : Application() {

    companion object {
        lateinit var console: Console
    }

    override fun onCreate() {
        super.onCreate()
        console = Console()
        //直接获取SU权限
        //console = Console(Permission.SU())
    }

}