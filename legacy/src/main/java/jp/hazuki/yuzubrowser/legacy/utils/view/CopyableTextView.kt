package jp.hazuki.yuzubrowser.legacy.utils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast

class CopyableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs), View.OnLongClickListener {

    init {
        setOnLongClickListener(this)
    }

    override fun onLongClick(v: View): Boolean {
        context.setClipboardWithToast(text.toString())
        return true
    }
}
