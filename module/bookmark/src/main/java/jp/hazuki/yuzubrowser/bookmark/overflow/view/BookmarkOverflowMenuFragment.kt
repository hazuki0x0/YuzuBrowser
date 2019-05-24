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

package jp.hazuki.yuzubrowser.bookmark.overflow.view

import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import jp.hazuki.bookmark.R
import jp.hazuki.bookmark.databinding.FragmentBookmarkOverflowBinding
import jp.hazuki.yuzubrowser.bookmark.overflow.HideMenuType
import jp.hazuki.yuzubrowser.bookmark.overflow.MenuType
import jp.hazuki.yuzubrowser.bookmark.overflow.viewmodel.OverflowMenuViewModel
import jp.hazuki.yuzubrowser.ui.extensions.get
import javax.inject.Inject

class BookmarkOverflowMenuFragment : DaggerFragment() {
    @Inject
    internal lateinit var factory: OverflowMenuViewModel.Factory

    private lateinit var mainViewModel: OverflowMenuViewModel

    private lateinit var binding: FragmentBookmarkOverflowBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBookmarkOverflowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding
        val activity = requireActivity()
        val arguments = arguments ?: throw IllegalArgumentException()

        mainViewModel = ViewModelProviders.of(this, factory).get()
        binding.lifecycleOwner = this

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.adapter = OverflowMenuAdapter()
        binding.viewModel = mainViewModel

        val type = arguments.getInt(TYPE)
        val builder = MenuBuilder(activity)
        MenuInflater(activity).inflate(getMenuRes(type), builder)
        val menuList = mutableListOf<MenuItem>()
        builder.forEach { menuList.add(it) }
        mainViewModel.setOverflowMenus(type, menuList)
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.save(arguments!!.getInt(TYPE))
    }

    private fun getMenuRes(type: Int): Int {
        return when (type) {
            MenuType.SITE -> R.menu.bookmark_site_menu
            MenuType.FOLDER -> R.menu.bookmark_folder_menu
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        private const val TYPE = "type"

        fun create(@HideMenuType type: Int): BookmarkOverflowMenuFragment {
            return BookmarkOverflowMenuFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE, type)
                }
            }
        }
    }
}
