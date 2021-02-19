/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import jp.hazuki.yuzubrowser.core.lifecycle.LiveEvent
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionIconMap
import jp.hazuki.yuzubrowser.legacy.action.ActionNameMap
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile

class SoftButtonActionDetailViewModel(application: Application) : AndroidViewModel(application) {
    var names: ActionNameMap? = null
    var icons: ActionIconMap? = null

    private val defaultName = application.getString(R.string.do_nothing)

    val action = MutableLiveData<SoftButtonActionFile>()

    val onClick = LiveEvent<Int>()

    fun getName(type: Int): String? {
        return getName(typeToAction(type) ?: return null)
    }

    private fun getName(action: Action): String {
        val names = names!!

        return names[action] ?: defaultName
    }

    fun getIcon(type: Int): Drawable? {
        return getIcon(typeToAction(type) ?: return null)
    }

    private fun getIcon(action: Action): Drawable? {
        val icons = icons!!

        return icons[action]
    }

    fun onClick(type: Int) {
        onClick.notify(type)
    }

    private fun typeToAction(type: Int): Action? {
        val action = action.value ?: return null
        return when (type) {
            BUTTON_PRESS -> action.press
            BUTTON_LONG_PRESS -> action.lpress
            BUTTON_UP -> action.up
            BUTTON_DOWN -> action.down
            BUTTON_LEFT -> action.left
            BUTTON_RIGHT -> action.right
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        const val BUTTON_PRESS = 0
        const val BUTTON_LONG_PRESS = 1
        const val BUTTON_UP = 2
        const val BUTTON_DOWN = 3
        const val BUTTON_LEFT = 4
        const val BUTTON_RIGHT = 5
    }
}
