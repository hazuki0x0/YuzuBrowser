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

package jp.hazuki.yuzubrowser.legacy.utils.view.swipebutton

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.ui.extensions.ellipsizeUrl
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.ui.widget.swipebutton.SwipeController

open class SwipeTextButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatButton(context, attrs), SwipeController.OnChangeListener {
    private val mController = SwipeSoftButtonController(getContext().applicationContext)

    private var content: CharSequence = ""
    private var visibleText: CharSequence? = null

    private var typeUrl: Boolean = false

    fun setActionData(action_list: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mController.setActionData(action_list, controller, iconManager)
        mController.setOnChangeListener(this)

        onSetNormalBackground()
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
        onSetNormalBackground()
        return false
    }

    override fun onEventCancel(): Boolean {
        onSetNormalBackground()
        return false
    }

    override fun onEventActionUp(whatNo: Int): Boolean {
        onSetNormalBackground()
        return false
    }

    override fun onEventActionDown(): Boolean {
        onSetPressedBackground()
        return false
    }

    protected open fun onSetNormalBackground() {
        setBackgroundResource(R.drawable.swipebtn_text_background_normal)
    }

    protected open fun onSetPressedBackground() {
        val theme = ThemeData.getInstance()
        if (theme?.toolbarTextButtonBackgroundPress != null) {
            background = theme.toolbarTextButtonBackgroundPress
        } else {
            setBackgroundResource(R.drawable.swipebtn_text_background_pressed)
        }
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
            content.ellipsizeUrl(paint, availWidth.toFloat())
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
