package jp.hazuki.yuzubrowser.provider

import android.net.Uri
import jp.hazuki.yuzubrowser.legacy.provider.ISuggestProvider

class SuggestProviderBridge : ISuggestProvider {
    override val uriNet: Uri
        get() = SuggestProvider.URI_NET
    override val uriLocal: Uri
        get() = SuggestProvider.URI_LOCAL
    override val uriNormal: Uri
        get() = SuggestProvider.URI_NORMAL
    override val uriNone: Uri
        get() = SuggestProvider.URI_NONE
    override val suggestHistory: String
        get() = SuggestProvider.SUGGEST_HISTORY
}