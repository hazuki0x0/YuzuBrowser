package jp.hazuki.yuzubrowser.provider

import android.net.Uri
import jp.hazuki.yuzubrowser.legacy.provider.IReadItLaterProvider

class ReadItLaterProviderBridge : IReadItLaterProvider {
    override val editUri: Uri
        get() = ReadItLaterProvider.EDIT_URI

    override fun getReadUri(time: Long) = ReadItLaterProvider.getReadUri(time)

    override fun getEditUri(time: Long) = ReadItLaterProvider.getEditUri(time)

    override fun convertToEdit(uri: Uri) = ReadItLaterProvider.convertToEdit(uri)
}