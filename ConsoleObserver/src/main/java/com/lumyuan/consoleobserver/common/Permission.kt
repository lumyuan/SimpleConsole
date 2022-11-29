package com.lumyuan.consoleobserver.common

abstract class Permission(private val permission: String) {
    override fun toString(): String {
        return permission
    }
    public class SU : Permission("su")
    public class SH : Permission("sh")
}