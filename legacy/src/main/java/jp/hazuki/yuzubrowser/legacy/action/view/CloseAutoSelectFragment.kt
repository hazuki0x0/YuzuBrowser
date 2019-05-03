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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.ui.extensions.addCallback
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.action_activity.*

class CloseAutoSelectFragment : Fragment(), OnRecyclerListener {

    private lateinit var defaultAction: Action
    private lateinit var intentAction: Action
    private lateinit var windowAction: Action

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.action_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        val arguments = arguments ?: throw IllegalArgumentException()

        defaultAction = arguments.getParcelable(DEFAULT) ?: Action()
        intentAction = arguments.getParcelable(INTENT) ?: Action()
        windowAction = arguments.getParcelable(WINDOW) ?: Action()


        resetButton.visibility = View.INVISIBLE
        cancelButton.setOnClickListener {
            requireActivity().run {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        okButton.setOnClickListener {
            requireActivity().run {
                val intent = Intent()
                intent.putExtra(DEFAULT, defaultAction as Parcelable?)
                intent.putExtra(INTENT, intentAction as Parcelable?)
                intent.putExtra(WINDOW, windowAction as Parcelable?)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        activity.onBackPressedDispatcher.addCallback(this) {
            requireActivity().run {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            true
        }

        val items = mutableListOf(getString(R.string.pref_close_default),
                getString(R.string.pref_close_intent),
                getString(R.string.pref_close_window))

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = CloseAutoSelectAdapter(activity, items, this)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        val builder = ActionActivity.Builder(requireActivity())
        when (position) {
            0 -> startActivityForResult(builder.setDefaultAction(defaultAction)
                    .setTitle(R.string.pref_close_default)
                    .create(),
                    REQUEST_DEFAULT)
            1 -> startActivityForResult(builder.setDefaultAction(intentAction)
                    .setTitle(R.string.pref_close_intent)
                    .create(),
                    REQUEST_INTENT)
            2 -> startActivityForResult(builder.setDefaultAction(windowAction)
                    .setTitle(R.string.pref_close_window)
                    .create(),
                    REQUEST_WINDOW)
            else -> throw IllegalArgumentException("Unknown position:$position")
        }
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_DEFAULT -> defaultAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION)
                REQUEST_INTENT -> intentAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION)
                REQUEST_WINDOW -> windowAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION)
            }
        }
    }

    companion object {
        private const val REQUEST_DEFAULT = 0
        private const val REQUEST_INTENT = 1
        private const val REQUEST_WINDOW = 2

        const val DEFAULT = "0"
        const val INTENT = "1"
        const val WINDOW = "2"

        operator fun invoke(defAction: Action?, intentAction: Action?, windowAction: Action?): Fragment {
            return CloseAutoSelectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DEFAULT, defAction)
                    putParcelable(INTENT, intentAction)
                    putParcelable(WINDOW, windowAction)
                }
            }
        }
    }
}
