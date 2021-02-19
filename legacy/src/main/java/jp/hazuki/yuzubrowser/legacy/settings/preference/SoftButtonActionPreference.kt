package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.view.SoftButtonActionActivity

class SoftButtonActionPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
    private val actionId: Int
    private val actionType: Int
    private val title: String

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ActionListPreference)
        actionType = a.getInt(R.styleable.ActionListPreference_actionGroup, 0)
        actionId = a.getInt(R.styleable.ActionListPreference_actionId, 0)
        title = a.getString(R.styleable.ActionListPreference_android_title)!!
        require(actionType != 0) { "mActionType is zero" }
        require(actionId != 0) { "actionId is zero" }
        a.recycle()
    }

    override fun onClick() {
        super.onClick()

        val intent = SoftButtonActionActivity.createIntent(context, title, actionType, actionId, 0)
        context.startActivity(intent)
    }
}
