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

package jp.hazuki.yuzubrowser.legacy.pattern.url

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.FastMatcherFactory
import jp.hazuki.yuzubrowser.core.utility.extensions.hideIme
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.PatternActivity
import jp.hazuki.yuzubrowser.legacy.pattern.action.OpenOthersPatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.action.WebSettingPatternAction
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentListActivity
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.pattern_add_websetting.*
import java.util.*
import java.util.regex.PatternSyntaxException

class PatternUrlActivity : PatternActivity<PatternUrlChecker>() {
    private lateinit var urlEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val headerView = layoutInflater.inflate(R.layout.pattern_list_url, null)
        urlEditText = headerView.findViewById(R.id.urlEditText)
        addHeaderView(headerView)

        if (intent != null) {
            urlEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
        }

        setPatternManager(PatternUrlManager(applicationContext))
    }

    private fun makeHeaderView(checker: PatternUrlChecker?): View {
        val headerView = layoutInflater.inflate(R.layout.pattern_list_url, null)
        val editText: EditText = headerView.findViewById(R.id.urlEditText)
        val url: CharSequence
        if (checker == null) {
            url = urlEditText.text
            urlEditText.text = null
        } else {
            url = checker.patternUrl ?: ""
        }
        editText.setText(url)
        return headerView
    }

    override fun getWebSettingDialog(checker: PatternUrlChecker?): androidx.fragment.app.DialogFragment {
        return SettingWebDialog.getInstance(getPosition(checker), checker)
    }

    override fun getOpenOtherDialog(checker: PatternUrlChecker?): androidx.fragment.app.DialogFragment {
        return OpenOtherDialog.newInstance(getPosition(checker), checker, urlEditText.text.toString())
    }

    override fun settingBlockAction(checker: PatternUrlChecker?, header_view: View?) {
        super.settingBlockAction(checker, makeHeaderView(checker))
    }

    override fun makeActionChecker(pattern_action: PatternAction, header_view: View): PatternUrlChecker? {
        val patternUrl = (header_view.findViewById<View>(R.id.urlEditText) as EditText).text.toString()
        try {
            val checker = PatternUrlChecker(pattern_action, FastMatcherFactory(), patternUrl)
            urlEditText.setText("")
            return checker
        } catch (e: PatternSyntaxException) {
            ErrorReport.printAndWriteLog(e)
            Toast.makeText(applicationContext, R.string.pattern_syntax_error, Toast.LENGTH_SHORT).show()
        }

        return null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingWebDialog : androidx.fragment.app.DialogFragment() {

        private lateinit var header: View
        private lateinit var layout: SettingWebDialogView

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val arguments = arguments ?: throw IllegalArgumentException()

            val checker = arguments.getSerializable(CHECKER) as? PatternUrlChecker
            val view = View.inflate(activity, R.layout.pattern_add_websetting, null) as ViewGroup
            if (activity is PatternUrlActivity) {
                header = (activity as PatternUrlActivity).makeHeaderView(checker)
                view.findViewById<FrameLayout>(R.id.header_frame).addView(header)
            }

            layout = SettingWebDialogView(view).apply { init(checker) }

            val alertDialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.pattern_change_websettings)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()

            alertDialog.setOnShowListener {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val action = layout.getAction()

                    if (activity is PatternUrlActivity) {
                        val newChecker = (activity as PatternUrlActivity).makeActionChecker(action, header)
                        if (newChecker != null) {
                            val id = arguments.getInt(ID)
                            (activity as PatternUrlActivity).add(id, newChecker)
                            dismiss()
                        }
                    }
                }
            }


            return alertDialog
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            layout.handleResult(requestCode, resultCode, data)
        }

        companion object {
            private const val ID = "id"
            private const val CHECKER = "checker"

            private const val REQUEST_USER_AGENT = 1

            fun getInstance(id: Int, checker: PatternUrlChecker?): androidx.fragment.app.DialogFragment {
                val fragment = SettingWebDialog()
                val bundle = Bundle()
                bundle.putInt(ID, id)
                bundle.putSerializable(CHECKER, checker)
                fragment.arguments = bundle
                return fragment
            }
        }

        private inner class SettingWebDialogView(override val containerView: View) : LayoutContainer {

            fun init(checker: PatternUrlChecker?) {
                setData(checker)
                setListeners()
            }

            fun getAction(): WebSettingPatternAction {
                var ua: String? = null
                if (uaCheckBox.isChecked) {
                    ua = uaEditText.text.toString()
                }

                var js = WebSettingPatternAction.UNDEFINED
                if (jsCheckBox.isChecked) {
                    js = if (jsSwitch.isChecked) {
                        WebSettingPatternAction.ENABLE
                    } else {
                        WebSettingPatternAction.DISABLE
                    }
                }

                var navLock = WebSettingPatternAction.UNDEFINED
                if (navLockCheckBox.isChecked) {
                    navLock = if (navLockSwitch.isChecked) {
                        WebSettingPatternAction.ENABLE
                    } else {
                        WebSettingPatternAction.DISABLE
                    }
                }
                var image = WebSettingPatternAction.UNDEFINED
                if (loadImageCheckBox.isChecked) {
                    image = if (loadImageSwitch.isChecked) {
                        WebSettingPatternAction.ENABLE
                    } else {
                        WebSettingPatternAction.DISABLE
                    }
                }

                var cookie = WebSettingPatternAction.UNDEFINED
                if (cookieCheckBox.isChecked) {
                    cookie = if (cookieSwitch.isChecked) {
                        WebSettingPatternAction.ENABLE
                    } else {
                        WebSettingPatternAction.DISABLE
                    }
                }

                var thirdCookie = WebSettingPatternAction.UNDEFINED
                if (thirdCookieCheckBox.isChecked) {
                    thirdCookie = if (thirdCookieSwitch.isChecked) {
                        WebSettingPatternAction.ENABLE
                    } else {
                        WebSettingPatternAction.DISABLE
                    }
                }
                var renderingMode = WebSettingPatternAction.UNDEFINED_RENDERING
                if (renderingModeCheckBox.isChecked) {
                    renderingMode = renderingModeSpinner.selectedItemPosition
                }

                return WebSettingPatternAction(ua, js, navLock, image, cookie, thirdCookie, renderingMode)
            }

            private fun setData(checker: PatternUrlChecker?) {
                if (checker == null) {
                    uaEditText.isEnabled = false
                    uaButton.isEnabled = false
                    jsSwitch.isEnabled = false
                    navLockSwitch.isEnabled = false
                    loadImageSwitch.isEnabled = false
                    cookieSwitch.isEnabled = false
                    thirdCookieSwitch.isEnabled = false
                    renderingModeSpinner.isEnabled = false
                } else {
                    val action = checker.action as WebSettingPatternAction
                    val ua = action.userAgentString
                    uaCheckBox.isChecked = ua != null
                    if (ua != null) {
                        uaEditText.setText(ua)
                    } else {
                        uaEditText.isEnabled = false
                        uaButton.isEnabled = false
                    }

                    when (action.javaScriptSetting) {
                        WebSettingPatternAction.UNDEFINED -> {
                            jsCheckBox.isChecked = false
                            jsSwitch.isChecked = false
                            jsSwitch.isEnabled = false
                        }
                        WebSettingPatternAction.ENABLE -> {
                            jsCheckBox.isChecked = true
                            jsSwitch.isChecked = true
                        }
                        WebSettingPatternAction.DISABLE -> {
                            jsCheckBox.isChecked = true
                            jsSwitch.isChecked = false
                        }
                    }

                    when (action.navLock) {
                        WebSettingPatternAction.UNDEFINED -> {
                            navLockCheckBox.isChecked = false
                            navLockSwitch.isChecked = false
                            navLockSwitch.isEnabled = false
                        }
                        WebSettingPatternAction.ENABLE -> {
                            navLockCheckBox.isChecked = true
                            navLockSwitch.isChecked = true
                        }
                        WebSettingPatternAction.DISABLE -> {
                            navLockCheckBox.isChecked = true
                            navLockSwitch.isChecked = false
                        }
                    }

                    when (action.loadImage) {
                        WebSettingPatternAction.UNDEFINED -> {
                            loadImageCheckBox.isChecked = false
                            loadImageSwitch.isChecked = false
                            loadImageSwitch.isEnabled = false
                        }
                        WebSettingPatternAction.ENABLE -> {
                            loadImageCheckBox.isChecked = true
                            loadImageSwitch.isChecked = true
                        }
                        WebSettingPatternAction.DISABLE -> {
                            loadImageCheckBox.isChecked = true
                            loadImageSwitch.isChecked = false
                        }
                    }

                    when (action.cookie) {
                        WebSettingPatternAction.UNDEFINED -> {
                            cookieCheckBox.isChecked = false
                            cookieSwitch.isChecked = false
                            cookieSwitch.isEnabled = false
                        }
                        WebSettingPatternAction.ENABLE -> {
                            cookieCheckBox.isChecked = true
                            cookieSwitch.isChecked = true
                        }
                        WebSettingPatternAction.DISABLE -> {
                            cookieCheckBox.isChecked = true
                            cookieSwitch.isChecked = false
                        }
                    }

                    when (action.thirdCookie) {
                        WebSettingPatternAction.UNDEFINED -> {
                            thirdCookieCheckBox.isChecked = false
                            thirdCookieSwitch.isChecked = false
                            thirdCookieSwitch.isEnabled = false
                        }
                        WebSettingPatternAction.ENABLE -> {
                            thirdCookieCheckBox.isChecked = true
                            thirdCookieSwitch.isChecked = true
                        }
                        WebSettingPatternAction.DISABLE -> {
                            thirdCookieCheckBox.isChecked = true
                            thirdCookieSwitch.isChecked = false
                        }
                    }
                    if (action.renderingMode < 0) {
                        renderingModeCheckBox.isChecked = false
                        renderingModeSpinner.isEnabled = false
                        renderingModeSpinner.setSelection(0)
                    } else {
                        renderingModeCheckBox.isChecked = true
                        renderingModeSpinner.isEnabled = true
                        renderingModeSpinner.setSelection(action.renderingMode)
                    }
                }
            }

            fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (requestCode == REQUEST_USER_AGENT && resultCode == RESULT_OK) {
                    uaEditText.setText(data?.getStringExtra(Intent.EXTRA_TEXT))
                }
            }

            private fun setListeners() {
                uaCheckBox.setOnCheckedChangeListener { _, b ->
                    uaEditText.isEnabled = b
                    uaButton.isEnabled = b
                }

                uaButton.setOnClickListener {
                    val intent = Intent(activity, UserAgentListActivity::class.java)
                    intent.putExtra(Intent.EXTRA_TEXT, uaEditText.text.toString())
                    startActivityForResult(intent, REQUEST_USER_AGENT)
                }

                jsCheckBox.setOnCheckedChangeListener { _, b -> jsSwitch.isEnabled = b }

                navLockCheckBox.setOnCheckedChangeListener { _, b -> navLockSwitch.isEnabled = b }

                loadImageCheckBox.setOnCheckedChangeListener { _, b -> loadImageSwitch.isEnabled = b }

                cookieCheckBox.setOnCheckedChangeListener { _, b -> cookieSwitch.isEnabled = b }

                thirdCookieCheckBox.setOnCheckedChangeListener { _, b -> thirdCookieSwitch.isEnabled = b }

                renderingModeCheckBox.setOnCheckedChangeListener { _, b -> renderingModeSpinner.isEnabled = b }
            }
        }
    }

    class OpenOtherDialog : androidx.fragment.app.DialogFragment() {

        private var patternActivity: PatternUrlActivity? = null
        private var intent: Intent? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity ?: throw IllegalStateException()
            val arguments = arguments ?: throw IllegalArgumentException()

            var url = arguments.getString(URL)
            val checker = arguments.getSerializable(CHECKER) as? PatternUrlChecker

            val headerView = View.inflate(activity, R.layout.pattern_list_url, null)
            val urlEditText = headerView.findViewById<EditText>(R.id.urlEditText)
            if (checker != null) {
                url = checker.patternUrl
            }
            if (url == null) {
                url = ""
            }
            urlEditText.setText(url)

            val view = View.inflate(activity, R.layout.pattern_add_open, null) as ViewGroup
            view.addView(headerView, 0)
            val listView = view.findViewById<ListView>(R.id.listView)

            val pm = activity.packageManager
            val intentUrl = url.replace("*", "")
            intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse(if (WebUtils.maybeContainsUrlScheme(intentUrl)) intentUrl else "http://$intentUrl"))
            val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PackageManager.MATCH_ALL
            } else {
                PackageManager.MATCH_DEFAULT_ONLY
            }
            val openAppList = pm.queryIntentActivities(intent, flag)
            Collections.sort(openAppList, ResolveInfo.DisplayNameComparator(pm))

            val arrayAdapter = object : ArrayAdapter<ResolveInfo>(activity, 0, openAppList) {
                private val app_icon_size = resources.getDimension(android.R.dimen.app_icon_size).toInt()

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    var v = convertView
                    if (v == null) {
                        v = View.inflate(activity, R.layout.image_text_list_item, null)
                        val imageView = v!!.findViewById<ImageView>(R.id.imageView)

                        val params = imageView.layoutParams
                        params.height = app_icon_size
                        params.width = app_icon_size
                        imageView.layoutParams = params
                    }

                    val imageView = v.findViewById<ImageView>(R.id.imageView)
                    val textView = v.findViewById<TextView>(R.id.textView)

                    if (position == 0) {
                        imageView.setImageDrawable(null)
                        textView.text = getString(R.string.pattern_open_app_list)
                    } else {
                        val item = getItem(position)
                        imageView.setImageDrawable(item!!.loadIcon(pm))
                        textView.text = item.loadLabel(pm)
                    }

                    return v
                }

                override fun getItem(position: Int): ResolveInfo? {
                    return super.getItem(position - 1)
                }

                override fun getCount(): Int {
                    return super.getCount() + 1
                }
            }

            listView.adapter = arrayAdapter

            val builder = AlertDialog.Builder(activity)
                    .setTitle(R.string.pattern_open_others)
                    .setView(view)
                    .setNegativeButton(android.R.string.cancel, null)

            if (checker != null)
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    val newChecker = patternActivity!!.makeActionChecker(checker.action, headerView)
                    if (newChecker != null) {
                        patternActivity!!.add(arguments.getInt(ID), newChecker)
                    }
                }

            listView.setOnItemClickListener { _, _, position, _ ->
                val pattern = if (position == 0) {
                    OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_LIST)
                } else {
                    val item = openAppList[position - 1]
                    intent!!.setClassName(item.activityInfo.packageName, item.activityInfo.name)
                    OpenOthersPatternAction(intent!!)
                }
                val newChecker = patternActivity!!.makeActionChecker(pattern, headerView)
                if (newChecker != null) {
                    patternActivity!!.add(arguments.getInt(ID), newChecker)
                    dismiss()
                }
            }

            listView.setOnItemLongClickListener { _, _, position, _ ->
                if (position == 0) {
                    val newPattern = OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_CHOOSER)
                    val newChecker = patternActivity!!.makeActionChecker(newPattern, headerView)
                    if (newChecker != null) {
                        patternActivity!!.add(arguments.getInt(ID), newChecker)
                        dismiss()
                    }
                }
                true
            }

            urlEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    activity.hideIme(urlEditText)
                    val url1 = urlEditText.text.toString()
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (WebUtils.maybeContainsUrlScheme(url1)) url1 else "http://$url1"))
                    val newOpenAppList = pm.queryIntentActivities(intent, flag)
                    Collections.sort(newOpenAppList, ResolveInfo.DisplayNameComparator(pm))
                    arrayAdapter.clear()
                    arrayAdapter.addAll(newOpenAppList)
                    arrayAdapter.notifyDataSetChanged()
                    return@setOnEditorActionListener true
                }
                false
            }

            return builder.create()
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            patternActivity = activity as PatternUrlActivity
        }

        override fun onDetach() {
            super.onDetach()
            patternActivity = null
        }

        companion object {
            private const val ID = "id"
            private const val CHECKER = "checker"
            private const val URL = "url"

            fun newInstance(id: Int, checker: PatternUrlChecker?, url: String): OpenOtherDialog {
                val dialog = OpenOtherDialog()
                val bundle = Bundle()
                bundle.putInt(ID, id)
                bundle.putSerializable(CHECKER, checker)
                bundle.putString(URL, url)
                dialog.arguments = bundle
                return dialog
            }
        }
    }
}

