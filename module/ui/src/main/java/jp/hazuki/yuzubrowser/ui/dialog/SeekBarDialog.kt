package jp.hazuki.yuzubrowser.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.app.AlertDialog
import jp.hazuki.yuzubrowser.ui.preference.SeekBarPreferenceController

class SeekBarDialog(context: Context) {
    private val mController: SeekBarPreferenceController = SeekBarPreferenceController(context)
    private val mBuilder: AlertDialog.Builder = AlertDialog.Builder(context)

    fun setSeekMin(i: Int): SeekBarDialog {
        mController.setSeekMin(i)
        return this
    }

    fun setSeekMax(i: Int): SeekBarDialog {
        mController.setSeekMax(i)
        return this
    }

    fun setValue(value: Int): SeekBarDialog {
        mController.value = value
        return this
    }

    fun setPositiveButton(textId: Int, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setPositiveButton(textId, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setPositiveButton(text: CharSequence, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setPositiveButton(text, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setNegativeButton(textId: Int, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setNegativeButton(textId, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setNegativeButton(text: CharSequence, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setNegativeButton(text, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setNeutralButton(textId: Int, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setNeutralButton(textId, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setNeutralButton(text: CharSequence, listener: OnClickListener?): SeekBarDialog {
        mBuilder.setNeutralButton(text, if (listener == null)
            null
        else
            DialogInterface.OnClickListener { dialog, which -> listener(dialog, which, mController.currentValue) })
        return this
    }

    fun setTitle(titleId: Int): SeekBarDialog {
        mBuilder.setTitle(titleId)
        return this
    }

    fun setTitle(title: CharSequence): SeekBarDialog {
        mBuilder.setTitle(title)
        return this
    }

    fun setCustomTitle(customTitleView: View): SeekBarDialog {
        mBuilder.setCustomTitle(customTitleView)
        return this
    }

    fun setIcon(iconId: Int): SeekBarDialog {
        mBuilder.setIcon(iconId)
        return this
    }

    fun setIcon(icon: Drawable): SeekBarDialog {
        mBuilder.setIcon(icon)
        return this
    }

    fun setCancelable(cancelable: Boolean): SeekBarDialog {
        mBuilder.setCancelable(cancelable)
        return this
    }

    fun setOnCancelListener(onCancelListener: OnCancelListener): SeekBarDialog {
        mBuilder.setOnCancelListener(onCancelListener)
        return this
    }

    fun create(): AlertDialog {
        mController.onPrepareDialogBuilder(mBuilder)
        return mBuilder.create()
    }

    fun show(): AlertDialog {
        val dialog = create()
        dialog.show()
        return dialog
    }
}

typealias OnClickListener = (dialog: DialogInterface, which: Int, value: Int) -> Unit
