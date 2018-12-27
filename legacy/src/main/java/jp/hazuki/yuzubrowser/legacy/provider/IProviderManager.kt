package jp.hazuki.yuzubrowser.legacy.provider

interface IProviderManager {
    val readItLaterProvider: IReadItLaterProvider

    val safeFileProvider: ISafeFileProvider

    val suggestProvider: ISuggestProvider

    val downloadFileProvider: IDownloadProvider
}