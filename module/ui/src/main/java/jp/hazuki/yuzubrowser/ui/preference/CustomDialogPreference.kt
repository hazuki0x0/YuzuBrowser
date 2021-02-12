package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.preference.DialogPreference
import androidx.preference.Preference

abstract class CustomDialogPreference @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : DialogPreference(context, attrs) {
    fun show(manager: FragmentManager?) {
        crateCustomDialog().show(manager!!, key)
    }

    protected abstract fun crateCustomDialog(): CustomDialogFragment
    open class CustomDialogFragment : DialogFragment(), TargetFragment {


        override fun <T : Preference?> findPreference(key: CharSequence): T? {
            val fragment = targetFragment as TargetFragment?
            return fragment!!.findPreference(key)
        }
    }
}
