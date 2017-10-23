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

package jp.hazuki.yuzubrowser.action.item

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.action.ActionNameArray
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener
import jp.hazuki.yuzubrowser.utils.view.recycler.RecyclerMenu
import kotlinx.android.synthetic.main.action_custom.*

class CustomSingleActionFragment : Fragment(), OnRecyclerListener, RecyclerMenu.OnRecyclerMenuListener {

    private lateinit var adapter: ActionAdapter
    private lateinit var actionNameArray: ActionNameArray

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.action_custom, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(DividerItemDecoration(activity))

            val helper = ItemTouchHelper(Touch())
            helper.attachToRecyclerView(this)
            addItemDecoration(helper)
        }

        val actions = arguments.getParcelable<Action>(ARG_ACTION)
        val name = arguments.getString(ARG_NAME)
        actionNameArray = arguments.getParcelable(ActionNameArray.INTENT_EXTRA) ?: ActionNameArray(activity)

        adapter = ActionAdapter(activity, actions, actionNameArray, this, this).apply {
            isSortMode = true
        }
        recyclerView.adapter = adapter
        editText.setText(name)

        okButton.setOnClickListener {
            var newName: String? = editText.text.toString()

            if (TextUtils.isEmpty(newName) && adapter.itemCount > 0) {
                newName = adapter.get(0).toString(actionNameArray)
            }

            val result = Intent()
            result.putExtra(CustomSingleActionActivity.EXTRA_NAME, newName)
            result.putExtra(CustomSingleActionActivity.EXTRA_ACTION, Action(adapter.items) as Parcelable)
            activity.setResult(Activity.RESULT_OK, result)
            activity.finish()
        }

        cancelButton.setOnClickListener { activity.finish() }

        addButton.setOnClickListener {
            val intent = ActionActivity.Builder(activity)
                    .setTitle(R.string.add)
                    .setActionNameArray(actionNameArray)
                    .create()

            startActivityForResult(intent, RESULT_REQUEST_PREFERENCE)
        }
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val bundle = Bundle()
        bundle.putInt(ARG_POSITION, position)
        val intent = ActionActivity.Builder(activity)
                .setTitle(R.string.edit_action)
                .setDefaultAction(Action(adapter.get(position)))
                .setActionNameArray(actionNameArray)
                .setReturnData(bundle)
                .create()

        startActivityForResult(intent, RESULT_REQUEST_EDIT)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        return false
    }

    override fun onDeleteClicked(position: Int) {
        adapter.remove(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_REQUEST_PREFERENCE -> {
                val action = ActionActivity.getActionFromIntent(resultCode, data) ?: return
                adapter.addAll(action)
                adapter.notifyItemInserted(adapter.itemCount - 1)
            }
            RESULT_REQUEST_EDIT -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return
                }
                val action = ActionActivity.getActionFromIntent(resultCode, data)
                val returnData = ActionActivity.getReturnData(data)
                if (action == null || returnData == null) {
                    return
                }
                val position = returnData.getInt(ARG_POSITION)
                if (action.size == 1) {
                    adapter.set(position, action[0])
                    adapter.notifyItemChanged(position)
                } else {
                    adapter.remove(position)
                    for (i in action.indices.reversed()) {
                        adapter.add(position, action[i])
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private inner class Touch : ItemTouchHelper.Callback() {

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) or ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            adapter.move(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val action = adapter.remove(position)

            Snackbar.make(rootLayout, R.string.deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo) {
                        adapter.add(position, action)
                        adapter.notifyItemInserted(position)
                    }
                    .show()
        }
    }

    private class ActionAdapter internal constructor(private val context: Context, list: Action, private val nameArray: ActionNameArray, private val menuListener: RecyclerMenu.OnRecyclerMenuListener, listener: OnRecyclerListener) : ArrayRecyclerAdapter<SingleAction, ActionAdapter.AVH>(context, list, listener), RecyclerMenu.OnRecyclerMoveListener {

        override fun onBindViewHolder(holder: AVH, item: SingleAction, position: Int) {
            holder.title.text = item.toString(nameArray)
            holder.menu.setOnClickListener { v -> RecyclerMenu(context, v, holder.adapterPosition, menuListener, this@ActionAdapter).show() }
        }

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): AVH {
            return AVH(inflater.inflate(R.layout.action_custom_item, parent, false), this)
        }

        override fun onMoveUp(position: Int) {
            if (position > 0) {
                move(position, position - 1)
            }
        }

        override fun onMoveDown(position: Int) {
            if (position < itemCount - 1) {
                move(position, position + 1)
            }
        }

        internal class AVH(itemView: View, adapter: ActionAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<SingleAction>(itemView, adapter) {

            internal val title: TextView = itemView.findViewById(R.id.titleTextView)
            internal val menu: ImageButton = itemView.findViewById(R.id.menu)
        }
    }

    companion object {
        private const val RESULT_REQUEST_PREFERENCE = 1
        private const val RESULT_REQUEST_EDIT = 2

        private const val ARG_ACTION = "action"
        private const val ARG_NAME = "name"

        private const val ARG_POSITION = "position"

        fun newInstance(actionList: Action, name: String, actionNameArray: ActionNameArray): CustomSingleActionFragment {
            return CustomSingleActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ACTION, actionList)
                    putString(ARG_NAME, name)
                    putParcelable(ActionNameArray.INTENT_EXTRA, actionNameArray)
                }
            }
        }
    }
}
