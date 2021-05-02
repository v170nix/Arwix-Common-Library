package net.arwix.file

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.io.File

sealed class DownloadEvent {
    object OnStart : DownloadEvent()
    data class OnProgressLoading(val percent: Int) : DownloadEvent()
    data class OnErrorLoading(val e: Throwable) : DownloadEvent()
    data class OnComplete(val file: File) : DownloadEvent() {
        fun delete() {
            try {
                file.delete()
            } catch (i: Exception) {
                runCatching {
                    file.delete()
                }
            }
        }
    }
}

fun interface DownloadFile {
   operator fun invoke(context: Context, url: String): Flow<DownloadEvent>
}
