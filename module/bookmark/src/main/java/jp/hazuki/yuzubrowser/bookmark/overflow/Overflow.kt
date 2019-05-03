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

package jp.hazuki.yuzubrowser.bookmark.overflow

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.bookmark.overflow.model.OverflowMenuModel
import jp.hazuki.yuzubrowser.bookmark.overflow.view.OverflowMenuAdapter

@BindingAdapter("bind:viewmodels")
internal fun RecyclerView.setViewModels(overflowMenuModels: List<OverflowMenuModel>?) {
    if (overflowMenuModels != null) {
        val adapter = adapter as OverflowMenuAdapter
        adapter.list.run {
            clear()
            addAll(overflowMenuModels)
        }
        adapter.notifyDataSetChanged()
    }
}
