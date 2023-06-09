package com.ale.texttospeech.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.ale.texttospeech.MainViewModel
import com.ale.texttospeech.R
import com.ale.texttospeech.databinding.FragmentDialogEnterNameBinding
import com.ale.texttospeech.databinding.FragmentDialogMp3Binding
import com.ale.texttospeech.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import kotlin.math.E


class HomeFragment : Fragment() {

    private var FOLDER_AUDIO: String = "Audio"
    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var saveMp3Launcher: ActivityResultLauncher<Intent>
    private var tag: String = "TAG_TTS"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mainViewModel = activity.run { ViewModelProvider(this!!).get(MainViewModel::class.java) }

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        createAction()
    }

    private fun observeData() {
        homeViewModel.isFabRun.observe(viewLifecycleOwner) {
            if (it) {
                binding.fabRun.setImageResource(R.drawable.pause)
                binding.fabRun.setOnClickListener {
                    if (MainViewModel.textToSpeech.isSpeaking) {
                        MainViewModel.textToSpeech.stop()
                        enableEditText(
                            homeViewModel.text.value,
                            homeViewModel.indexCursor.value ?: 0
                        )
                    } else {
                        homeViewModel.isFabRun.value = false
                    }

                }
            } else {
                binding.fabRun.setImageResource(R.drawable.play)
                binding.fabRun.setOnClickListener {
                    homeViewModel.indexCursor.value = binding.edtMain.selectionStart
                    homeViewModel.text.value = binding.edtMain.text.toString()
                    MainViewModel.textToSpeech.setOnUtteranceProgressListener(
                        utteranceProgressSpeakListener(homeViewModel.indexCursor.value ?: 0)
                    )
                    var text =
                        homeViewModel.text.value?.substring(homeViewModel.indexCursor.value ?: 0)
                    if (text?.trim()?.length == 0) {
                        putSnackbar(getString(R.string.put_cursor))
                        homeViewModel.isFabRun.value = false
                    } else {
                        MainViewModel.textToSpeech.speak(
                            text,
                            TextToSpeech.QUEUE_ADD,
                            null,
                            "new"
                        )
                        homeViewModel.isFabRun.value = true
                    }
                }
            }
        }
    }

    private fun createAction() {
        binding.root.setOnClickListener {
            showKeyboard()
        }

        mainViewModel.onExportMp3Listener = object : MainViewModel.OnExportMp3Listener {
            override fun export() {
                var text = binding.edtMain.text.toString().trim()
                if (text.length != 0) {
                    saveMp3FileToStorage(binding.edtMain.text.toString())
                } else {
                    putSnackbar(getString(R.string.input_text))
                }
            }
        }
    }

    fun saveMp3FileToStorage(text: String) {
        var dialogNameBinding = FragmentDialogEnterNameBinding.inflate(layoutInflater)
        var dialog = showDialog(context, dialogNameBinding)

        dialogNameBinding.edtName.setText((text).takeIf { it.length < 30 } ?: text.substring(0, 30))

        dialogNameBinding.btnClose.setOnClickListener {
            dialog?.cancel()
        }

        dialogNameBinding.btnSave.setOnClickListener {
            val name = dialogNameBinding.edtName.text.toString().trim()
            if (name.length > 0) {
                var root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                var folder = File(root, FOLDER_AUDIO)
                if (!folder.exists()) {
                    folder.mkdir()
                }
                var file = File(folder, name + ".mp3")
                if (file.exists()) {
                    file.delete()
                }
                showDialogExportMp3(binding.edtMain.text?.length!!, file)
                MainViewModel.textToSpeech.synthesizeToFile(
                    binding.edtMain.text.toString(), null, file, "new"
                )
                dialog?.cancel()
            }
        }

        dialog?.show()
    }

    private fun showDialogExportMp3(length: Int, file: File) {
        val dialogMp3Binding = FragmentDialogMp3Binding.inflate(layoutInflater)
        var dialog = showDialog(context, dialogMp3Binding)

        dialogMp3Binding.btnCancel.setOnClickListener {
            MainViewModel.textToSpeech.stop()
            if (file.exists()) {
                file.delete()
            }
        }

        dialogMp3Binding.btnClose.setOnClickListener {
            dialog?.cancel()
        }

        dialogMp3Binding.btnOpen.setOnClickListener {

            val fileUri = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                FOLDER_AUDIO
            ).toUri()

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "resource/folder")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

        MainViewModel.textToSpeech.setOnUtteranceProgressListener(
            utteranceProgressExportAudioListener(dialogMp3Binding, length)
        )

        dialog?.show()
    }

    fun showDialog(context: Context?, viewBinding: ViewBinding): AlertDialog? {
        context?.let {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.action_export_file)
            builder.setView(viewBinding.root)

            val dialog: AlertDialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }
        return null
    }

    fun putSnackbar(message: String) {
        val snackbar = Snackbar.make(binding.fabRun, message, Snackbar.LENGTH_LONG).setAction(
            "Action", null
        )
        context?.let {
            var drawable = ContextCompat.getDrawable(it, R.drawable.snackbar_background)
            snackbar.view.background = drawable
        }
        snackbar.show()
    }

    fun utteranceProgressSpeakListener(indexCursor: Int): UtteranceProgressListener {
        return object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                binding.root.post {
                    binding.root.isEnabled = false
                    binding.edtMain.isEnabled = false
                    hideKeyboard()
                    Log.d(tag, "utterance started");
                }
            }

            override fun onDone(utteranceId: String?) {
                enableEditText(homeViewModel.text.value ?: "", indexCursor)
            }

            override fun onError(utteranceId: String?) {
                putSnackbar(getString(R.string.error))
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                val spannableString = SpannableString(homeViewModel.text.value)
                val colorSpan = ForegroundColorSpan(resources.getColor(R.color.light_blue))
                spannableString.setSpan(
                    colorSpan,
                    indexCursor + start,
                    indexCursor + end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.edtMain.post {
                    binding.edtMain.setText(spannableString)
                }
            }
        }
    }

    fun utteranceProgressExportAudioListener(
        dialogMp3Binding: FragmentDialogMp3Binding,
        length: Int
    ): UtteranceProgressListener {
        return object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }

            override fun onDone(utteranceId: String?) {
                dialogMp3Binding.pcsExportAudio.post {
                    dialogMp3Binding.pcsExportAudio.progress = 100
                    dialogMp3Binding.btnCancel.visibility = View.GONE
                    dialogMp3Binding.btnClose.visibility = VISIBLE
                    dialogMp3Binding.btnOpen.visibility = VISIBLE
                }
            }

            override fun onError(utteranceId: String?) {
                putSnackbar(getString(R.string.error))
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                dialogMp3Binding.pcsExportAudio.post {
                    dialogMp3Binding.pcsExportAudio.progress = ((end * 100) / length!!)
                }

                Log.d(tag, "" + frame + "    " + start + "   " + end)
            }
        }
    }

    fun enableEditText(text: String?, indexCursor: Int) {
        binding.root.post {
            binding.root.isEnabled = true
            binding.edtMain.isEnabled = true
            binding.edtMain.setText(SpannableString(text))
            binding.edtMain.setSelection(indexCursor)
            homeViewModel.isFabRun.value = false
            Log.d(tag, "utterance done")
        }
    }

    fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    fun showKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        binding.edtMain.requestFocus()
        imm?.showSoftInput(binding.edtMain, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}