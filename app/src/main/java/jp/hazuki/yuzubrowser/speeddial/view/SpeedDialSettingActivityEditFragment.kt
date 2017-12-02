package jp.hazuki.yuzubrowser.speeddial.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download.DownloadFileProvider
import jp.hazuki.yuzubrowser.speeddial.SpeedDial
import jp.hazuki.yuzubrowser.speeddial.WebIcon
import jp.hazuki.yuzubrowser.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_edit_speeddial.*
import java.io.File

class SpeedDialSettingActivityEditFragment : Fragment() {

    private lateinit var speedDial: SpeedDial
    private var mCallBack: SpeedDialEditCallBack? = null
    private var goBack: GoBackController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_speeddial, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        speedDial = arguments.getSerializable(DATA) as? SpeedDial ?: SpeedDial()


        superFrameLayout.setOnImeShownListener { visible -> bottomBar.visibility = if (visible) View.GONE else View.VISIBLE }

        name.setText(speedDial.title)
        url.setText(speedDial.url)

        val iconBitmap = speedDial.icon ?: WebIcon.createIcon(ImageUtils.getBitmapFromVectorDrawable(activity, R.drawable.ic_public_white_24dp))

        icon.setImageBitmap(iconBitmap.bitmap)

        use_favicon.isChecked = speedDial.isFavicon
        setIconEnable(!speedDial.isFavicon)

        use_favicon.setOnCheckedChangeListener { _, isChecked ->
            speedDial.isFavicon = isChecked
            setIconEnable(!isChecked)
        }

        icon.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }

        okButton.setOnClickListener {
            mCallBack?.run {
                speedDial.title = name.text.toString()
                speedDial.url = url.text.toString()
                onEdited(speedDial)
            }
        }

        cancelButton.setOnClickListener {
            goBack?.run {
                goBack()
            }
        }
    }

    private fun setIconEnable(enable: Boolean) {
        icon.isEnabled = enable
        icon.alpha = if (enable) 1.0f else 0.6f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_IMAGE -> if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    var uri = data.data ?: return
                    if ("file" == uri.scheme) {
                        uri = DownloadFileProvider.getUriForFIle(File(uri.path))
                    }
                    val intent = Intent("com.android.camera.action.CROP").apply {
                        this.data = uri
                        putExtra("outputX", 200)
                        putExtra("outputY", 200)
                        putExtra("aspectX", 1)
                        putExtra("aspectY", 1)
                        putExtra("scale", true)
                        putExtra("return-data", true)
                    }
                    startActivityForResult(intent, REQUEST_CROP_IMAGE)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, "Activity not found", Toast.LENGTH_SHORT).show()
                }

            }
            REQUEST_CROP_IMAGE -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val bitmap = data.extras.getParcelable<Bitmap>("data")
                speedDial.icon = WebIcon.createIconOrNull(bitmap)
                icon.setImageBitmap(bitmap)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mCallBack = activity as SpeedDialEditCallBack
            goBack = activity as GoBackController
        } catch (e: ClassCastException) {
            throw IllegalStateException(e)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallBack = null
        goBack = null
    }

    internal interface GoBackController {
        fun goBack(): Boolean
    }

    companion object {
        private const val DATA = "dat"
        private const val REQUEST_PICK_IMAGE = 100
        private const val REQUEST_CROP_IMAGE = 101

        fun newInstance(speedDial: SpeedDial): Fragment {
            val fragment = SpeedDialSettingActivityEditFragment()
            val bundle = Bundle()
            bundle.putSerializable(DATA, speedDial)
            fragment.arguments = bundle
            return fragment
        }
    }
}
