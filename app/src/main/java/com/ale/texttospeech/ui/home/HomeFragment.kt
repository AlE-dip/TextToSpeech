package com.ale.texttospeech.ui.home

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.ale.texttospeech.MainViewModel
import com.ale.texttospeech.R
import com.ale.texttospeech.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File


class HomeFragment : Fragment() {

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
        registerSaveMp3Result()
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
                        utteranceProgressListener(homeViewModel.indexCursor.value ?: 0)
                    )
                    var text = homeViewModel.text.value?.substring(homeViewModel.indexCursor.value ?: 0)
                    if(text?.trim()?.length == 0){
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
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/mpeg" // Set the MIME type to audio/mpeg for MP3 files
        intent.putExtra(
            Intent.EXTRA_TITLE,
            (text + ".mp3").takeIf { it.length < 24 } ?: text.substring(0, 20) + ".mp3"
        ) // Set the desired file name with .mp3 extension
        saveMp3Launcher.launch(intent)
    }

    fun registerSaveMp3Result() {
        saveMp3Launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    it.data?.data?.let {
                        var descriptor = uriToParcelFileDescriptor(context, it)
                        descriptor?.let { des ->
                            var bundle = Bundle()
                            MainViewModel.textToSpeech.synthesizeToFile(
                                binding.edtMain.text.toString(), bundle, des, "new"
                            )
                            Toast.makeText(context, "dsdsd", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    fun uriToParcelFileDescriptor(context: Context?, uri: Uri): ParcelFileDescriptor? {
        val contentResolver: ContentResolver? = context?.contentResolver
        return try {
            contentResolver?.openFileDescriptor(uri, "w")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    fun utteranceProgressListener(indexCursor: Int): UtteranceProgressListener {
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
                Log.d(tag, "utterance error");
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