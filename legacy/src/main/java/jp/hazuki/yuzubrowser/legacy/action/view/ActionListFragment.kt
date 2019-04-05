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

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.RecyclerFabFragment
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import jp.hazuki.yuzubrowser.ui.widget.recycler.SimpleViewHolder

class ActionListFragment : RecyclerFabFragment(), OnRecyclerListener, DeleteDialogCompat.OnDelete {

    private lateinit var mList: ActionList
    private lateinit var adapter: ActionListAdapter
    private lateinit var mActionNameArray: ActionNameArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val arguments = arguments ?: return

        mActionNameArray = arguments.getParcelable(ActionNameArray.INTENT_EXTRA)!!

        val actions = arguments.getParcelable<ActionList>(EXTRA_ACTION_LIST)
        setActionList(actions)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val bundle = Bundle()
        bundle.putInt(EXTRA_POSITION, position)
        val intent = ActionActivity.Builder(activity ?: return)
                .setDefaultAction(mList[position])
                .setActionNameArray(mActionNameArray)
                .setTitle(R.string.edit_action)
                .setReturnData(bundle)
                .create()
        startActivityForResult(intent, RESULT_REQUEST_EDIT)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        if (childFragmentManager.findFragmentByTag(TAG_DELETE_FRAGMENT) == null) {
            DeleteDialogCompat.newInstance(activity, R.string.confirm, R.string.confirm_delete_action, position)
                    .show(childFragmentManager, TAG_DELETE_FRAGMENT)
        }
        return true
    }

    override fun onDelete(position: Int) {
        mList.removeAt(position)
        adapter.notifyDataSetChanged()
        onActionListChanged()
    }

    override fun onAddButtonClick() {
        val intent = ActionActivity.Builder(activity ?: return)
                .setTitle(R.string.edit_action)
                .setActionNameArray(mActionNameArray)
                .create()

        startActivityForResult(intent, RESULT_REQUEST_ADD)
    }

    override fun onAddButtonLongClick(): Boolean {
        startEasyAdd()
        return true
    }

    private fun setActionList(list: ActionList?) {
        val activity = activity ?: return

        mList = list ?: ActionList()

        adapter = ActionListAdapter(activity, mList, mActionNameArray, this)
        setRecyclerViewAdapter(adapter)
    }

    private fun onActionListChanged() {
        val activity = activity ?: return

        if (activity is ActionListActivity) {
            activity.onActionListChanged(mList)
        }
        val data = Intent()
        data.putExtra(EXTRA_ACTION_LIST, mList as Parcelable?)
        activity.setResult(RESULT_OK, data)
    }

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, fromIndex: Int, toIndex: Int): Boolean {
        adapter.move(fromIndex, toIndex)
        onActionListChanged()
        return true
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, index: Int) {
        val action = mList.removeAt(index)
        adapter.notifyDataSetChanged()
        onActionListChanged()
        Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    mList.add(index, action)
                    adapter.notifyDataSetChanged()
                    onActionListChanged()
                }
                .show()
    }

    private fun startEasyAdd() {
        val intent = ActionActivity.Builder(activity ?: return)
                .setTitle(R.string.action_easy_add)
                .setActionNameArray(mActionNameArray)
                .create()

        startActivityForResult(intent, RESULT_REQUEST_ADD_EASY)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                val next = !adapter.isSortMode
                adapter.isSortMode = next

                Toast.makeText(activity, if (next) R.string.start_sort else R.string.end_sort, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.actionToJson -> {
                val intent = Intent(activity, ActionStringActivity::class.java)
                intent.putExtra(ActionStringActivity.EXTRA_ACTION, mList as Parcelable?)
                startActivityForResult(intent, RESULT_REQUEST_JSON)
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            val action = data.getParcelableExtra<Action>(ActionActivity.EXTRA_ACTION)
            when (requestCode) {
                RESULT_REQUEST_ADD -> mList.add(action)
                RESULT_REQUEST_EDIT -> {
                    if (action.isEmpty()) {
                        Snackbar.make(rootView, R.string.action_cant_empty, Snackbar.LENGTH_SHORT).show()
                        return
                    }
                    val position = data.getBundleExtra(ActionActivity.EXTRA_RETURN).getInt(EXTRA_POSITION)
                    mList[position] = action
                }
                RESULT_REQUEST_ADD_EASY -> action.forEach {
                    mList.add(Action(it))
                }
                RESULT_REQUEST_JSON -> {
                    val actionList = data.getParcelableExtra<ActionList>(ActionStringActivity.EXTRA_ACTION)
                    mList.clear()
                    mList.addAll(actionList)
                }
            }

            onActionListChanged()
            adapter.notifyDataSetChanged()
        }
    }

    override val isLongPressDragEnabled
        get() = adapter.isSortMode

    private class ActionListAdapter internal constructor(context: Context, actionList: ActionList, private val nameList: ActionNameArray, recyclerListener: OnRecyclerListener) : ArrayRecyclerAdapter<Action, SimpleViewHolder<Action>>(context, actionList, recyclerListener) {

        override fun onBindViewHolder(holder: SimpleViewHolder<Action>, item: Action, position: Int) {
            holder.textView.text = item.toString(nameList)
        }

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): SimpleViewHolder<Action> {
            return SimpleViewHolder(
                    inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false),
                    android.R.id.text1,
                    this)
        }
    }

    companion object {
        const val EXTRA_ACTION_LIST = "ActionListActivity.extra.actionList"
        private const val EXTRA_POSITION = "pos"
        private const val RESULT_REQUEST_ADD = 1
        private const val RESULT_REQUEST_EDIT = 2
        private const val RESULT_REQUEST_ADD_EASY = 3
        private const val RESULT_REQUEST_JSON = 4

        private const val TAG_DELETE_FRAGMENT = "delete"

        fun newInstance(actionList: ActionList, nameArray: ActionNameArray): androidx.fragment.app.Fragment {
            return ActionListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_ACTION_LIST, actionList)
                    putParcelable(ActionNameArray.INTENT_EXTRA, nameArray)
                }
            }
        }
    }
}
