/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.utils.view.swipebutton

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.manager.ActionController
import jp.hazuki.yuzubrowser.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.theme.ThemeData
import jp.hazuki.yuzubrowser.utils.UrlUtils

open class SwipeTextButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatButton(context, attrs), SwipeController.OnChangeListener {
    private val mController = SwipeSoftButtonController(getContext().applicationContext)

    private var content: CharSequence = ""
    private var visibleText: CharSequence? = null

    private var typeUrl: Boolean = false

    fun setActionData(action_list: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mController.setActionData(action_list, controller, iconManager)
        mController.setOnChangeListener(this)

        setBackgroundResource(R.drawable.swipebtn_text_background_normal)
    }

    fun notifyChangeState() {
        mController.notifyChangeState()
        visibility = if (mController.shouldShow()) VISIBLE else GONE
    }

    fun setSense(sense: Int) {
        mController.setSense(sense)
    }

    fun setTypeUrl(typeUrl: Boolean) {
        this.typeUrl = typeUrl
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mController.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onLongPress() {}

    override fun onEventOutSide(): Boolean {
        setBackgroundResource(R.drawable.swipebtn_text_background_normal)
        return false
    }

    override fun onEventCancel(): Boolean {
        setBackgroundResource(R.drawable.swipebtn_text_background_normal)
        return false
    }

    override fun onEventActionUp(whatNo: Int): Boolean {
        setBackgroundResource(R.drawable.swipebtn_text_background_normal)
        return false
    }

    override fun onEventActionDown(): Boolean {
        if (ThemeData.isEnabled() && ThemeData.getInstance().toolbarButtonBackgroundPress != null)
            background = ThemeData.getInstance().toolbarButtonBackgroundPress
        else
            setBackgroundResource(R.drawable.swipebtn_text_background_pressed)
        return false
    }

    override fun onChangeState(whatNo: Int) {}

    override fun setText(text: CharSequence?, type: BufferType) {
        content = text ?: ""
        contentDescription = content
        updateVisibleText(measuredWidth - paddingLeft - paddingRight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        updateVisibleText(availWidth)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getTruncatedText(availWidth: Int): CharSequence {
        return if (typeUrl) {
            UrlUtils.ellipsizeUrl(content, paint, availWidth.toFloat())
        } else {
            TextUtils.ellipsize(content, paint, availWidth.toFloat(), TextUtils.TruncateAt.END)
        }
    }

    private fun updateVisibleText(availWidth: Int) {
        val newText = getTruncatedText(availWidth)

        if (newText != visibleText) {
            visibleText = newText

            super.setText(visibleText, BufferType.SPANNABLE)
        }
    }
}
