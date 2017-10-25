package jp.hazuki.yuzubrowser.utils.view

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View

import jp.hazuki.yuzubrowser.utils.extensions.setClipboardWithToast

class CopyableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs), View.OnLongClickListener {

    init {
        setOnLongClickListener(this)
    }

    override fun onLongClick(v: View): Boolean {
        context.setClipboardWithToast(text.toString())
        return true
    }
}
