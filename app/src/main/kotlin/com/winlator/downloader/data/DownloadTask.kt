package com.winlator.downloader.data

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

enum class DownloadStatus {
    IDLE, QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED
}

class DownloadTask(
    val url: String,
    val file: File,
    val title: String
) {
    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _status = MutableStateFlow(DownloadStatus.IDLE)
    val status = _status.asStateFlow()

    private var job: Job? = null
    private val client get() = AppDownloadManager.client

    fun start(scope: CoroutineScope) {
        if (_status.value == DownloadStatus.DOWNLOADING || _status.value == DownloadStatus.COMPLETED) return
        _status.value = DownloadStatus.QUEUED

        job = scope.launch(Dispatchers.IO) {
            try {
                _status.value = DownloadStatus.DOWNLOADING
                val downloadedBytes = if (file.exists()) file.length() else 0L

                val request = Request.Builder()
                    .url(url)
                    .header("Range", "bytes=$downloadedBytes-")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful && response.code != 206) {
                        if (response.code == 416) {
                            // Already finished or range not satisfiable
                            _progress.value = 1f
                            _status.value = DownloadStatus.COMPLETED
                            return@launch
                        }
                        _status.value = DownloadStatus.FAILED
                        return@launch
                    }

                    val body = response.body ?: throw Exception("Empty body")
                    val totalBytes = (body.contentLength() + downloadedBytes)

                    if (downloadedBytes >= totalBytes && totalBytes > 0) {
                        _progress.value = 1f
                        _status.value = DownloadStatus.COMPLETED
                        return@launch
                    }

                    file.parentFile?.mkdirs()
                    val raf = RandomAccessFile(file, "rw")
                    try {
                        raf.seek(downloadedBytes)

                        val source = body.source()
                        val buffer = ByteArray(8192)
                        var bytesRead = 0
                        var currentDownloaded = downloadedBytes

                        while (isActive && source.read(buffer).also { bytesRead = it } != -1) {
                            raf.write(buffer, 0, bytesRead)
                            currentDownloaded += bytesRead
                            _progress.value = if (totalBytes > 0) currentDownloaded.toFloat() / totalBytes else 0f
                        }
                    } finally {
                        raf.close()
                    }
                    if (isActive) {
                        _status.value = DownloadStatus.COMPLETED
                        _progress.value = 1f
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    _status.value = DownloadStatus.FAILED
                }
            }
        }
    }

    fun pause() {
        if (_status.value == DownloadStatus.DOWNLOADING || _status.value == DownloadStatus.QUEUED) {
            job?.cancel()
            _status.value = DownloadStatus.PAUSED
        }
    }

    fun resume(scope: CoroutineScope) {
        if (_status.value == DownloadStatus.PAUSED || _status.value == DownloadStatus.FAILED) {
            start(scope)
        }
    }
}

object AppDownloadManager {
    val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _tasks = mutableStateListOf<DownloadTask>()
    val tasks: List<DownloadTask> = _tasks
    val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun addTask(url: String, file: File, title: String): DownloadTask {
        val existing = _tasks.find { it.url == url }
        if (existing != null) return existing

        var finalFile = file
        if (finalFile.exists()) {
            val name = finalFile.nameWithoutExtension
            val ext = finalFile.extension
            val parent = finalFile.parentFile
            var counter = 1
            while (finalFile.exists()) {
                val newName = if (ext.isNotEmpty()) "$name ($counter).$ext" else "$name ($counter)"
                finalFile = File(parent, newName)
                counter++
            }
        }

        val newTask = DownloadTask(url, finalFile, title)
        _tasks.add(newTask)
        return newTask
    }

    fun removeTask(task: DownloadTask) {
        task.pause()
        _tasks.remove(task)
    }
}
