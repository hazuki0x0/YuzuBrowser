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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class ActionNameArrayAdapter(
        context: Context,
        val nameArray: ActionNameArray,
        private val listener: OnRecyclerListener
) : RecyclerView.Adapter<ActionNameArrayAdapter.ViewHolder>() {
    private val checked = BooleanArray(itemCount)
    private val inflater = LayoutInflater.from(context)
    private val iconPosDB = context.resources.getIntArray(R.array.action_values)
    private val icons = context.resources.obtainTypedArray(R.array.action_icons)
    private var mListener: OnSettingButtonListener? = null

    override fun getItemCount(): Int {
        return nameArray.actionList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun getName(position: Int): String {
        return nameArray.actionList[position]!!
    }

    fun getItemValue(position: Int): Int {
        return nameArray.actionValues[position]
    }

    private fun getIcon(position: Int): Drawable? {
        val iconPos = iconPosDB.indexOf(nameArray.actionValues[position])

        return if (iconPos >= 0) icons.getDrawable(iconPos) else null
    }

    fun isChecked(position: Int): Boolean {
        return checked[position]
    }

    fun clearChoices() {
        for (i in checked.indices) {
            checked[i] = false
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.select_action_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.icon.setImageDrawable(getIcon(position))
        holder.text.text = getName(position)

        val checked = isChecked(position)

        holder.checkBox.isChecked = checked

        if (SingleAction.checkSubPreference(getItemValue(position))) {
            holder.setting.visibility = View.VISIBLE
            holder.setting.isEnabled = checked
            holder.setting.imageAlpha = if (checked) 0xff else 0x88
            holder.setting.setOnClickListener { mListener?.invoke(position) }
        } else {
            holder.setting.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { listener.onRecyclerItemClicked(it, holder.adapterPosition) }
        holder.itemView.setOnLongClickListener { listener.onRecyclerItemLongClicked(it, holder.adapterPosition) }
    }

    fun toggleCheck(position: Int): Boolean {
        val newState = !checked[position]
        checked[position] = newState
        notifyItemChanged(position)
        return newState
    }

    fun setChecked(position: Int, value: Boolean) {
        checked[position] = value
    }

    fun setListener(mListener: OnSettingButtonListener) {
        this.mListener = mListener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iconImageView)
        val text: TextView = view.findViewById(R.id.nameTextView)
        val setting: ImageButton = view.findViewById(R.id.settingsButton)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }
}
