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
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.ui.app.OnActivityResultListener
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

open class ActionListActivity : ThemeActivity() {

    protected open val actionList: ActionList?
        get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        var mList = actionList

        var mActionNameArray: ActionNameArray? = null

        val intent = intent
        if (intent != null) {
            val title = intent.getStringExtra(Intent.EXTRA_TITLE)
            setTitle(title)

            mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA)
            if (mList == null)
                mList = intent.getParcelableExtra(EXTRA_ACTION_LIST)
        }

        if (mList == null)
            mList = ActionList()

        if (mActionNameArray == null)
            mActionNameArray = ActionNameArray(this)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, ActionListFragment.newInstance(mList, mActionNameArray))
                .commit()
    }

    open fun onActionListChanged(actionList: ActionList) {}

    class Builder(private val mContext: Context) {
        private val intent = Intent(mContext.applicationContext, ActionListActivity::class.java)
        private var listener: OnActivityResultListener? = null

        fun setTitle(title: Int): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, mContext.getString(title))
            return this
        }

        fun setTitle(title: String): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, title)
            return this
        }

        fun setActionNameArray(array: ActionNameArray?): Builder {
            intent.putExtra(ActionNameArray.INTENT_EXTRA, array as Parcelable?)
            return this
        }

        fun setDefaultActionList(actionlist: ActionList): Builder {
            intent.putExtra(EXTRA_ACTION_LIST, actionlist as Parcelable)
            return this
        }

        fun setOnActionListActivityResultListener(l: (actionList: ActionList) -> Unit): Builder {
            listener = listener@ { _, resultCode, intent ->
                if (resultCode != Activity.RESULT_OK || intent == null) {
                    Logger.w(RESULT_TAG, "resultCode != Activity.RESULT_OK || intent == null")
                    return@listener
                }

                val actionList = intent.getParcelableExtra<ActionList>(EXTRA_ACTION_LIST)
                if (actionList == null) {
                    Logger.w(RESULT_TAG, "Action is null")
                    return@listener
                }
                l.invoke(actionList)
            }
            return this
        }

        fun makeStartActivityInfo(): StartActivityInfo {
            return StartActivityInfo(intent, listener)
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
        private const val RESULT_TAG = "OnActionActivityResultListener"
        const val EXTRA_ACTION_LIST = "ActionListActivity.extra.actionList"
    }
}
