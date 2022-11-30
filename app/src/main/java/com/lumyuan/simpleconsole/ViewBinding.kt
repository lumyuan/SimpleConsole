package com.lumyuan.simpleconsole

import android.app.UiModeManager
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar

/**
 * 自定义函数，在Activity中快速创建ViewBinding
 */
inline fun <VB : ViewBinding> AppCompatActivity.binding(
    crossinline inflate: (LayoutInflater) -> VB
) = lazy{
    val uiModeManager: UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    ImmersionBar.with(this)
        .transparentStatusBar()  //透明状态栏，不写默认透明色
        .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
        .statusBarDarkFont(uiModeManager.nightMode != UiModeManager.MODE_NIGHT_YES)   //状态栏字体是深色，不写默认为亮色
        .navigationBarDarkIcon(uiModeManager.nightMode != UiModeManager.MODE_NIGHT_YES) //导航栏图标是深色，不写默认为亮色
        .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)  //单独指定软键盘模式
        .init()
    inflate(layoutInflater).apply {
        setContentView(this.root)
    }
}