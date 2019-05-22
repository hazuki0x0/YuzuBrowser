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

package jp.hazuki.yuzubrowser.search.presentation.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import jp.hazuki.yuzubrowser.search.presentation.widget.SearchSimpleIconView

class SearchUrlSpinnerAdapter(
    context: Context,
    objects: List<SearchUrl>,
    private val faviconManager: FaviconManager
) : ArrayAdapter<SearchUrl>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.search_url_selected, parent, false)
        val iconView = view.findViewById<SearchSimpleIconView>(R.id.iconColorView)

        val searchUrl = getItem(position)
        checkNotNull(searchUrl) { "position:$position is not available" }

        val url = searchUrl.url.replace("%s", "")

        val favicon = if (searchUrl.isUseFavicon) faviconManager[url] else null
        if (favicon != null) {
            iconView.setFavicon(favicon)
        } else {
            iconView.setSearchUrl(searchUrl)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.serach_url_spinner, parent, false)

        val searchUrl = getItem(position)
        checkNotNull(searchUrl) {"position:$position is not available"}

        val simpleIconView: SearchSimpleIconView = view.findViewById(R.id.iconColorView)
        val url = searchUrl.url.replace("%s", "")

        val favicon = if (searchUrl.isUseFavicon) faviconManager[url] else null
        if (favicon != null) {
            simpleIconView.setFavicon(favicon)
        } else {
            simpleIconView.setSearchUrl(searchUrl)
        }

        view.findViewById<TextView>(R.id.titleTextView).text = if (searchUrl.title.isNotEmpty()) searchUrl.title else searchUrl.url
        return view
    }
}
