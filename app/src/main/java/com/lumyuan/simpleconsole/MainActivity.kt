package com.lumyuan.simpleconsole

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lumyuan.consoleobserver.Console
import com.lumyuan.consoleobserver.observer.Observer
import com.lumyuan.simpleconsole.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnClickListener {

    private val binding by binding(ActivityMainBinding::inflate)
    //主线程控制器
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    //控制台对象
    private lateinit var console: Console
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        console = Console()
        //直接获取SU权限
        //console = Console(Permission.SU())

        //注册成功消息的监听器（观察者）
        console.successLiveData.observe(object : Observer<Console.Logcat>{
            override fun onChanged(t: Console.Logcat?) {
                mainLooperHandler.post{
                    //绑定视图，为文本框添加消息
                    binding.logcat.append(console.getSpanned(t))
                    //ScrollView滚动到底部
                    binding.scrollView.postDelayed({
                        binding.scrollView.apply {
                            fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }, 200)
                }
            }
        })

        //注册错误消息的监听器（观察者）
        console.errorLiveData.observe(object : Observer<Console.Logcat>{
            override fun onChanged(t: Console.Logcat?) {
                mainLooperHandler.post{
                    //绑定视图，为文本框添加消息
                    binding.logcat.append(console.getSpanned(t))
                    //ScrollView滚动到底部
                    binding.scrollView.postDelayed({
                        binding.scrollView.apply {
                            fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    }, 200)
                }
            }
        })

        //为按钮绑定点击事件
        binding.submit.setOnClickListener(this)
        binding.restart.setOnClickListener(this)
        binding.getLog.setOnClickListener(this)
        binding.clear.setOnClickListener(this)
        binding.destroy.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.submit -> {
                val cmd = binding.input.text.toString().trim()
                if (!TextUtils.isEmpty(cmd)){
                    console.execString(cmd)
                    binding.input.text = null
                    binding.logcat.append("input: $cmd\n")
                }else {
                    Toast.makeText(this, "还没有输入指令哦~", Toast.LENGTH_SHORT).show()
                }
            }
            binding.restart -> {
                console.restart()
            }
            binding.getLog -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Log", console.getLogSpanned()) as ClipData
                clipboard.setPrimaryClip(clip);
            }
            binding.clear -> {
                console.clear()
                binding.logcat.text = null
            }
            binding.destroy -> {
                console.destroy()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        console.destroy()
    }
}