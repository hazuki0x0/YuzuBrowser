/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.speeddial

import java.io.Serializable

data class SpeedDial(var id: Int, var url: String?, var title: String?, var icon: WebIcon?, var isFavicon: Boolean, var updateTime: Long = 0) : Serializable {
    @JvmOverloads constructor(url: String? = "", title: String? = "", icon: WebIcon? = null, isFavicon: Boolean = false) : this(-1, url, title, icon, isFavicon)


    override fun equals(other: Any?): Boolean {
        return if (other is SpeedDial) {
            if (other.id <= 0) false else other.id == id
        } else false
    }
}
