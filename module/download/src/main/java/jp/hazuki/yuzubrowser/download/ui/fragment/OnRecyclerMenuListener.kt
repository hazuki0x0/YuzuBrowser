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

package jp.hazuki.yuzubrowser.download.ui.fragment

import android.view.Menu
import android.view.View

interface OnRecyclerMenuListener {
    fun onRecyclerItemClicked(v: View, position: Int)

    fun onCreateContextMenu(menu: Menu, position: Int)

    fun onRecyclerItemLongClicked(v: View, position: Int)

    fun onSelectionStateChange(items: Int)

    fun onCancelMultiSelectMode()
}