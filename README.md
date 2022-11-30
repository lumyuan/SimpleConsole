# SimpleConsole
一个简易的控制台程序，支持Process复用以及实时监听日志

## 引入依赖
app/build.gradle
```java
dependencies {
	implementation 'com.github.lumyuan:SimpleConsole:v0.0.1'
}
```

## 使用
1. 创建Console实例
```kotlin
//因为进程可以复用，所以推荐声明为静态变量
//这里推荐放到Application中，这样就不会重复创建Console实例
companion object {
	val console = Console()
	//直接获取SU权限
        //val console = Console(Permission.SU())
}
```
2. 运行指令
```kotlin
//给控制台写入字符串
val isRun = console.execString("xxxxx")
//给控制台写入字节数组
//val isRun = console.execBytes("xxxxx")
println(isRun)
```
3. 注册控制台消息观察者：SuccessObserver & ErrorObserver
```kotlin
//注册成功消息的监听器（观察者）
console.successLiveData.observe(object : Observer<Console.Logcat>{
    override fun onChanged(t: Console.Logcat?) {
	//at backgroud thread
	println(t?.message)
    }
})

//注册错误消息的监听器（观察者）
console.errorLiveData.observe(object : Observer<Console.Logcat>{
    override fun onChanged(t: Console.Logcat?) {
	//at backgroud thread
	println(t?.message)
    }
})
```
4. 其他公开函数
```kotlin
//重启控制台
console.restart()
//销毁控制台
console.destroy()
//清空日志
console.clear()
//获取所有日志
console.getLogSpanned()
......
```
## 其他
本项目推荐Kotlin项目导入使用，克隆本项目查看更多推荐写法。Java项目导入本项目请一并导入Kotlin核心依赖

# 免责声明
本项目仅用于学习与交流，请勿使用或引用本项目进行任何违法犯罪活动！

[![](https://jitpack.io/v/lumyuan/SimpleConsole.svg)](https://jitpack.io/#lumyuan/SimpleConsole)
