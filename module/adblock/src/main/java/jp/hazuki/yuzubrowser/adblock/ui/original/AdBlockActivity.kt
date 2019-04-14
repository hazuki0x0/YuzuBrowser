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

package jp.hazuki.yuzubrowser.adblock.ui.original

import android.net.Uri
import android.os.Bundle
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlock
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlockManager
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class AdBlockActivity : ThemeActivity(), AdBlockMainFragment.OnAdBlockMainListener, AdBlockFragment.AdBlockFragmentListener, AdBlockImportFragment.OnImportListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AdBlockMainFragment())
                    .commit()

        if (intent != null && intent.action != null) {
            when (intent.action) {
                ACTION_OPEN_BLACK -> openBlackList()
                ACTION_OPEN_WHITE -> openWhiteList()
                ACTION_OPEN_WHITE_PAGE -> openWhitePageList()
            }
        }
    }

    override fun openBlackList() {
        openList(AdBlockManager.TYPE_BLACK_TABLE)
    }

    override fun openWhiteList() {
        openList(AdBlockManager.TYPE_WHITE_TABLE)
    }

    override fun openWhitePageList() {
        openList(AdBlockManager.TYPE_WHITE_PAGE_TABLE)
    }

    private fun openList(type: Int) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, AdBlockFragment(type), TAG_LIST)
                .addToBackStack("type:$type")
                .commit()
    }

    override fun onImport(adBlocks: List<AdBlock>) {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_LIST)
        (fragment as? AdBlockFragment)?.addAll(adBlocks)
    }


    override fun setFragmentTitle(type: Int) {
        when (type) {
            AdBlockManager.TYPE_BLACK_TABLE -> setTitle(R.string.pref_ad_block_black)
            AdBlockManager.TYPE_WHITE_TABLE -> setTitle(R.string.pref_ad_block_white)
            AdBlockManager.TYPE_WHITE_PAGE_TABLE -> setTitle(R.string.pref_ad_block_white_page)
        }
    }

    override fun requestImport(uri: Uri) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, AdBlockImportFragment(uri))
                .addToBackStack("")
                .commit()
    }

    override fun getExportFileName(type: Int) = when (type) {
        AdBlockManager.TYPE_BLACK_TABLE -> "black_list.txt"
        AdBlockManager.TYPE_WHITE_TABLE -> "white_list.txt"
        AdBlockManager.TYPE_WHITE_PAGE_TABLE -> "white_page_list.txt"
        else -> throw IllegalArgumentException()
    }

    companion object {
        const val ACTION_OPEN_BLACK = "jp.hazuki.yuzubrowser.adblock.AdBlockActivity.action.open.black"
        const val ACTION_OPEN_WHITE = "jp.hazuki.yuzubrowser.adblock.AdBlockActivity.action.open.white"
        const val ACTION_OPEN_WHITE_PAGE = "jp.hazuki.yuzubrowser.adblock.AdBlockActivity.action.open.whitepage"

        private const val TAG_LIST = "list"
    }
}
