package jp.hazuki.yuzubrowser.provider

import jp.hazuki.yuzubrowser.legacy.provider.IProviderManager

class ProviderManager : IProviderManager {
    override val downloadFileProvider = DownloadFileProviderBridge()

    override val readItLaterProvider = ReadItLaterProviderBridge()

    override val safeFileProvider = SafeFileProviderBridge()

    override val suggestProvider = SuggestProviderBridge()
}