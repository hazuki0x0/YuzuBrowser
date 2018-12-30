package jp.hazuki.yuzubrowser.legacy.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import jp.hazuki.utility.extensions.clipboardText
import jp.hazuki.yuzubrowser.legacy.BrowserApplication
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.getMimeType
import jp.hazuki.yuzubrowser.legacy.utils.getPathFromUri

inline val Context.browserApplicationContext: BrowserApplication
    get() = applicationContext as BrowserApplication

fun Context.setClipboardWithToast(text: String?) {
    if (text == null) return

    clipboardText = text
    Toast.makeText(this, getString(R.string.copy_clipboard_mes_before) + text, Toast.LENGTH_SHORT).show()
}

fun createFileOpenIntent(context: Context, uri: Uri, mimeType: String, name: String): Intent {
    val target = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val provider = (context.applicationContext as BrowserApplication).providerManager.downloadFileProvider
        val path = uri.path ?: ""
        if (uri.scheme == "file") provider.getUriFromPath(path) else uri
    } else {
        if (uri.scheme == "file") {
            uri
        } else {
            val path = context.getPathFromUri(uri)
            if (path != null) Uri.parse("file://$path") else uri
        }
    }

    var resolvedMineType = getMimeType(name)
    if (resolvedMineType == Constants.mimeType.UNKNOWN) {
        resolvedMineType = mimeType
    }

    return Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        setDataAndType(target, resolvedMineType)
    }
}