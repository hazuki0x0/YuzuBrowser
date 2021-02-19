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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionIconMap
import jp.hazuki.yuzubrowser.legacy.action.ActionManager
import jp.hazuki.yuzubrowser.legacy.action.ActionNameMap
import jp.hazuki.yuzubrowser.legacy.action.SoftButtonActionArrayManagerBase
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionArrayFile
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.RecyclerFabFragment
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.extensions.applyIconColor
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class SoftButtonActionArrayFragment : RecyclerFabFragment(), OnRecyclerListener, DeleteDialogCompat.OnDelete {

    private val activityViewModel by activityViewModels<SoftButtonActionViewModel> {
        SoftButtonActionViewModel.Factory(
            ActionNameMap(resources),
            ActionIconMap(resources)
        )
    }

    private var mActionType: Int = 0
    private var mActionId: Int = 0
    private lateinit var actionArray: SoftButtonActionArrayFile
    private lateinit var actionManager: SoftButtonActionArrayManagerBase
    private lateinit var adapter: ActionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(SoftButtonActionDetailFragment.RESTART) { _, _ ->
            adapter.notifyDataSetChanged()
            checkMax()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity ?: return

        setHasOptionsMenu(true)
        initData()
        val actionNames = activityViewModel.actionNames
        val actionIcons = activityViewModel.actionIcons
        val list = actionArray.list
        adapter = ActionListAdapter(activity, list, actionNames, actionIcons, this)
        setRecyclerViewAdapter(adapter)
        checkMax()
    }

    override fun onMove(recyclerView: RecyclerView, fromIndex: Int, toIndex: Int): Boolean {
        adapter.move(fromIndex, toIndex)
        return true
    }

    override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        actionArray.write(requireContext().applicationContext)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        onListItemClick(position)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        DeleteDialogCompat.newInstance(activity, R.string.confirm, R.string.confirm_delete_button, position)
            .show(childFragmentManager, "delete")
        return true
    }

    override fun onDelete(position: Int) {
        actionArray.list.removeAt(position)
        actionArray.write(requireContext())
        adapter.notifyDataSetChanged()
    }

    override fun onAddButtonClick() {
        actionArray.list.add(SoftButtonActionFile())
        actionArray.write(requireContext())
        onListItemClick(actionArray.list.size - 1)
    }

    private fun onListItemClick(position: Int) {
        parentFragmentManager.commit {
            replace(R.id.container, SoftButtonActionDetailFragment(mActionType, mActionId, position))
            addToBackStack(null)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, index: Int) {
        val file = actionArray.list.removeAt(index)
        val context = requireActivity().applicationContext
        adapter.notifyDataSetChanged()
        checkMax()
        Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
            .setAction(R.string.undo) {
                actionArray.list.add(index, file)
                adapter.notifyDataSetChanged()
                checkMax()
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        actionArray.write(context)
                    }
                }
            })
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_REQUEST_ADD -> {
                adapter.notifyDataSetChanged()
                checkMax()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort, menu)
        applyIconColor(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                val next = !adapter.isSortMode
                adapter.isSortMode = next

                Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    override val isLongPressDragEnabled
        get() = adapter.isSortMode

    private fun checkMax() {
        setAddButtonEnabled(actionManager.max >= adapter.itemCount)
    }

    private fun initData() {
        val arguments = arguments ?: throw IllegalArgumentException()
        val context = requireContext().applicationContext

        mActionType = arguments.getInt(ACTION_TYPE)
        mActionId = arguments.getInt(ACTION_ID)

        actionManager = ActionManager.getActionManager(context, mActionType) as SoftButtonActionArrayManagerBase

        actionArray = actionManager.getActionArrayFile(mActionId)
    }

    private class ActionListAdapter(
        context: Context,
        list: MutableList<SoftButtonActionFile>,
        private val actionNames: ActionNameMap,
        private val actionIcons: ActionIconMap,
        listener: OnRecyclerListener
    ) : ArrayRecyclerAdapter<SoftButtonActionFile, ActionListAdapter.ViewHolder>(context, list, listener) {

        override fun onBindViewHolder(holder: ViewHolder, item: SoftButtonActionFile, position: Int) {
            holder.apply {
                textView.text = actionNames[item.press]
                imageView.setImageDrawable(actionIcons[item.press])
            }
        }

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                inflater.inflate(R.layout.action_list_item, parent, false), this)
        }

        private class ViewHolder(
            view: View, adapter: ActionListAdapter
        ) : ArrayViewHolder<SoftButtonActionFile>(view, adapter) {
            val textView: TextView = view.findViewById(R.id.textView)
            val imageView: ImageView = view.findViewById(R.id.imageView)
        }
    }

    override val isNeedDivider: Boolean
        get() = false

    companion object {
        private const val ACTION_TYPE = "type"
        private const val ACTION_ID = "id"
        private const val RESULT_REQUEST_ADD = 1

        operator fun invoke(actionType: Int, actionId: Int): androidx.fragment.app.Fragment {
            return SoftButtonActionArrayFragment().apply {
                arguments = Bundle().apply {
                    putInt(ACTION_TYPE, actionType)
                    putInt(ACTION_ID, actionId)
                }
            }
        }
    }
}
