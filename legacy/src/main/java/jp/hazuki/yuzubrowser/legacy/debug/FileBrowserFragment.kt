/*
 * Copyright 2020 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.debug

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.debug.file.FileAdapter
import jp.hazuki.yuzubrowser.legacy.debug.file.FileBrowserViewModel
import jp.hazuki.yuzubrowser.legacy.debug.file.FileItem
import jp.hazuki.yuzubrowser.ui.dialog.EditTextDialogFragment
import jp.hazuki.yuzubrowser.ui.extensions.registerForStartActivityForResult
import jp.hazuki.yuzubrowser.ui.widget.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class FileBrowserFragment : Fragment(), FileAdapter.OnFileClickListener,
    EditTextDialogFragment.OnTextChangeCallback {

    private val viewModel by viewModels<FileBrowserViewModel> {
        val file = arguments?.getSerializable(ARG_FILE) as File?
        FileBrowserViewModel.Factory(file ?: File(requireContext().applicationInfo.dataDir))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_debug_file_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = FileAdapter(viewLifecycleOwner, this)

        recyclerView.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter
        }

        viewModel.contents.observe(viewLifecycleOwner) {
            adapter.files = it
            adapter.notifyDataSetChanged()
        }

        viewModel.currentRoot.observe(viewLifecycleOwner) {
            backCallback.isEnabled = it != viewModel.root
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add("Paste").setOnMenuItemClickListener {
            val copy = viewModel.copy ?: return@setOnMenuItemClickListener true

            copy.copyTo(File(viewModel.currentRoot.value, copy.name), true)
            viewModel.reload()

            true
        }

        menu.add("Create directory").setOnMenuItemClickListener {
            EditTextDialogFragment(ID_CREATE_DIR, "Create directory")
                .show(childFragmentManager, "")
            true
        }

        menu.add("Create a new file").setOnMenuItemClickListener {
            EditTextDialogFragment(ID_CREATE_FILE, "Create an empty file")
                .show(childFragmentManager, "")
            true
        }

        menu.add("Import file").setOnMenuItemClickListener {
            importFile()
            true
        }
    }

    override fun onFileClick(item: FileItem) {
        if (item.path.isDirectory) {
            viewModel.setDir(item.path)
        } else {
            parentFragmentManager.commit {
                replace(R.id.container, FileEditFragment(item.path))
                addToBackStack("")
            }
        }
    }

    override fun onFileLongPress(item: FileItem): Boolean {
        LongPressFragment(item.path).show(childFragmentManager, "")

        return true
    }

    private fun delete(file: File) {
        file.deleteRecursively()
        viewModel.reload()
    }

    private fun rename(file: File) {
        RenameFragment(file).show(childFragmentManager, "")
    }

    private fun rename(file: File, newFile: File) {
        file.renameTo(newFile)
        viewModel.reload()
    }

    private fun createDirectory(name: String) {
        File(viewModel.currentRoot.value, name).mkdir()
        viewModel.reload()
    }

    private fun createFile(name: String) {
        try {
            File(viewModel.currentRoot.value, name).createNewFile()
            viewModel.reload()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.setDir(viewModel.currentRoot.value.parentFile!!)
        }
    }

    private fun export(file: File) {
        viewModel.exportFile = file
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).also {
            it.type = getMimeType(file.name)
            it.putExtra(Intent.EXTRA_TITLE, file.name)
        }

        exportLauncher.launch(intent)
    }

    private val exportLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult

        val uri = it.data?.data ?: return@registerForStartActivityForResult


        GlobalScope.launch(Dispatchers.IO) {
            val context = requireContext()
            val file = viewModel.exportFile
            val result = if (file != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        file.inputStream().use { ins -> ins.copyTo(os) }
                    }
                    true
                } catch (e: IOException) {
                    false
                }
            } else {
                false
            }

            withContext(Dispatchers.Main) {
                if (result) {
                    context.toast("Exported.")
                } else {
                    context.toast(R.string.failed)
                }
            }
        }
    }

    private fun importFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "*/*"
        }

        importLauncher.launch(intent)
    }

    private val importLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult

        val uri = it.data?.data ?: return@registerForStartActivityForResult

        val context = requireContext()
        val name = DocumentFile.fromSingleUri(context, uri)!!.name
        if (name == null) {
            context.toast(R.string.failed)
            return@registerForStartActivityForResult
        }

        val file = File(viewModel.currentRoot.value, name)

        GlobalScope.launch(Dispatchers.IO) {
            val result = try {
                file.outputStream().use { os ->
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.copyTo(os)
                    }
                }
                true
            } catch (e: IOException) {
                false
            }

            withContext(Dispatchers.Main) {
                if (result) {
                    context.toast("Imported.")
                } else {
                    context.toast(R.string.failed)
                }
                viewModel.reload()
            }
        }
    }

    class LongPressFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val file = requireArguments().getSerializable(ARG_FILE) as File
            val items = if (file.isDirectory) {
                arrayOf("Copy", "Delete", "Rename")
            } else {
                arrayOf("Copy", "Delete", "Rename", "Export")
            }

            return AlertDialog.Builder(requireActivity())
                .setTitle(file.name)
                .setItems(items) { _, which ->
                    val parent = parentFragment
                    if (parent !is FileBrowserFragment) return@setItems

                    when (which) {
                        0 -> parent.viewModel.copy = file
                        1 -> parent.delete(file)
                        2 -> parent.rename(file)
                        3 -> parent.export(file)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        companion object {
            operator fun invoke(file: File): LongPressFragment {
                return LongPressFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_FILE, file)
                    }
                }
            }
        }
    }

    class RenameFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val file = requireArguments().getSerializable(ARG_FILE) as File
            val editText = AppCompatEditText(requireActivity())
            editText.setText(file.name)

            return AlertDialog.Builder(requireActivity())
                .setTitle("Rename")
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val parent = parentFragment
                    if (parent !is FileBrowserFragment) return@setPositiveButton

                    parent.rename(file, File(file.parentFile, editText.text.toString()))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        companion object {
            operator fun invoke(file: File): RenameFragment {
                return RenameFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_FILE, file)
                    }
                }
            }
        }
    }

    override fun onTextApply(id: Int, text: String, data: Bundle?) {
        when (id) {
            ID_CREATE_DIR -> createDirectory(text)
            ID_CREATE_FILE -> createFile(text)
        }
    }

    companion object {
        private const val ARG_FILE = "file"

        private const val ID_CREATE_DIR = 1
        private const val ID_CREATE_FILE = 2

        operator fun invoke(path: File?): FileBrowserFragment {
            return FileBrowserFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_FILE, path)
                }
            }
        }
    }
}
