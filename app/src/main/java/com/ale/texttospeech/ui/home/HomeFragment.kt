package com.ale.texttospeech.ui.home

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.ale.texttospeech.MainActivity
import com.ale.texttospeech.MainViewModel
import com.ale.texttospeech.databinding.FragmentHomeBinding
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
        createAction()
    }

    private fun observeData() {

    }

    private fun createAction() {
        binding.root.setOnClickListener {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            binding.edtMain.requestFocus()
            imm?.showSoftInput(binding.edtMain, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.fabRun.setOnClickListener {
            var text = binding.edtMain.text.toString()
            MainViewModel.textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null)
            Toast.makeText(context, MainViewModel.textToSpeech.language.isO3Country + " " + MainViewModel.textToSpeech.language.isO3Language, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}