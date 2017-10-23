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

package jp.hazuki.yuzubrowser.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.search.settings.SearchSimpleIconView
import jp.hazuki.yuzubrowser.search.settings.SearchUrl

class SearchUrlSpinnerAdapter(context: Context, objects: List<SearchUrl>) : ArrayAdapter<SearchUrl>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.search_url_selected, parent, false)!!
        }
        val iconView = view.findViewById<SearchSimpleIconView>(R.id.iconColorView)

        iconView.setSearchUrl(getItem(position))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.serach_url_spinner, parent, false)!!
        }

        val searchUrl = getItem(position)

        view.findViewById<SearchSimpleIconView>(R.id.iconColorView).setSearchUrl(searchUrl)
        view.findViewById<TextView>(R.id.titleTextView).text = if (searchUrl.title.isNotEmpty()) searchUrl.title else searchUrl.url
        return view
    }
}
