package net.arwix.file

import androidx.annotation.WorkerThread
import java.io.File

class FileCache(
    private val directory: File,
    private val maxNumFiles: Int)
{

    @WorkerThread
    fun clear() {
        val files = directory.listFiles() ?: return
        files
            .sortedBy { -it.lastModified() }
            .asSequence()
            .drop(maxNumFiles)
            .forEach {
                runCatching { it.delete() }
            }
    }

}

fun File.deleteOldest(maxSaveFiles: Int) {
    val files = this.listFiles() ?: return
    files
        .sortedBy { -it.lastModified() }
        .asSequence()
        .drop(maxSaveFiles)
        .forEach {
            runCatching { it.delete() }
        }
}