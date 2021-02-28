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
package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebViewDatabase
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import dagger.hilt.android.AndroidEntryPoint
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.browser.BrowserManager
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import jp.hazuki.yuzubrowser.ui.preference.CustomDialogPreference
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import javax.inject.Inject

class ClearBrowserDataAlertDialog @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : CustomDialogPreference(context, attrs) {
    override fun crateCustomDialog(): CustomDialogFragment = ClearDialog()

    @AndroidEntryPoint
    class ClearDialog : CustomDialogFragment() {
        private var mSelected = 0
        private var mArrayMax = 0
        private lateinit var ids: IntArray

        @Inject
        lateinit var faviconManager: FaviconManager

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            mSelected = AppPrefs.clear_data_default.get()
            val context = requireContext()
            val arrays = context.resources.getStringArray(R.array.clear_browser_data)
            ids = context.resources.getIntArray(R.array.clear_browser_data_id)
            if (arrays.size != ids.size) {
                throw RuntimeException()
            }
            mArrayMax = arrays.size
            val listView = ListView(context)
            listView.adapter = ArrayAdapter(context, R.layout.select_dialog_multichoice, arrays)
            listView.itemsCanFocus = false
            listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            for (i in 0 until mArrayMax) {
                val shifted = 1 shl i
                listView.setItemChecked(i, mSelected and shifted == shifted)
            }
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                mSelected = if (listView.isItemChecked(position)) {
                    mSelected or (1 shl position)
                } else {
                    mSelected and (1 shl position).inv()
                }
            }
            return AlertDialog.Builder(activity)
                .setTitle(R.string.pref_clear_browser_data)
                .setView(listView)
                .setPositiveButton(android.R.string.ok) { _, _ -> onClickPositiveButton() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        private fun onClickPositiveButton() {
            for (i in 0 until mArrayMax) {
                val shifted = 1 shl i
                if (mSelected and shifted == shifted) runAction(ids[i])
            }
            AppPrefs.clear_data_default.set(mSelected)
            AppPrefs.commit(requireContext().applicationContext, AppPrefs.clear_data_default)
        }

        private fun runAction(i: Int) {
            val con = requireContext()
            when (i) {
                0 -> {
                    BrowserManager.clearAppCacheFile(con.applicationContext)
                    BrowserManager.clearCache(con.applicationContext)
                }
                1 -> CookieManager.getInstance().removeAllCookies(null)
                2 -> WebViewDatabase.getInstance(con.applicationContext).clearHttpAuthUsernamePassword()
                3 -> WebViewDatabase.getInstance(con).clearFormData()
                4 -> BrowserManager.clearWebDatabase()
                5 -> BrowserManager.clearGeolocation()
                6 -> BrowserHistoryManager.getInstance(con.applicationContext).deleteAll()
                7 -> con.applicationContext.contentResolver.delete(
                    (con.applicationContext as BrowserApplication).providerManager.suggestProvider.uriLocal, null, null)
                8 -> faviconManager.clear()
            }
        }
    }
}
