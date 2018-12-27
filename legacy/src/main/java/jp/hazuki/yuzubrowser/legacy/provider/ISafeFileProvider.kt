package jp.hazuki.yuzubrowser.legacy.provider

interface ISafeFileProvider {
    fun convertToSaferUrl(url: String): String
}