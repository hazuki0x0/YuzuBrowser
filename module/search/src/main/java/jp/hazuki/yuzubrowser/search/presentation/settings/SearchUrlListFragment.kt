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

package jp.hazuki.yuzubrowser.search.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.databinding.SearchSettingsFragmentBinding
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.extensions.get
import jp.hazuki.yuzubrowser.ui.extensions.observe
import jp.hazuki.yuzubrowser.ui.widget.recycler.RecyclerMenu
import javax.inject.Inject

class SearchUrlListFragment : DaggerFragment(), SearchSettingDialog.OnUrlEditedListener, RecyclerMenu.OnRecyclerMenuListener, DeleteDialogCompat.OnDelete, SearchUrlAdapter.OnSearchUrlClickListener {

    private lateinit var binding: SearchSettingsFragmentBinding
    private lateinit var viewModel: SearchSettingsViewModel
    @Inject
    internal lateinit var factory: SearchSettingsViewModel.Factory
    @Inject
    internal lateinit var faviconManager: FaviconManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        viewModel = ViewModelProviders.of(this, factory).get()
        viewModel.removedItem.observe(this) {
            Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    viewModel.resetRemovedItem()
                }
                .show()
        }
        viewModel.onFabClick.observe(this) {
            SearchSettingDialog.newInstance(-1, null).show(childFragmentManager, "edit")
        }

        val binding = binding
        binding.lifecycleOwner = this
        binding.adapter = SearchUrlAdapter(faviconManager, this)
        binding.model = viewModel

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        val touchHelper = ItemTouchHelper(viewModel.touchHelperCallback)
        binding.recyclerView.addItemDecoration(touchHelper)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.init()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return SearchSettingsFragmentBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }
    }

    override fun onEdit(position: Int, searchUrl: SearchUrl) {
        SearchSettingDialog.newInstance(position, searchUrl).show(childFragmentManager, "edit")
    }

    override fun onOpenMenu(view: View, position: Int) {
        RecyclerMenu(requireActivity(), view, position, this, viewModel).show()
    }

    override fun onUrlEdited(index: Int, url: SearchUrl) {
        if (index >= 0) {
            viewModel.update(index, url)
        } else {
            viewModel.add(url)
        }
    }

    override fun onDeleteClicked(position: Int) {
        if (viewModel.size > 1) {
            DeleteDialogCompat.newInstance(context, R.string.confirm, R.string.confirm_delete_search_url, position)
                .show(childFragmentManager, "delete")
        }
    }

    override fun onDelete(position: Int) {
        if (viewModel.size > position) {
            viewModel.remove(position)
        }
    }
}
