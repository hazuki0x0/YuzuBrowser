package jp.hazuki.yuzubrowser.legacy.provider

import android.net.Uri

interface ISuggestProvider {
    val uriNet: Uri
    val uriLocal: Uri
    val uriNormal: Uri
    val uriNone: Uri

    val suggestHistory: String
}