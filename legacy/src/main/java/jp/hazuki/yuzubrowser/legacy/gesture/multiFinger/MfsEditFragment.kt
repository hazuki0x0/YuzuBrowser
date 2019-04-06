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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureItem
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector.MultiFingerGestureDetector
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.fragment_multi_finger_edit.*

class MfsEditFragment : androidx.fragment.app.Fragment() {

    private var listener: OnMfsEditFragmentListener? = null
    private lateinit var item: MultiFingerGestureItem
    private lateinit var nameArray: ActionNameArray
    private lateinit var adapter: MfsFingerAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_multi_finger_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity ?: return
        val fragmentManager = fragmentManager ?: return
        val arguments = arguments ?: throw IllegalArgumentException()

        item = arguments.getParcelable(ARG_ITEM) ?: MultiFingerGestureItem()
        nameArray = ActionNameArray(activity)

        val text = item.action.toString(nameArray)
        actionButton.text = text ?: getText(R.string.action_empty)
        actionButton.setOnClickListener {
            val intent = ActionActivity.Builder(activity)
                    .setDefaultAction(item.action)
                    .setTitle(R.string.pref_multi_finger_gesture_settings)
                    .create()
            startActivityForResult(intent, REQUEST_ACTION)
        }

        seekTextView.text = Integer.toString(item.fingers)
        fingerSeekBar.run {
            progress = item.fingers - 1
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    item.fingers = progress + 1
                    seekTextView.text = Integer.toString(progress + 1)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }

        upButton.setOnClickListener { addFingerAction(MultiFingerGestureDetector.SWIPE_UP) }

        downButton.setOnClickListener { addFingerAction(MultiFingerGestureDetector.SWIPE_DOWN) }

        leftButton.setOnClickListener { addFingerAction(MultiFingerGestureDetector.SWIPE_LEFT) }

        rightButton.setOnClickListener { addFingerAction(MultiFingerGestureDetector.SWIPE_RIGHT) }

        deleteButton.setOnClickListener {
            item.removeLastTrace()
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }

        cancelButton.setOnClickListener { fragmentManager.popBackStack() }

        okButton.setOnClickListener {
            listener?.onEdited(arguments.getInt(ARG_INDEX, -1), item)
            fragmentManager.popBackStack()
        }

        recyclerView.let {
            it.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = MfsFingerAdapter(activity, item.traces, null)
            it.adapter = adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ACTION -> if (resultCode == Activity.RESULT_OK && data != null) {
                item.action = ActionActivity.getActionFromIntent(data)
                val text = item.action.toString(nameArray)
                actionButton.text = text ?: getText(R.string.action_empty)
            }
        }
    }

    private fun addFingerAction(action: Int) {
        if (item.checkTrace(action)) {
            item.addTrace(action)
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is OnMfsEditFragmentListener)
            listener = activity as OnMfsEditFragmentListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    internal interface OnMfsEditFragmentListener {
        fun onEdited(index: Int, item: MultiFingerGestureItem)
    }

    private class MfsFingerAdapter(context: Context, list: MutableList<Int>, listener: OnRecyclerListener?) : ArrayRecyclerAdapter<Int, MfsFingerAdapter.ViewHolder>(context, list, listener) {
        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.fragment_multi_finger_edit_item, parent, false), this)
        }

        internal class ViewHolder(itemView: View, adapter: MfsFingerAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<Int>(itemView, adapter) {

            val title: TextView = itemView.findViewById(R.id.numTextView)
            val icon: ImageView = itemView.findViewById(R.id.imageView)

            override fun setUp(item: Int) {
                super.setUp(item)
                title.text = (adapterPosition + 1).toString() + "."
                icon.setImageResource(getImage(item))
            }

            @DrawableRes
            private fun getImage(type: Int): Int {
                return when (type) {
                    MultiFingerGestureDetector.SWIPE_UP -> R.drawable.ic_arrow_upward_white_24dp
                    MultiFingerGestureDetector.SWIPE_DOWN -> R.drawable.ic_arrow_downward_white_24dp
                    MultiFingerGestureDetector.SWIPE_LEFT -> R.drawable.ic_arrow_back_white_24dp
                    MultiFingerGestureDetector.SWIPE_RIGHT -> R.drawable.ic_arrow_forward_white_24dp
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    companion object {
        private const val ARG_INDEX = "index"
        private const val ARG_ITEM = "item"
        private const val REQUEST_ACTION = 1

        fun newInstance(index: Int, item: MultiFingerGestureItem): MfsEditFragment {
            val fragment = MfsEditFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_INDEX, index)
            bundle.putParcelable(ARG_ITEM, item)
            fragment.arguments = bundle
            return fragment
        }
    }
}
