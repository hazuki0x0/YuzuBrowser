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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import jp.hazuki.yuzubrowser.core.utility.extensions.clipboardText
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.databinding.SearchActivityBinding
import jp.hazuki.yuzubrowser.search.databinding.SearchSeachBarBinding
import jp.hazuki.yuzubrowser.search.presentation.widget.SearchButton
import jp.hazuki.yuzubrowser.ui.INTENT_EXTRA_MODE_FULLSCREEN
import jp.hazuki.yuzubrowser.ui.app.DaggerThemeActivity
import jp.hazuki.yuzubrowser.ui.extensions.get
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import javax.inject.Inject

class SearchActivity : DaggerThemeActivity(), SearchButton.Callback, SearchSuggestAdapter.OnSearchSelectedListener, SuggestDeleteDialog.OnDeleteQuery {

    @Inject
    internal lateinit var factory: SearchViewModel.Factory
    @Inject
    internal lateinit var faviconManager: FaviconManager

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: SearchActivityBinding
    private lateinit var barBinding: SearchSeachBarBinding

    private var appData: Bundle? = null
    private var openNewTab: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.search_activity)
        barBinding = SearchSeachBarBinding.inflate(layoutInflater, binding.rootLayout, false)

        binding.lifecycleOwner = this
        barBinding.lifecycleOwner = this

        val intent = intent ?: throw IllegalStateException("Intent is null")
        if (intent.getBooleanExtra(INTENT_EXTRA_MODE_FULLSCREEN, AppPrefs.fullscreen.get()))
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val bottomBoxMode = intent.getBooleanExtra(EXTRA_REVERSE, false)
        if (bottomBoxMode) {
            binding.bottomBox.addView(barBinding.root)
        } else {
            binding.topBox.addView(barBinding.root)
        }
        barBinding.callback = this

        viewModel = ViewModelProviders.of(this, factory).get()
        viewModel.also {
            binding.model = it
            barBinding.viewModel = it

            barBinding.searchUrlSpinner.adapter = SearchUrlSpinnerAdapter(
                this, it.suggestProviders.urls, faviconManager)
            it.providerSelection.set(it.suggestProviders.getSelectedIndex())
        }

        if (!AppPrefs.searchUrlShowIcon.get()) {
            barBinding.searchUrlSpinner.visibility = View.GONE
        }

        val searchButton = barBinding.searchButton
        val editText = barBinding.editText
        val recyclerView = binding.recyclerView

        ThemeData.getInstance()?.let { themeData ->
            if (themeData.toolbarBackgroundColor != 0)
                barBinding.root.setBackgroundColor(themeData.toolbarBackgroundColor)
            val textColor = themeData.toolbarTextColor
            if (textColor != 0) {
                editText.setTextColor(textColor)
                editText.setHintTextColor(textColor and 0xffffff or 0x55000000)
            }
            if (themeData.toolbarImageColor != 0)
                searchButton.setColorFilter(themeData.toolbarImageColor)
            if (themeData.statusBarColor != 0) {
                window.run {
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    statusBarColor = themeData.statusBarColor
                    decorView.systemUiVisibility = ThemeData.getSystemUiVisibilityFlag()
                }
            }
        }

        searchButton.setSense(AppPrefs.swipebtn_sensitivity.get())

        editText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu) = true

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = true

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val text = viewModel.query

                var min = 0
                var max = text.length

                if (editText.isFocused) {
                    val selStart = editText.selectionStart
                    val selEnd = editText.selectionEnd

                    min = Math.max(0, Math.min(selStart, selEnd))
                    max = Math.max(0, Math.max(selStart, selEnd))
                }

                when (item.itemId) {
                    android.R.id.copy -> if (min == 0 && max == text.length && viewModel.decodedInitQuery == text) {
                        clipboardText = viewModel.initQuery!!
                        mode.finish()
                        return true
                    }
                    android.R.id.cut -> if (min == 0 && max == text.length && viewModel.decodedInitQuery == text) {
                        clipboardText = viewModel.initQuery!!
                        editText.setText("")
                        mode.finish()
                        return true
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) = Unit
        }

        appData = intent.getBundleExtra(EXTRA_APP_DATA)
        openNewTab = intent.getBooleanExtra(EXTRA_OPEN_NEW_TAB, false)

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            if (bottomBoxMode) reverseLayout = true
        }
        recyclerView.setOnOutSideClickListener { finish() }
        recyclerView.setOnClickListener { finish() }

        binding.adapter = SearchSuggestAdapter().also { it.listener = this }

        val initQuery = intent.getStringExtra(EXTRA_QUERY)
        if (initQuery != null) {
            viewModel.setInitQuery(initQuery)
            editText.setText(initQuery)
            viewModel.setQuery(initQuery)
        } else {
            editText.setText("")
            viewModel.setQuery("")
        }

        if (intent.getBooleanExtra(EXTRA_SELECT_INITIAL_QUERY, true)) {
            editText.selectAll()
        }
    }

    override fun onStart() {
        super.onStart()
        if (AppPrefs.searchUrlShowIcon.get() && AppPrefs.searchUrlSaveSwitching.get())
            viewModel.providerSelection.addOnPropertyChangedCallback(callback)
    }

    override fun onPause() {
        super.onPause()
        viewModel.providerSelection.removeOnPropertyChangedCallback(callback)
    }

    override fun forceOpenUrl() {
        finish(SearchViewModel.SEARCH_MODE_URL)
    }

    override fun forceSearchWord() {
        finish(SearchViewModel.SEARCH_MODE_WORD)
    }

    override fun autoSearch() {
        finish(SearchViewModel.SEARCH_MODE_AUTO)
    }

    private fun finish(mode: Int) {
        val result = viewModel.getFinishResult(mode)
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_QUERY, result.query)
            putExtra(EXTRA_SEARCH_MODE, mode)
            putExtra(EXTRA_SEARCH_URL, result.url)
            putExtra(EXTRA_OPEN_NEW_TAB, openNewTab)
            appData?.let { putExtra(EXTRA_APP_DATA, it) }
        })
        finish()
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
                if (results.isNotEmpty()) {
                    val query = results[0]
                    barBinding.editText.run {
                        setText(query)
                        selectAll()
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onSelectedQuery(query: String) {
        viewModel.setQuery(query)
        finish(SearchViewModel.SEARCH_MODE_AUTO)
    }

    override fun onInputQuery(query: String) {
        barBinding.editText.run {
            setText(query)
            setSelection(query.length)
        }
    }

    override fun onDeleteQuery(query: String) {
        SuggestDeleteDialog.newInstance(query).show(supportFragmentManager, "delete")
    }

    override fun onDelete(query: String) {
        viewModel.deleteQuery(query)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            finish()
        }
        return super.onTouchEvent(event)
    }

    private val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            AppPrefs.search_url.set(viewModel.suggestProviders[viewModel.providerSelection.get()].url)
            AppPrefs.commit(this@SearchActivity, AppPrefs.search_url)
            viewModel.saveProvider()
        }
    }

    companion object {
        const val EXTRA_QUERY = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.query"
        const val EXTRA_SELECT_INITIAL_QUERY = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.selectinitquery"
        const val EXTRA_APP_DATA = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.appdata"
        const val EXTRA_SEARCH_MODE = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.searchmode"
        const val EXTRA_SEARCH_URL = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.searchUrl"
        const val EXTRA_OPEN_NEW_TAB = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.openNewTab"
        const val EXTRA_REVERSE = "jp.hazuki.yuzubrowser.legacy.search.SearchActivity.extra.reverse"

        const val SEARCH_MODE_AUTO = SearchViewModel.SEARCH_MODE_AUTO
        const val SEARCH_MODE_URL = SearchViewModel.SEARCH_MODE_URL
        const val SEARCH_MODE_WORD = SearchViewModel.SEARCH_MODE_WORD

        private const val RESULT_REQUEST_SPEECH = 1
    }
}
