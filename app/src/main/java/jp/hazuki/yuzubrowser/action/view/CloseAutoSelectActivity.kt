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

package jp.hazuki.yuzubrowser.action.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.ListFragment
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity

class CloseAutoSelectActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = InnerFragment()
        val bundle = Bundle()
        intent?.run {
            bundle.putParcelable(DEFAULT, getParcelableExtra(DEFAULT))
            bundle.putParcelable(INTENT, getParcelableExtra(INTENT))
            bundle.putParcelable(WINDOW, getParcelableExtra(WINDOW))
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is InnerFragment) {
            val intent = fragment.returnData
            setResult(RESULT_OK, intent)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class InnerFragment : ListFragment() {

        private lateinit var defaultAction: Action
        private lateinit var intentAction: Action
        private lateinit var windowAction: Action

        internal val returnData: Intent
            get() {
                val intent = Intent()
                intent.putExtra(DEFAULT, defaultAction as Parcelable?)
                intent.putExtra(INTENT, intentAction as Parcelable?)
                intent.putExtra(WINDOW, windowAction as Parcelable?)
                return intent
            }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            defaultAction = arguments.getParcelable(DEFAULT) ?: Action()
            intentAction = arguments.getParcelable(INTENT) ?: Action()
            windowAction = arguments.getParcelable(WINDOW) ?: Action()

            listAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1).apply {
                add(getString(R.string.pref_close_default))
                add(getString(R.string.pref_close_intent))
                add(getString(R.string.pref_close_window))
            }
        }

        override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
            val builder = ActionActivity.Builder(activity)
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
                else -> throw IllegalArgumentException("Unknown position:" + position)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == RESULT_OK) {
                when (requestCode) {
                    REQUEST_DEFAULT -> defaultAction = data!!.getParcelableExtra(ActionActivity.EXTRA_ACTION)
                    REQUEST_INTENT -> intentAction = data!!.getParcelableExtra(ActionActivity.EXTRA_ACTION)
                    REQUEST_WINDOW -> windowAction = data!!.getParcelableExtra(ActionActivity.EXTRA_ACTION)
                }
            }
        }

        companion object {
            private const val REQUEST_DEFAULT = 0
            private const val REQUEST_INTENT = 1
            private const val REQUEST_WINDOW = 2
        }
    }

    class Builder(private val con: Context) {
        private var listener: OnActivityResultListener? = null

        fun setListener(callback: (defaultAction: Action, intentAction: Action, windowAction: Action) -> Unit): Builder {
            listener = OnActivityResultListener { _, resultCode, intent ->
                if (resultCode == RESULT_OK) {
                    val defaultAction = intent.getParcelableExtra<Action>(DEFAULT)
                    val intentAction = intent.getParcelableExtra<Action>(INTENT)
                    val windowAction = intent.getParcelableExtra<Action>(WINDOW)
                    callback.invoke(defaultAction, intentAction, windowAction)
                }
            }
            return this
        }

        fun getActivityInfo(defaultAction: Action, intentAction: Action, windowAction: Action): StartActivityInfo {
            val intent = Intent(con, CloseAutoSelectActivity::class.java).apply {
                putExtra(DEFAULT, defaultAction as Parcelable)
                putExtra(INTENT, intentAction as Parcelable)
                putExtra(WINDOW, windowAction as Parcelable)
            }

            return StartActivityInfo(intent, listener)
        }
    }

    companion object {
        private const val DEFAULT = "0"
        private const val INTENT = "1"
        private const val WINDOW = "2"
    }
}
