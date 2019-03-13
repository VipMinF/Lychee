package com.vecharm.lychee.sample.rxjava2.task

import android.widget.Toast
import com.vecharm.lychee.http.config.defaults.getService
import com.vecharm.lychee.http.core.bytesRange
import com.vecharm.lychee.http.task.SpeedTask
import com.vecharm.lychee.sample.rxjava2.api.API
import com.vecharm.lychee.sample.rxjava2.ui.App
import com.vecharm.lychee.sample.rxjava2.api.request
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.RandomAccessFile

class DownloadTask : SpeedTask() {

    var onUpdate = { fileName: String, currLen: Long, size: Long, speed: Long, progress: Int ->
        this.speed = speed
        setTaskProgress(progress, currLen)
        setFileInfo(fileName, size)
        this.updateUI?.invoke() ?: Unit
    }
    var service: Disposable? = null

    var isPause = false
        private set

    fun pause() {
        isPause = true
        service?.dispose()
    }

    fun resume() {
        if (!isPause) return
        url ?: return
        filePath ?: return
        download(url!!, File(filePath))
        isPause = false
    }

    fun toggle(callBack: () -> Unit) {
        if (isPause) resume() else pause()
        callBack.invoke()
    }

    fun cache() {
        //todo 将任务信息保存到本地
    }


    fun download(url: String, saveFile: File) {
        setPathInfo(url, saveFile.absolutePath)
        if (range == 0L) saveFile.delete()
        val file = RandomAccessFile(saveFile, "rwd").also { it.seek(range) }
        service = getService<API>().download(url, range.bytesRange()).request(file) {
            onUpdateProgress = onUpdate
            onSuccess = { Toast.makeText(App.app, "${id}下载完成", Toast.LENGTH_LONG).show() }
        }
    }
}