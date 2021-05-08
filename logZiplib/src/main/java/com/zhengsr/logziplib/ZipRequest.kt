package com.zhengsr.logziplib

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.Executors

/**
 * @author by zhengshaorui 2021/4/30 17:57
 * describe：
 */
class ZipRequest(private val context: Context) {
    private val executors = Executors.newSingleThreadExecutor()

    //墓碑文件
    private var TOMBSTONES_PATH = "${Environment.getDataDirectory()}/tombstones"

    //anr文件
    private val ANR_PATH = "${Environment.getDataDirectory()}/anr"
    private val handler = Handler(Looper.getMainLooper())
    private var listener: ZLogListener? = null
    private var logPath: List<String>? = null
    private var zipName: String? = null
    private var zipPath: String? = null
    private var logFiles: List<File>? = null

    /**
     * 自己应用的log
     */
    fun logPath(vararg logPath: String): ZipRequest {
        this.logPath = logPath.toList()
        return this
    }

    /**
     * 需要一起压缩的log文件,可以是其他log的文件
     */
    fun logFiles(logFiles: List<File>): ZipRequest {
        this.logFiles = logFiles
        return this
    }


    /**
     * 需要解压的路径
     * @param zipPath 默认放到U盘下
     */
    fun zipPath(zipPath: String): ZipRequest {
        this.zipPath = zipPath
        return this
    }

    /**
     * 压缩的名字
     * @param zipName 默认包名.zip
     */
    fun zipName(zipName: String): ZipRequest {
        this.zipName = zipName
        return this
    }

    /**
     * 监听
     */
    fun listener(listener: ZLogListener): ZipRequest {
        this.listener = listener
        return this
    }

    fun zip() {
        if (logPath.isNullOrEmpty() && logFiles.isNullOrEmpty()) {
            handler.post {
                listener?.onFail("无log路径或文件，请输入 logPath 或 logFiles")
            }
            return
        }
        val usbPath = ZipUtil.getUsbPath(context)
        if (zipPath.isNullOrEmpty() && usbPath.isNullOrEmpty()) {
            handler.post {
                listener?.onFail("找不到U盘或没有输出路径")
            }
            return
        }
        zipPath = if (!zipPath.isNullOrEmpty()) zipPath else usbPath

        zipTask()
    }


    private fun zipTask() {
        executors.execute {
            handler.post {
                listener?.onStart()
            }
            val zipFileName = zipName ?: "${context.packageName}.zip"
            //开始打包
            zipPath?.let {
                try {
                    val fileList = kotlin.collections.mutableListOf<File>()
                    val dstPath = it + java.io.File.separator + zipFileName
                    val zipFile = File(it, zipFileName)
                    //先删除
                    if (zipFile.exists()) {
                        zipFile.delete()
                    }
                    File(ANR_PATH).apply {
                        if (this.exists()) {
                            fileList.add(this)
                        }
                    }
                    File(TOMBSTONES_PATH).apply {
                        if (this.exists()) {
                            fileList.add(this)
                        }
                    }
                    logPath?.forEach {
                        File(it).apply {
                            if (this.exists()) {
                                fileList.add(this)
                            }
                        }
                    }
                    logFiles?.forEach { file ->
                        if (file.exists()) {
                            fileList.add(file)
                        }
                    }
                    if (fileList.isNullOrEmpty()) {
                        listener?.onFail("没有需要压缩的文件")
                        return@execute
                    }
                    com.zhengsr.logziplib.ZipUtil.zipFolder(fileList, dstPath)
                    val dstFile = File(dstPath)
                    handler.post {
                        if (dstFile.length() <= 0) {
                            listener?.onFail("压缩失败,$zipFileName 为空 ")
                        } else {
                            listener?.onSuccess(dstPath)
                            try {
                                com.jaredrummler.android.shell.Shell.SH.run("sync")
                            } catch (e: Exception) {
                            }
                        }
                    }
                } catch (e: Exception) {
                    handler.post {
                        listener?.onFail("压缩失败: $e")
                    }
                }

            } ?: listener?.onFail("找不到U盘或没有输出路径")
        }
    }


}