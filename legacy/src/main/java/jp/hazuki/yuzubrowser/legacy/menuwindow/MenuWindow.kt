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

package jp.hazuki.yuzubrowser.legacy.menuwindow

import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToFloatPx
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

typealias OnMenuCloseListener = () -> Unit

class MenuWindow(context: ThemeActivity, actionList: ActionList, controller: ActionController, iconManager: ActionIconManager) : PopupWindow.OnDismissListener {

    private val windowMargin = context.convertDpToPx(4)
    private val window = PopupWindow(context)
    private val handler = Handler()
    private var locking = false
    private var mListener: OnMenuCloseListener? = null

    init {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.drop_down_list, null) as ScrollView
        val layout = v.findViewById<LinearLayout>(R.id.items)
        val array = ActionNameArray(context)

        window.contentView = v
        window.isOutsideTouchable = true
        window.height = LinearLayout.LayoutParams.WRAP_CONTENT
        window.width = LinearLayout.LayoutParams.WRAP_CONTENT
        window.setBackgroundDrawable(context.getDrawable(R.drawable.menu_drop_down_background))
        window.elevation = context.convertDpToFloatPx(10)
        window.setOnDismissListener(this)
        window.contentView.isFocusableInTouchMode = true
        window.contentView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_MENU && event.repeatCount == 0 && event.action == KeyEvent.ACTION_DOWN) {
                dismiss()
                return@setOnKeyListener true
            }
            false
        }

        val fontSize = FontUtils.getTextSize(AppPrefs.font_size.menu.get())
        for (action in actionList) {
            val child = inflater.inflate(R.layout.menu_list_item, v, false)
            val icon = child.findViewById<ImageView>(R.id.iconImageView)
            val name = child.findViewById<TextView>(R.id.actionNameTextView)
            if (fontSize >= 0) {
                name.textSize = fontSize.toFloat()
            }
            child.setOnClickListener {
                controller.run(action)
                window.dismiss()
            }
            if (AppPrefs.menu_icon.get()) {
                icon.setImageDrawable(iconManager[action])
            } else {
                icon.visibility = View.GONE
            }
            name.text = action.toString(array)
            layout.addView(child)
        }

        if (AppPrefs.fullscreen.get())
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun show(root: View, gravity: Int) {
        if (!locking) {
            //This is a magic!
            window.isFocusable = false

            window.showAtLocation(root, gravity, if (gravity and 0x110 != 0) windowMargin else 0, 0)

            //Reset focusable
            window.isFocusable = true
        }
    }

    fun showAsDropDown(anchor: View) {
        if (!locking) {
            //This is a magic!
            window.isFocusable = false

            window.showAsDropDown(anchor)

            //Reset focusable
            window.isFocusable = true
        }
    }

    fun setSystemUiVisibility(flags: Int) {
        window.contentView.systemUiVisibility = flags
    }

    val isShowing: Boolean
        get() = window.isShowing

    fun dismiss() {
        window.dismiss()
    }

    fun setListener(listener: OnMenuCloseListener) {
        mListener = listener
    }

    override fun onDismiss() {
        locking = true
        handler.postDelayed(lock, 50)

        mListener?.invoke()
    }

    private val lock = { locking = false }
}
