package com.zhengsr.logziplib

interface ZLogListener {
    fun onStart(){}
    fun onSuccess(path: String)
    fun onFail(errorMsg: String)
}