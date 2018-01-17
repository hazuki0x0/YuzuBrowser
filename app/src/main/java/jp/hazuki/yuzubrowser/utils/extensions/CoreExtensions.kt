package jp.hazuki.yuzubrowser.utils.extensions

inline fun <reified T> Any?.isInstanceOf(action: (T) -> Unit) {
    if (this is T) action(this)
}