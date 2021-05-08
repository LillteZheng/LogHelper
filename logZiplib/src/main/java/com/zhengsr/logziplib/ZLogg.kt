package com.zhengsr.logziplib

import android.content.Context

/**
 * @author by zhengshaorui 2021/4/29 15:45
 * describe：log 打包库，直接把指定路径的log打包到第一个U盘，也支持客制化
 * log文件包括墓碑文件和anr文件
 */


object ZLogg {
    fun with(context: Context) = ZipRequest(context.applicationContext)
}






