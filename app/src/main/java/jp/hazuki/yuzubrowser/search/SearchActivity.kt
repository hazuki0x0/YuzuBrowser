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

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager
import jp.hazuki.yuzubrowser.history.BrowserHistoryManager
import jp.hazuki.yuzubrowser.search.settings.SearchUrlManager
import jp.hazuki.yuzubrowser.search.suggest.SuggestHistory
import jp.hazuki.yuzubrowser.search.suggest.SuggestItem
import jp.hazuki.yuzubrowser.search.suggest.Suggestion
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.theme.ThemeData
import jp.hazuki.yuzubrowser.utils.*
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.utils.view.recycler.OutSideClickableRecyclerView
import java.util.*

class SearchActivity : ThemeActivity(), TextWatcher, SearchButton.Callback, SearchRecyclerAdapter.OnSuggestSelectListener, SuggestDeleteDialog.OnDeleteQuery {

    private lateinit var mContentUri: Uri
    private var mAppData: Bundle? = null

    private lateinit var editText: EditText
    private lateinit var adapter: SearchRecyclerAdapter
    private lateinit var searchUrlSpinner: Spinner
    private lateinit var manager: SearchUrlManager

    private var initQuery: String? = null
    private var initDecodedQuery = ""

    private var openNewTab: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reverse = intent.getBooleanExtra(EXTRA_REVERSE, false)

        if (reverse)
            setContentView(R.layout.search_activity_reverse)
        else
            setContentView(R.layout.search_activity)

        editText = findViewById(R.id.editText)
        val searchButton = findViewById<SearchButton>(R.id.searchButton)
        val recyclerView = findViewById<OutSideClickableRecyclerView>(R.id.recyclerView)

        searchUrlSpinner = findViewById(R.id.searchUrlSpinner)
        manager = SearchUrlManager(this)
        searchUrlSpinner.adapter = SearchUrlSpinnerAdapter(this, manager)
        searchUrlSpinner.setSelection(manager.getSelectedIndex())

        if (!AppData.search_url_show_icon.get()) {
            searchUrlSpinner.visibility = View.GONE
        }

