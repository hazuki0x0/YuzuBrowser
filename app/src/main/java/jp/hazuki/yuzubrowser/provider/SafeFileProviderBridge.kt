package jp.hazuki.yuzubrowser.provider

import jp.hazuki.yuzubrowser.legacy.provider.ISafeFileProvider

class SafeFileProviderBridge : ISafeFileProvider {
    override fun convertToSaferUrl(url: String) = SafeFileProvider.convertToSaferUrl(url)
}