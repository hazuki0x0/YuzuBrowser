package jp.hazuki.yuzubrowser.legacy.provider

import android.net.Uri

interface IReadItLaterProvider {
    val editUri: Uri

    fun getReadUri(time: Long): Uri

    fun getEditUri(time: Long): Uri

    fun convertToEdit(uri: Uri): Uri

    companion object {
        const val TIME = "time"
        const val URL = "url"
        const val TITLE = "title"
        const val PATH = "path"

        const val COL_TIME = 0
        const val COL_URL = 1
        const val COL_TITLE = 2
    }
}