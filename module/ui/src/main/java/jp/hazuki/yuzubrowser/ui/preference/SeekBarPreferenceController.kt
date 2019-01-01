/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import jp.hazuki.yuzubrowser.core.utility.extensions.hideIme
import jp.hazuki.yuzubrowser.ui.R

open class SeekBarPreferenceController(private val mContext: Context) {
    private var mSeekMin: Int = 0
    private var mSeekMax: Int = 0
    var value: Int = 0
    private var mTempValue: Int = 0
    private var comment: String? = null

    val currentValue: Int
        get() = mTempValue + mSeekMin

    fun setSeekMin(i: Int) {
        mSeekMin = i
    }

    fun setSeekMax(i: Int) {
        mSeekMax = i
    }

    fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        val view = LayoutInflater.from(mContext).inflate(R.layout.seekbar_preference, null)
        val textView = view.findViewById<TextView>(R.id.countTextView)
        val seekbar = view.findViewById<SeekBar>(R.id.seekBar)

        seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mTempValue = seekBar.progress
                textView.text = (mTempValue + mSeekMin).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = (progress + mSeekMin).toString()
            }
        })
        seekbar.max = mSeekMax - mSeekMin
        mTempValue = value - mSeekMin
        seekbar.progress = mTempValue

        view.findViewById<View>(R.id.prevImageButton).setOnClickListener {
            if (mTempValue > 0)
                seekbar.progress = --mTempValue
        }
        view.findViewById<View>(R.id.nextImageButton).setOnClickListener {
            if (mTempValue < mSeekMax - mSeekMin)
                seekbar.progress = ++mTempValue
        }

        textView.setOnClickListener {
            val edittext = EditText(mContext)
            edittext.inputType = InputType.TYPE_CLASS_NUMBER
            edittext.setText((mTempValue + mSeekMin).toString())

            AlertDialog.Builder(mContext)
                    .setView(edittext)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        try {
                            val value = Integer.parseInt(edittext.text.toString(), 10)
                            if (value in mSeekMin..mSeekMax) {
                                mTempValue = value - mSeekMin
                                seekbar.progress = mTempValue
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                        mContext.applicationContext.hideIme(edittext)
                        onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }

        if (!TextUtils.isEmpty(comment)) {
            val commentText = view.findViewById<TextView>(R.id.commentTextView)
            commentText.visibility = View.VISIBLE
            commentText.text = comment
        }

        builder.setView(view)
    }

    fun setComment(comment: String?) {
        this.comment = comment
    }

    open fun onClick(dialog: DialogInterface, which: Int) {}
}
