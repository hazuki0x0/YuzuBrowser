package jp.hazuki.yuzubrowser.legacy.utils

interface IErrorReport {
    fun setDetailedLogRemote(enable: Boolean)

    fun printAndWriteLogRemote(e: Throwable):Boolean
}