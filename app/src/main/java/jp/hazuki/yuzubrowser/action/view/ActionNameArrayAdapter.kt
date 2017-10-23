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

package jp.hazuki.yuzubrowser.action.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.ActionNameArray
import jp.hazuki.yuzubrowser.action.SingleAction

class ActionNameArrayAdapter(context: Context, val nameArray: ActionNameArray) : BaseAdapter() {
    private val checked = BooleanArray(count)
    private val inflater = LayoutInflater.from(context)
    private var mListener: OnSettingButtonListener? = null

    override fun getCount(): Int {
        return nameArray.actionList.size
    }

    override fun getItem(position: Int): Any {
        return nameArray.actionList[position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getName(position: Int): String {
        return nameArray.actionList[position]!!
    }

    fun getItemValue(position: Int): Int {
        return nameArray.actionValues[position]
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.select_action_item, parent, false)
            holder = ViewHolder(view)
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.text.text = getName(position)

        val checked = isChecked(position)

        holder.checkBox.isChecked = checked

        if (SingleAction.checkSubPreference(getItemValue(position))) {
            holder.setting.visibility = View.VISIBLE
            holder.setting.isEnabled = checked
            holder.setting.imageAlpha = if (checked) 0xff else 0x88
            holder.setting.setOnClickListener {
                if (mListener != null)
                    mListener!!.invoke(position)
            }
        } else {
            holder.setting.visibility = View.GONE
        }

        return view
    }

    fun toggleCheck(position: Int): Boolean {
        checked[position] = !checked[position]
        notifyDataSetChanged()
        return checked[position]
    }

    fun setChecked(position: Int, value: Boolean) {
        checked[position] = value
    }

    fun setListener(mListener: OnSettingButtonListener) {
        this.mListener = mListener
    }

    internal class ViewHolder(view: View) {
        val text: TextView = view.findViewById(R.id.nameTextView)
        val setting: ImageButton = view.findViewById(R.id.settingsButton)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        init {
            view.tag = this
        }
    }
}
