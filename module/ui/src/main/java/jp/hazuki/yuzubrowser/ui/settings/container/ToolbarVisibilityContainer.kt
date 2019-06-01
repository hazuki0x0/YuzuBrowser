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

package jp.hazuki.yuzubrowser.ui.settings.container

class ToolbarVisibilityContainer(name: String, def_value: Int) : IntContainer(name, def_value) {

    var isVisible: Boolean
        get() = mValue and 0x01 != 0
        set(visible) {
            mValue = if (visible) mValue or 0x01 else mValue and 0x01.inv()
        }

    val isHideWhenFullscreen: Boolean
        get() = mValue and 0x02 != 0

    var isHideWhenEndLoading: Boolean
        get() = mValue and 0x04 != 0
        set(hide) {
            mValue = if (hide) mValue or 0x04 else mValue and 0x04.inv()
        }

    val isHideWhenPortrait: Boolean
        get() = mValue and 0x08 != 0

    val isHideWhenLandscape: Boolean
        get() = mValue and 0x10 != 0

    val isHideWhenLayoutShrink: Boolean
        get() = mValue and 0x20 != 0

    fun setAlwaysVisible(visible: Boolean) {
        mValue = if (visible) 0x01 else 0
    }

    //if add, please also add MultiListIntPreference max
}
