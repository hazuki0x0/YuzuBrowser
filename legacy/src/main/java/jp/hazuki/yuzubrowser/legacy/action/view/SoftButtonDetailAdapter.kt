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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.databinding.FragmentSoftButtonActionItemBinding

class SoftButtonDetailAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: SoftButtonActionDetailViewModel,
) : RecyclerView.Adapter<SoftButtonDetailAdapter.ItemModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemModel {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentSoftButtonActionItemBinding.inflate(inflater, parent, false)
        return ItemModel(binding)
    }

    override fun onBindViewHolder(holder: ItemModel, position: Int) {
        holder.binding.let {
            it.lifecycleOwner = lifecycleOwner
            it.type = position
            it.typeName = holder.binding.root.context.resolveTypeName(position)
            it.viewModel = viewModel
        }
    }

    override fun getItemCount() = 6

    private fun Context.resolveTypeName(type: Int): String {
        return getString(when (type) {
            0 -> R.string.pref_btn_action_press
            1 -> R.string.pref_btn_action_lpress
            2 -> R.string.pref_btn_action_up
            3 -> R.string.pref_btn_action_down
            4 -> R.string.pref_btn_action_left
            5 -> R.string.pref_btn_action_right
            else -> throw IllegalArgumentException()
        })
    }

    class ItemModel(
        val binding: FragmentSoftButtonActionItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
