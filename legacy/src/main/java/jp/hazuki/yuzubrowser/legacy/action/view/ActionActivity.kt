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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.*
import jp.hazuki.yuzubrowser.ui.app.OnActivityResultListener
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import kotlinx.android.synthetic.main.action_activity.*

class ActionActivity : ThemeActivity(), OnRecyclerListener {

    private var mActionManager: ActionManager? = null
    private var mOnActivityResultListener: OnActivityResultListener? = null
    lateinit var actionNameArray: ActionNameArray
        private set
    private var mActionId: Int = 0
    private lateinit var mAction: Action
    private lateinit var adapter: ActionNameArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_activity)

        val intent = intent ?: throw NullPointerException("intent is null")

        if (ACTION_ALL_ACTION == intent.action) {
            val fullscreen = intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, AppPrefs.fullscreen.get())
            val orientation = intent.getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, AppPrefs.oritentation.get())

            if (fullscreen)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestedOrientation = orientation
        }

        actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA) ?: ActionNameArray(applicationContext)

        adapter = ActionNameArrayAdapter(this, actionNameArray, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0)
        mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0)
        if (mActionType != 0) {
            mActionManager = ActionManager.getActionManager(applicationContext, mActionType)

            mAction = when (mActionManager) {
                is SingleActionManager -> Action((mActionManager as SingleActionManager).getAction(mActionId))//copy
                is ListActionManager -> Action()
                else -> throw IllegalArgumentException()
            }
        } else {
            mActionManager = null
            mAction = intent.getParcelableExtra(EXTRA_ACTION) ?: Action()
        }

        title = intent.getStringExtra(Intent.EXTRA_TITLE)

        var initialPosition = -1
        for (action in mAction) {
            val id = action.id
            val size = actionNameArray.actionValues.size
            for (i in 0 until size) {
                if (actionNameArray.actionValues[i] == id) {
                    adapter.setChecked(i, true)

                    if (initialPosition == -1)
                        initialPosition = i
                }
            }
        }
        adapter.notifyDataSetChanged()

        if (initialPosition != -1)
            recyclerView.scrollToPosition(initialPosition)

        okButton.setOnClickListener {
            when (mActionManager) {
                null -> {
                    val intent1 = Intent()
                    intent1.putExtra(EXTRA_ACTION, mAction as Parcelable?)
                    intent1.putExtra(EXTRA_RETURN, getIntent().getBundleExtra(EXTRA_RETURN))
                    setResult(Activity.RESULT_OK, intent1)
                }
                is SingleActionManager -> {
                    val list = (mActionManager as SingleActionManager).getAction(mActionId)
                    list.clear()
                    list.addAll(mAction)
                    mActionManager!!.save(applicationContext)
                    setResult(Activity.RESULT_OK)
                }
                is ListActionManager -> {
                    (mActionManager as ListActionManager).addAction(mActionId, mAction)
                    mActionManager!!.save(applicationContext)
                    setResult(Activity.RESULT_OK)
                }
            }
            finish()
        }

        okButton.setOnLongClickListener {
            startJsonStringActivity()
            false
        }

        resetButton.setOnClickListener {
            mAction.clear()
            adapter.clearChoices()
        }

        cancelButton.setOnClickListener { finish() }

        adapter.setListener(this::showSubPreference)
    }

    override fun onRecyclerItemClicked(v: View, position: Int) {
        itemClicked(position, false)
    }

    override fun onRecyclerItemLongClicked(v: View, position: Int): Boolean {
        if (!adapter.isChecked(position))
            itemClicked(position, true)

        showSubPreference(position)
        return true
    }

    private fun itemClicked(position: Int, forceShowPref: Boolean) {
        val value = adapter.getItemValue(position)

        if (adapter.toggleCheck(position)) {
            val action = SingleAction.makeInstance(value)
            mAction.add(action)
            showPreference(if (forceShowPref) {
                action.showSubPreference(this)
            } else {
                action.showMainPreference(this)
            })
        } else {
            for (i in mAction.indices) {
                if (mAction[i].id == value) {
                    mAction.removeAt(i)
                    break
                }
            }
        }
    }

    private fun showSubPreference(position: Int) {
        val value = adapter.getItemValue(position)
        val size = mAction.size
        for (i in 0 until size) {
            if (mAction[i].id == value) {
                showPreference(mAction[i].showSubPreference(this@ActionActivity))
                break
            }
        }
    }

    private fun showPreference(screen: StartActivityInfo?): Boolean {
        if (screen == null)
            return false
        mOnActivityResultListener = screen.onActivityResultListener
        startActivityForResult(screen.intent, RESULT_REQUEST_PREFERENCE)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_REQUEST_PREFERENCE -> if (mOnActivityResultListener != null) {
                mOnActivityResultListener!!.invoke(this, resultCode, data)
                mOnActivityResultListener = null
            }
            RESULT_REQUEST_JSON -> if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getParcelableExtra<Action>(ActionStringActivity.EXTRA_ACTION)
                mAction.clear()
                mAction.addAll(result)

                adapter.clearChoices()
                var initialPosition = -1
                val actionNameArray = adapter.nameArray
                for (action in mAction) {
                    val id = action.id
                    val size = actionNameArray.actionValues.size
                    for (i in 0 until size) {
                        if (actionNameArray.actionValues[i] == id) {
                            adapter.setChecked(i, true)

                            if (initialPosition == -1)
                                initialPosition = i
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                if (initialPosition != -1)
                    recyclerView.scrollToPosition(initialPosition)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.action_to_json).setOnMenuItemClickListener {
            startJsonStringActivity()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun startJsonStringActivity() {
        val intent = Intent(applicationContext, ActionStringActivity::class.java)
        intent.putExtra(ActionStringActivity.EXTRA_ACTION, mAction as Parcelable?)
        startActivityForResult(intent, RESULT_REQUEST_JSON)
    }

    class Builder(private val mContext: Context) {
        private val intent: Intent = Intent(mContext.applicationContext, ActionActivity::class.java)
        private var listener: OnActivityResultListener? = null

        fun setTitle(title: Int): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, mContext.getString(title))
            return this
        }

        fun setTitle(title: CharSequence): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, title)
            return this
        }

        fun setActionNameArray(array: ActionNameArray?): Builder {
            intent.putExtra(ActionNameArray.INTENT_EXTRA, array as Parcelable?)
            return this
        }

        fun setActionManager(actionType: Int, actionId: Int): Builder {
            intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, actionType)
            intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, actionId)
            return this
        }

        fun setDefaultAction(action: Action?): Builder {
            intent.putExtra(EXTRA_ACTION, action as Parcelable?)
            return this
        }

        fun setReturnData(bundle: Bundle): Builder {
            intent.putExtra(EXTRA_RETURN, bundle)
            return this
        }

        fun setOnActionActivityResultListener(l: (action: Action) -> Unit): Builder {
            listener = listener@ { _, resultCode, intent ->
                val action = getActionFromIntent(resultCode, intent)
                if (action == null) {
                    Logger.w("ActionActivityResult", "Action is null")
                    return@listener
                }
                l.invoke(action)
            }
            return this
        }

        fun makeStartActivityInfo(): StartActivityInfo {
            return StartActivityInfo(intent, listener)
        }

        fun create(): Intent {
            return intent
        }

        fun show() {
            mContext.startActivity(intent)
        }

        fun show(requestCode: Int): OnActivityResultListener? {
            if (mContext is Activity)
                mContext.startActivityForResult(intent, requestCode)
            else
                throw IllegalArgumentException("Context is not instanceof Activity")

            return listener
        }
    }

    companion object {
        private const val TAG = "ActionActivity"
        const val ACTION_ALL_ACTION = "ActionActivity.action.allaction"
        const val EXTRA_ACTION = "ActionActivity.extra.action"
        const val EXTRA_RETURN = "ActionActivity.extra.return"
        const val RESULT_REQUEST_PREFERENCE = 1
        private const val RESULT_REQUEST_JSON = 2

        fun getActionFromIntent(resultCode: Int, intent: Intent?): Action? {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                Logger.w(TAG, "resultCode != Activity.RESULT_OK || intent == null")
                return null
            }
            return intent.getParcelableExtra(EXTRA_ACTION)
        }

        @JvmStatic
        fun getActionFromIntent(intent: Intent): Action {
            return intent.getParcelableExtra(EXTRA_ACTION)
        }

        @JvmStatic
        fun getReturnData(intent: Intent): Bundle? {
            return intent.getBundleExtra(EXTRA_RETURN)
        }
    }
}
