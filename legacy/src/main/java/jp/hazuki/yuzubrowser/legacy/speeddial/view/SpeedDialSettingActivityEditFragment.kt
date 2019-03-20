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

package jp.hazuki.yuzubrowser.legacy.speeddial.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDial
import jp.hazuki.yuzubrowser.legacy.speeddial.WebIcon
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import kotlinx.android.synthetic.main.fragment_edit_speeddial.*
import java.io.File

class SpeedDialSettingActivityEditFragment : androidx.fragment.app.Fragment() {

    private lateinit var speedDial: SpeedDial
    private var mCallBack: SpeedDialEditCallBack? = null
    private var goBack: GoBackController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_speeddial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val arguments = arguments ?: throw IllegalArgumentException()

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
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                    val activity = activity ?: return
                    if ("file" == uri.scheme) {
                        val provider = (activity.applicationContext as BrowserApplication).providerManager.downloadFileProvider
                        uri = provider.getUriForFile(File(uri.path))
                    }
                    val intent = Intent("com.android.camera.action.CROP").apply {
                        this.data = uri
                        putExtra("outputX", 200)
                        putExtra("outputY", 200)
                        putExtra("aspectX", 1)
                        putExtra("aspectY", 1)
                        putExtra("scale", true)
                        putExtra("return-data", true)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    override fun onAttach(context: Context) {
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

        fun newInstance(speedDial: SpeedDial): androidx.fragment.app.Fragment {
            val fragment = SpeedDialSettingActivityEditFragment()
            val bundle = Bundle()
            bundle.putSerializable(DATA, speedDial)
            fragment.arguments = bundle
            return fragment
        }
    }
}
