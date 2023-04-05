package net.arwix.file

import android.content.Context
import kotlinx.coroutines.flow.Flow

fun interface RemoteFileMediator {
   operator fun invoke(context: Context, url: String): Flow<DownloadEvent>
}