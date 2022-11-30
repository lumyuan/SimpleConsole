package com.lumyuan.simpleconsole

import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lumyuan.consoleobserver.Console
import com.lumyuan.consoleobserver.observer.Observer
import com.lumyuan.simpleconsole.ConsoleApplication.Companion.console
import com.lumyuan.simpleconsole.databinding.ActivityMainBinding
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

class MainActivity : AppCompatActivity(), OnClickListener {
    private val logModel by viewModels<MainViewModel>()
    private val binding by binding(ActivityMainBinding::inflate)
    //主线程控制器
    private val mainLooperHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        logModel.log.observe(this){
            binding.editor.text.replace(0, binding.editor.text.length, it)
        }

        //注册成功消息的监听器（观察者）
        console.successLiveData.observe(object : Observer<Console.Logcat>{
            override fun onChanged(t: Console.Logcat?) {
                mainLooperHandler.post{
                    val length = binding.editor.text.length
                    binding.editor.text.replace(length, length, "${t?.message}")
                }
            }
        })

        //注册错误消息的监听器（观察者）
        console.errorLiveData.observe(object : Observer<Console.Logcat>{
            override fun onChanged(t: Console.Logcat?) {
                mainLooperHandler.post{
                    val length = binding.editor.text.length
                    binding.editor.text.replace(length, length, "${t?.message}")
                }
            }
        })

        loadDefaultThemes()
        loadDefaultLanguages()
        loadLanguage()
        val typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        binding.editor.apply {
            typefaceText = typeface
            setLineSpacing(2f, 1.1f)
            nonPrintablePaintingFlags =
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
            val uiModeManager: UiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            ThemeRegistry.getInstance().setTheme(
                if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES){
                    "darcula"
                }else {
                    "quietlight"
                }
            )
            this.isLineNumberEnabled = false
            this.editable = false
            this.textSizePx = 36f
        }

        binding.exec.setOnClickListener(this)
        binding.restart.setOnClickListener(this)
        binding.destroy.setOnClickListener(this)
        binding.getLog.setOnClickListener(this)
        binding.clear.setOnClickListener(this)
    }

    private fun ensureTextmateTheme() {
        val editor = binding.editor
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = editorColorScheme
        }
    }

    private fun loadLanguage(){
        try {
            ensureTextmateTheme()
            val editorLanguage = binding.editor.editorLanguage
            val language = if (editorLanguage is TextMateLanguage) {
                editorLanguage.updateLanguage(
                    "text.html.markdown"
                )
                editorLanguage
            } else {
                TextMateLanguage.create(
                    "text.html.markdown",
                    true
                )
            }
            binding.editor.setEditorLanguage(
                language
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDefaultLanguages() {
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
    }

    private fun loadDefaultThemes() {

        //add assets file provider
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                applicationContext.assets
            )
        )

        val themes = arrayOf("darcula", "abyss", "quietlight", "solarized_drak")
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                )
            )
        }

        themeRegistry.setTheme("quietlight")
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.exec -> {
                val trim = binding.input.text.toString().trim()
                if (!TextUtils.isEmpty(trim)){
                    val length = binding.editor.text.length
                    binding.editor.text.replace(
                        length, length, "${
                        if (console.isSu()){
                            "# "
                        }else {
                            "$ "
                        }
                    }${trim}\n")
                    val execString = console.execString(trim)
                    if (execString){
                        binding.input.text = null
                    }
                }else {
                    Toast.makeText(this, "还没有输入指令哦~", Toast.LENGTH_SHORT).show()
                }
            }
            binding.restart -> {
                console.restart()
            }
            binding.destroy -> {
                console.destroy()
            }
            binding.getLog -> {
                val logSpanned = console.getLogSpanned()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Log", logSpanned) as ClipData
                clipboard.setPrimaryClip(clip);
            }
            binding.clear -> {
                MaterialAlertDialogBuilder(this).apply{
                    setTitle("清空日志")
                    setMessage("该操作不可撤销，确定清空吗")
                    this.setPositiveButton("清空"){_, _ ->
                        console.clear()
                        binding.editor.setText(null)
                    }
                    this.setNeutralButton("取消", null)
                }.show()
            }
        }
    }

    override fun onDestroy() {
        logModel.log.value = binding.editor.text.toString()
        binding.editor.release()
        super.onDestroy()
    }
}