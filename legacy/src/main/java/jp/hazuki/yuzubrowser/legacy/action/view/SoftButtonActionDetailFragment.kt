/*
 * Copyright (C) 2017-2021 Hazuki
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import jp.hazuki.yuzubrowser.core.utility.extensions.isInstanceOf
import jp.hazuki.yuzubrowser.legacy.action.ActionIconMap
import jp.hazuki.yuzubrowser.legacy.action.ActionManager
import jp.hazuki.yuzubrowser.legacy.action.ActionNameMap
import jp.hazuki.yuzubrowser.legacy.action.SoftButtonActionArrayManagerBase
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionManager
import jp.hazuki.yuzubrowser.legacy.databinding.FragmentSoftButtonActionDetailBinding

class SoftButtonActionDetailFragment : Fragment() {
    private val activityViewModel by activityViewModels<SoftButtonActionViewModel> {
        SoftButtonActionViewModel.Factory(
            ActionNameMap(resources),
            ActionIconMap(resources)
        )
    }

    private val viewModel by viewModels<SoftButtonActionDetailViewModel>()

    private var bindingImpl: FragmentSoftButtonActionDetailBinding? = null

    private val binding: FragmentSoftButtonActionDetailBinding
        get() = bindingImpl!!

    private lateinit var manager: ActionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bindingImpl = FragmentSoftButtonActionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.let {
            it.names = activityViewModel.actionNames
            it.icons = activityViewModel.actionIcons
            it.onClick.observe(viewLifecycleOwner, this::onClick)
        }

        binding.let {
            it.lifecycleOwner = viewLifecycleOwner
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = SoftButtonDetailAdapter(viewLifecycleOwner, viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        val arguments = requireArguments()
        val context = requireContext()

        val type = arguments.getInt(ACTION_TYPE)
        val id = arguments.getInt(ACTION_ID)
        val pos = arguments.getInt(ACTION_POSITION)

        manager = ActionManager.getActionManager(context, type)
        val manager = manager
        if (manager is SoftButtonActionArrayManagerBase) {
            val file = manager.getActionArrayFile(id)
            viewModel.action.value = file[pos]
        } else if (manager is SoftButtonActionManager) {
            viewModel.action.value = manager.btn_url_center
        }
        binding.adapter?.notifyDataSetChanged()

        parentFragmentManager.setFragmentResult(RESTART, Bundle())
    }

    private fun onClick(actionType: Int) {
        val activity = requireActivity()
        val arguments = requireArguments()

        var actionId = arguments.getInt(ACTION_ID)
        val type = arguments.getInt(ACTION_TYPE)
        val pos = arguments.getInt(ACTION_POSITION)

        manager.isInstanceOf<SoftButtonActionArrayManagerBase> {
            actionId = it.makeActionIdFromPosition(actionId, pos)
        }

        actionId = when (actionType) {
            0 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_PRESS
            1 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_LPRESS
            2 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_UP
            3 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_DOWN
            4 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_LEFT
            5 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_RIGHT
            else -> throw IllegalArgumentException("Unknown type:$type")
        }

        ActionActivity.Builder(activity)
            .setTitle(activity.title)
            .setActionManager(type, actionId)
            .show()
    }

    companion object {
        private const val ACTION_TYPE = "type"
        private const val ACTION_ID = "id"
        private const val ACTION_POSITION = "pos"

        const val RESTART = "detail_restart"

        operator fun invoke(type: Int, id: Int, position: Int) =
            SoftButtonActionDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ACTION_TYPE, type)
                    putInt(ACTION_ID, id)
                    putInt(ACTION_POSITION, position)
                }
            }
    }
}
