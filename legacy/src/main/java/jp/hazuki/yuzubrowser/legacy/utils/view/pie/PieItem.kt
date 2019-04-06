/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils.view.pie

import android.content.Context
import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType

import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.utils.view.pie.PieMenu.PieView

/**
 * Pie menu item
 */
class PieItem @JvmOverloads constructor(context: Context, itemsize: Int, private val action: Action, private val controller: ActionController, private val iconManager: ActionIconManager, val level: Int, var pieView: PieView? = null) : View.OnClickListener {
    val view = ImageView(context).apply {
        scaleType = ScaleType.FIT_CENTER
        layoutParams = ViewGroup.LayoutParams(itemsize, itemsize)
        setOnClickListener(this@PieItem)
    }
    var startAngle: Float = 0.toFloat()
        private set
    var sweep: Float = 0.toFloat()
        private set
    var innerRadius: Int = 0
        private set
    var outerRadius: Int = 0
        private set
    var isSelected: Boolean = false
        set(s) {
            field = s
            view.isSelected = s
        }
    var path: Path? = null
        private set

    val isPieView: Boolean
        get() = pieView != null

    fun notifyChangeState() {
        view.setImageDrawable(iconManager[action])
    }

    fun setGeometry(st: Float, sw: Float, inside: Int, outside: Int, p: Path) {
        startAngle = st
        sweep = sw
        innerRadius = inside
        outerRadius = outside
        path = p
    }

    override fun onClick(v: View) {
        controller.run(action)
    }
}