        recyclerView.setOnOutSideClickListener { this.finish() }

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(
                this, ResourcesCompat.getColor(resources, R.color.divider, theme)))

        if (reverse) {
            layoutManager.reverseLayout = true
        }

        adapter = SearchRecyclerAdapter(this, ArrayList(), this)
        recyclerView.adapter = adapter

        recyclerView.setOnClickListener { _ -> finish() }

        if (ThemeData.isEnabled()) {
            if (ThemeData.getInstance().toolbarBackgroundColor != 0)
                findViewById<View>(R.id.search_bar_container).setBackgroundColor(ThemeData.getInstance().toolbarBackgroundColor)
            val textColor = ThemeData.getInstance().toolbarTextColor
            if (textColor != 0) {
                editText.setTextColor(textColor)
                editText.setHintTextColor(textColor and 0xffffff or 0x55000000)
            }
            if (ThemeData.getInstance().toolbarImageColor != 0)
                searchButton.setColorFilter(ThemeData.getInstance().toolbarImageColor)
            if (ThemeData.getInstance().statusBarColor != 0) {
                window.run {
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    statusBarColor = ThemeData.getInstance().statusBarColor
                    decorView.systemUiVisibility = ThemeData.getSystemUiVisibilityFlag()
                }
            }
        }

        editText.addTextChangedListener(this)
        editText.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                finishWithResult(editText.text.toString(), SEARCH_MODE_AUTO)
                return@setOnEditorActionListener true
            }
            false
        }
        editText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val text = editText.text

                var min = 0
                var max = text.length

                if (editText.isFocused) {
                    val selStart = editText.selectionStart
                    val selEnd = editText.selectionEnd

                    min = Math.max(0, Math.min(selStart, selEnd))
                    max = Math.max(0, Math.max(selStart, selEnd))
                }

                when (item.itemId) {
                    android.R.id.copy -> if (min == 0 && max == text.length && initDecodedQuery == text.toString()) {
                        ClipboardUtils.setClipboardText(this@SearchActivity, initQuery, false)
                        mode.finish()
                        return true
                    }
                    android.R.id.cut -> if (min == 0 && max == text.length && initDecodedQuery == text.toString()) {
                        ClipboardUtils.setClipboardText(this@SearchActivity, initQuery, false)
                        editText.setText("")
                        mode.finish()
                        return true
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) = Unit
        }

        searchButton.setActionCallback(this)
        searchButton.setSense(AppData.swipebtn_sensitivity.get())

        val intent = intent
        if (intent != null) {
            if (intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, AppData.fullscreen.get()))
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            mContentUri = intent.getParcelableExtra(EXTRA_URI) ?: when (AppData.search_suggest.get()) {
                0 -> SuggestProvider.URI_NORMAL
                1 -> SuggestProvider.URI_NET
                2 -> SuggestProvider.URI_LOCAL
                else -> SuggestProvider.URI_NORMAL
            }

            initQuery = intent.getStringExtra(EXTRA_QUERY)
            if (initQuery != null) {
                initDecodedQuery = UrlUtils.decodeUrl(initQuery)
                editText.setText(initDecodedQuery)
            }

            if (intent.getBooleanExtra(EXTRA_SELECT_INITIAL_QUERY, true)) {
                editText.selectAll()
            }

            mAppData = intent.getBundleExtra(EXTRA_APP_DATA)

            openNewTab = intent.getBooleanExtra(EXTRA_OPEN_NEW_TAB, false)
        } else {
            throw IllegalStateException("Intent is null")
        }
    }

    override fun afterTextChanged(s: Editable) {
        setQuery(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

    private fun setQuery(query: String) = ui {
        val search = async { getSearchQuery(query) }
        val histories = if (AppData.search_suggest_histories.get()) async { getHistoryQuery(query) } else null
        val bookmarks = if (AppData.search_suggest_bookmarks.get()) async { getBookmarkQuery(query) } else null

        val suggestions = mutableListOf<SuggestItem>()
        suggestions.addAll(search.await())
        histories?.run { suggestions.addAll(await()) }
        bookmarks?.run { suggestions.addAll(await()) }

        adapter.clear()
        adapter.addAll(suggestions)
        adapter.notifyDataSetChanged()
    }

    private fun getSearchQuery(query: String): List<SuggestItem> {
        val uri = mContentUri.buildUpon().appendQueryParameter("q", query).build()

        val suggestions = ArrayList<SuggestItem>()

        contentResolver.query(uri, null, null, null, null).use { c ->
            if (c != null) {
                val COL_QUERY = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
                val COL_HISTORY = c.getColumnIndex(SuggestProvider.SUGGEST_HISTORY)
                while (c.moveToNext()) {
                    suggestions.add(Suggestion(c.getString(COL_QUERY), c.getInt(COL_HISTORY) == 1))
                }
            }
        }
        return suggestions
    }

    private fun getHistoryQuery(query: String): List<SuggestHistory> {
        if (query.isEmpty()) {
            return listOf()
        }

        val histories = BrowserHistoryManager.getInstance(this).search(query, 0, 5)
        val list = ArrayList<SuggestHistory>(histories.size)
        histories.forEach {
            list.add(SuggestHistory(it.title ?: "", it.url ?: ""))
        }
        return list
    }

    private fun getBookmarkQuery(query: String): List<SuggestHistory> {
        if (query.isEmpty()) {
            return listOf()
        }

        val bookmarks = BookmarkManager.getInstance(this).search(query)
        val list = ArrayList<SuggestHistory>(if (bookmarks.size >= 5) 5 else bookmarks.size)
        bookmarks.asSequence().take(5).forEach {
            list.add(SuggestHistory(it.title, it.url))
        }
        return list
    }

    override fun onSelectSuggest(query: String) {
        finishWithResult(query, SEARCH_MODE_AUTO)
    }

    override fun onInputSuggest(query: String) {
        editText.setText(query)
        editText.setSelection(query.length)
    }

    override fun onLongClicked(query: String) {
        SuggestDeleteDialog.newInstance(query)
                .show(supportFragmentManager, "delete")
    }

    override fun onDelete(query: String) {
        contentResolver.delete(mContentUri, SearchManager.SUGGEST_COLUMN_QUERY + " = ?", arrayOf(query))
        setQuery(query)
    }

    private fun finishWithResult(query: String, mode: Int) {
        if (!AppData.private_mode.get() && !TextUtils.isEmpty(query) && mode != SEARCH_MODE_URL && !WebUtils.isUrl(query)) {
            val values = ContentValues()
            values.put(SearchManager.SUGGEST_COLUMN_QUERY, query)
            contentResolver.insert(mContentUri, values)
        }
        val data = Intent()
        data.putExtra(EXTRA_QUERY, query)
        data.putExtra(EXTRA_SEARCH_MODE, mode)
        data.putExtra(EXTRA_SEARCH_URL, manager[searchUrlSpinner.selectedItemPosition].url)
        data.putExtra(EXTRA_OPEN_NEW_TAB, openNewTab)
        if (mAppData != null)
            data.putExtra(EXTRA_APP_DATA, mAppData)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun forceOpenUrl() {
        finishWithResult(editText.text.toString(), SEARCH_MODE_URL)
    }

    override fun forceSearchWord() {
        finishWithResult(editText.text.toString(), SEARCH_MODE_WORD)
    }

    override fun autoSearch() {
        finishWithResult(editText.text.toString(), SEARCH_MODE_AUTO)
    }

    override fun recognizeSpeech() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            startActivityForResult(intent, RESULT_REQUEST_SPEECH)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_REQUEST_SPEECH -> {
                if (resultCode != RESULT_OK || data == null) return
                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!results.isEmpty()) {
                    val query = results[0]
                    editText.setText(query)
                    editText.setSelection(query.length)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            finish()
        }
        return super.onTouchEvent(event)
    }

    override fun lightThemeResource(): Int {
        return R.style.BrowserMinThemeLight_Transparent
    }

    companion object {
        const val EXTRA_URI = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.uri"
        const val EXTRA_QUERY = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.query"
        const val EXTRA_SELECT_INITIAL_QUERY = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.selectinitquery"
        const val EXTRA_APP_DATA = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.appdata"
        const val EXTRA_SEARCH_MODE = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.searchmode"
        const val EXTRA_SEARCH_URL = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.searchUrl"
        const val EXTRA_OPEN_NEW_TAB = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.openNewTab"
        const val EXTRA_REVERSE = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.reverse"
        const val SEARCH_MODE_AUTO = 0
        const val SEARCH_MODE_URL = 1
        const val SEARCH_MODE_WORD = 2

        private const val RESULT_REQUEST_SPEECH = 0
    }
}
