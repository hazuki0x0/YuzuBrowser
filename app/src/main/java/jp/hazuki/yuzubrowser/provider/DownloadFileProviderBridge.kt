package jp.hazuki.yuzubrowser.provider

import jp.hazuki.yuzubrowser.legacy.provider.IDownloadProvider
import java.io.File

class DownloadFileProviderBridge : IDownloadProvider {
    override fun getUriFromPath(filePath: String) = DownloadFileProvider.getUriFromPath(filePath)

    override fun getUriForFile(file: File) = DownloadFileProvider.getUriForFile(file)
}