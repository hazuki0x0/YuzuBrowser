package jp.hazuki.yuzubrowser.legacy.utils

object ErrorReport {
    private var server: IErrorReport? = null

    fun init(errorReport: IErrorReport) {
        server = errorReport
    }

    fun setDetailedLog(enable: Boolean) {
        server?.setDetailedLogRemote(enable)
    }

    @JvmStatic
    fun printAndWriteLog(e: Throwable): Boolean {
        val server = server
        return if (server != null) {
            server.printAndWriteLogRemote(e)
        } else {
            e.printStackTrace()
            false
        }
    }
}