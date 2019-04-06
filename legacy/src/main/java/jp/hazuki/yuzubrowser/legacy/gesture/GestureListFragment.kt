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

package jp.hazuki.yuzubrowser.legacy.gesture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.core.utility.extensions.dimension
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.view.recycler.RecyclerFabFragment
import jp.hazuki.yuzubrowser.ui.dialog.DeleteDialogCompat
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class GestureListFragment : RecyclerFabFragment(), OnRecyclerListener, DeleteDialogCompat.OnDelete {

    private lateinit var mManager: GestureManager
    private var mGestureId: Int = 0
    private lateinit var adapter: GestureListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity ?: return
        val arguments = arguments ?: throw IllegalArgumentException()

        val mActionNameArray = arguments.getParcelable<ActionNameArray>(ActionNameArray.INTENT_EXTRA) ?: throw IllegalArgumentException()
        mGestureId = arguments.getInt(GestureManager.INTENT_EXTRA_GESTURE_ID)
        mManager = GestureManager.getInstance(activity.applicationContext, mGestureId)
        mManager.load()
        adapter = GestureListAdapter(activity, mManager.list, mActionNameArray, this)
        setRecyclerViewAdapter(adapter)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val item = adapter[position]
        val bundle = Bundle().apply { putLong(ITEM_ID, item.id) }
        val intent = ActionActivity.Builder(activity ?: return)
                .setDefaultAction(item.action)
                .setTitle(R.string.action_settings)
                .setReturnData(bundle)
                .create()

        startActivityForResult(intent, RESULT_REQUEST_EDIT)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        DeleteDialogCompat.newInstance(activity, R.string.confirm, R.string.confirm_delete_gesture, position)
                .show(childFragmentManager, "delete")
        return true
    }

    override fun onDelete(position: Int) {
        val item = adapter.remove(position)
        mManager.remove(item)
    }

    override fun onAddButtonClick() {
        val activity = activity ?: return

        val intent = Intent(activity, AddGestureActivity::class.java)
        intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, mGestureId)
        intent.putExtra(Intent.EXTRA_TITLE, activity.title)
        startActivityForResult(intent, RESULT_REQUEST_ADD)
    }

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, fromIndex: Int, toIndex: Int): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_REQUEST_ADD -> reset()
                RESULT_REQUEST_EDIT -> {
                    if (data == null) return

                    val bundle = data.getBundleExtra(ActionActivity.EXTRA_RETURN)
                    val action = data.getParcelableExtra<Action>(ActionActivity.EXTRA_ACTION)
                    mManager.updateAction(bundle.getLong(ITEM_ID), action)
                    reset()
                }
            }
        }
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, index: Int) {
        val item = adapter.remove(index)
        Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo) {
                    adapter.add(index, item)
                    adapter.notifyItemInserted(index)
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && event != Snackbar.Callback.DISMISS_EVENT_MANUAL) {
                            mManager.remove(item)
                        }
                    }
                })
                .show()
    }

    override val isLongPressDragEnabled = false

    private fun reset() {
        mManager.load()
        adapter.clear()
        adapter.addAll(mManager.list)
        adapter.notifyDataSetChanged()
    }

    private class GestureListAdapter internal constructor(context: Context, actionList: MutableList<GestureItem>, private val nameList: ActionNameArray, recyclerListener: OnRecyclerListener) : ArrayRecyclerAdapter<GestureItem, GestureListAdapter.ViewHolder>(context, actionList, recyclerListener) {
        private val size: Int = context.dimension(R.dimen.gesture_image_size)
        private val color: Int = context.getResColor(R.color.add_gesture_color)

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.image_text_list_item, parent, false), this)
        }

        internal inner class ViewHolder(itemView: View, adapter: GestureListAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<GestureItem>(itemView, adapter) {
            var title: TextView = itemView.findViewById(R.id.textView)
            var imageView: ImageView = itemView.findViewById(R.id.imageView)

            override fun setUp(item: GestureItem) {
                super.setUp(item)
                imageView.setImageBitmap(item.getBitmap(size, size, color))

                val action = item.action

                if (action == null || action.isEmpty())
                    title.setText(R.string.action_empty)
                else
                    title.text = action.toString(nameList)
            }
        }
    }

    companion object {
        private const val RESULT_REQUEST_ADD = 0
        private const val RESULT_REQUEST_EDIT = 1
        private const val ITEM_ID = "id"

        operator fun invoke(gestureId: Int, nameArray: ActionNameArray): androidx.fragment.app.Fragment {
            return GestureListFragment().apply {
                arguments = Bundle().apply {
                    putInt(GestureManager.INTENT_EXTRA_GESTURE_ID, gestureId)
                    putParcelable(ActionNameArray.INTENT_EXTRA, nameArray)
                }
            }
        }
    }
}
