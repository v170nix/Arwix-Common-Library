@file:Suppress("unused")

package net.arwix.file

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import java.io.File

class FileRemoteRepository(
    private val context: Context,
    private val pathname: String,
    private val remoteFileMediator: RemoteFileMediator
    ) {

    suspend fun getFileOrDownload(key: String, url: String, force: Boolean): Flow<FileNetEvent> {
        if (!force) {
            val fileEvent = getFile(key).single()
            if (fileEvent is FileNetEvent.OnSuccess) {
                return flow { emit(fileEvent) }
            }
        }
        return remoteFileMediator(context, url).transform {
            when (it) {
                is DownloadEvent.OnComplete -> {
                    val file = File(pathname, key)
                    runCatching { it.file.copyTo(file, true); it.delete() }
                        .onSuccess { emit(FileNetEvent.OnSuccess(key, file)) }
                        .onFailure { e -> emit(FileNetEvent.OnError(key, e)) }
                }
                is DownloadEvent.OnErrorLoading -> {
                    emit(FileNetEvent.OnError(key, it.e))
                }
                is DownloadEvent.OnProgressLoading -> {
                    emit(FileNetEvent.OnProgressLoading(key, it.percent))
                }
                DownloadEvent.OnStart -> {
                    emit(FileNetEvent.OnStartLoading(key))
                }
            }
        }
    }

    suspend fun deleteOldest(maxSavedFiles: Int) {
        withContext(Dispatchers.IO) {
            runCatching {
                File(pathname).deleteOldest(maxSavedFiles)
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun getFile(key: String) = flow {
        val file = File(pathname, key)
        if (!file.exists()) {
            emit(FileNetEvent.OnNonExist(key))
        } else {
            emit(FileNetEvent.OnSuccess(key, file))
        }

    }

    sealed class FileNetEvent(open val key: String) {
        data class OnStartLoading(override val key: String) : FileNetEvent(key)
        data class OnProgressLoading(override val key: String, val percent: Int) : FileNetEvent(key)
        data class OnNonExist(override val key: String): FileNetEvent(key)
        data class OnSuccess(override val key: String, val file: File): FileNetEvent(key)
        data class OnError(override val key: String, val e: Throwable) : FileNetEvent(key)
    }



}