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

package jp.hazuki.yuzubrowser.legacy.toolbar

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.util.StateSet
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.extensions.forEach
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

open class AbstractToolbar(context: Context) : LinearLayout(context) {

    //ThemeData maybe null
    fun onThemeChanged(themeData: ThemeData?) {
        THEME_IMAGE_COLOR_FILTER = if (themeData != null && themeData.toolbarImageColor != 0)
            PorterDuffColorFilter(themeData.toolbarImageColor, PorterDuff.Mode.SRC_ATOP)
        else
            null

        if (themeData?.toolbarButtonBackgroundPress != null) {
            val press: Drawable
            press = themeData.toolbarButtonBackgroundPress
            val drawable = StateListDrawable()
            drawable.addState(intArrayOf(android.R.attr.state_pressed), press)
            drawable.addState(StateSet.WILD_CARD, resources.getDrawable(R.drawable.swipebtn_image_background_normal, context.theme))
            THEME_BUTTON_BG = drawable
        } else {
            THEME_BUTTON_BG = resources.getDrawable(R.drawable.swipebtn_image_background, context.theme)
        }
        applyTheme(themeData)
    }

    open fun applyTheme(themeData: ThemeData?) {}

    protected open fun applyThemeAutomatically(themeData: ThemeData?) {
        applyTheme(themeData, this)
    }

    protected fun applyTheme(themeData: ThemeData?, viewGroup: ViewGroup) {
        viewGroup.forEach {
            when (it) {
                is ViewGroup -> applyTheme(themeData, it)
                is TextView -> applyTheme(themeData, it)
                is ImageView -> applyTheme(it)
            }
        }
    }

    protected fun applyTheme(themeData: ThemeData?, textView: TextView) {
        if (themeData != null && themeData.toolbarTextColor != 0)
            textView.setTextColor(themeData.toolbarTextColor)
        else {
            textView.setTextColor(ResourcesCompat.getColor(resources, R.color.toolbar_text_color, context.theme))
        }

    }

    protected fun applyTheme(themeData: ThemeData, button: Button) {
        applyTheme(themeData, button as TextView)
        button.background = THEME_BUTTON_BG
    }

    companion object {
        private var THEME_IMAGE_COLOR_FILTER: ColorFilter? = null
        private var THEME_BUTTON_BG: Drawable? = null

        internal fun applyTheme(imageView: ImageView) {
            imageView.colorFilter = THEME_IMAGE_COLOR_FILTER
        }

        internal fun applyTheme(button: ImageButton) {
            applyTheme(button as ImageView)
            button.background = THEME_BUTTON_BG
        }

        internal fun applyTheme(controller: ButtonToolbarController) {
            controller.setColorFilter(THEME_IMAGE_COLOR_FILTER)
            controller.setBackgroundDrawable(THEME_BUTTON_BG)
        }
    }
}
