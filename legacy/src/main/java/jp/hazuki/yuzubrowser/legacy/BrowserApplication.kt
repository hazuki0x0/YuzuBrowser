package jp.hazuki.yuzubrowser.legacy

import jp.hazuki.yuzubrowser.legacy.provider.IProviderManager

interface BrowserApplication {
    val applicationId: String
    val permissionAppSignature: String
    val browserState: BrowserState
    val providerManager: IProviderManager
}