package com.ale.texttospeech.ui.home

import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.provider.Contacts
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.ale.texttospeech.MainActivity
import com.ale.texttospeech.MainViewModel
import com.ale.texttospeech.R
import com.ale.texttospeech.databinding.FragmentHomeBinding
import java.lang.StringBuilder
import java.util.Locale
import java.util.Objects


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private var tag: String = "TAG_TTS"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)


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
        var text = StringBuilder()

        homeViewModel.isFabRun.observe(viewLifecycleOwner) {
            if (it) {
                binding.fabRun.setImageResource(R.drawable.pause)
                binding.fabRun.setOnClickListener {
                    if(MainViewModel.textToSpeech.isSpeaking){
                        MainViewModel.textToSpeech.stop()
                        enableEditText(text.toString())
                    } else {
                        homeViewModel.isFabRun.value = false
                    }

                }
            } else {
                binding.fabRun.setImageResource(R.drawable.play)
                binding.fabRun.setOnClickListener {
                    text.clear()
                    text.append(binding.edtMain.text.toString())
                    MainViewModel.textToSpeech.setOnUtteranceProgressListener(
                        utteranceProgressListener(text.toString())
                    )
                    MainViewModel.textToSpeech.speak(
                        text, TextToSpeech.QUEUE_ADD, null, "new"
                    )
                    homeViewModel.isFabRun.value = true
                }
            }
        }
    }

    private fun createAction() {
        binding.root.setOnClickListener {
            showKeyboard()
        }
    }

    fun utteranceProgressListener(text: String): UtteranceProgressListener {
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
                enableEditText(text)
            }

            override fun onError(utteranceId: String?) {
                Log.d(tag, "utterance error");
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                val spannableString = SpannableString(text)
                val colorSpan = ForegroundColorSpan(resources.getColor(R.color.light_blue))
                spannableString.setSpan(
                    colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.edtMain.post {
                    binding.edtMain.setText(spannableString)
                }
            }
        }
    }

    fun enableEditText(text: String){
        binding.root.post {
            binding.root.isEnabled = true
            binding.edtMain.isEnabled = true
            binding.edtMain.setText(SpannableString(text))
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