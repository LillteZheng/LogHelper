package com.zhengsr.easylog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.zhengsr.logziplib.ZLogListener
import com.zhengsr.logziplib.ZLogg
import java.util.jar.Manifest

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)

    }

    fun zip(view: View) {
        val logPath1 = "${Environment.getExternalStorageDirectory().absolutePath}/Logs/com.exceedshare.boxlauncher"
        val logPath2 = "${Environment.getExternalStorageDirectory().absolutePath}/Logs/com.exceedshare.boxsetting"
        val zipPath = "${Environment.getExternalStorageDirectory().absolutePath}"
        val zipName = "BoxSettingLog.zip"
        ZLogg.with(this)
                .logPath(logPath1,logPath2)
                .zipPath(zipPath)
                .zipName(zipName)
                .listener(object : ZLogListener {
                    override fun onStart() {
                        super.onStart()
                        Log.d(TAG, "onStart() called")
                        Toast.makeText(this@MainActivity, "开始压缩", Toast.LENGTH_SHORT).show()
                    }
                    override fun onSuccess(path: String) {
                        Log.d(TAG, "onSuccess() called with: path = $path")
                        Toast.makeText(this@MainActivity, "压缩成功:$path", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFail(errorMsg: String) {
                        Log.d(TAG, "onFail() called with: errorMsg = $errorMsg")
                        Toast.makeText(this@MainActivity, "压缩失败: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }).zip()
    }
}