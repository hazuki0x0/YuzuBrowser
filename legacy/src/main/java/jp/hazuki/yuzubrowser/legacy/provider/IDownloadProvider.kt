package jp.hazuki.yuzubrowser.legacy.provider

import android.net.Uri
import java.io.File

interface IDownloadProvider {
    fun getUriFromPath(filePath: String): Uri

    fun getUriForFile(file: File): Uri
}