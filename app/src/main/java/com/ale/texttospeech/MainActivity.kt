package com.ale.texttospeech

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ale.texttospeech.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContentView(binding.root)


        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navHostFragment, appBarConfiguration)
        navView.setupWithNavController(navHostFragment)

        observeData()
        createAction()

        //Create text To Speech
        MainViewModel.textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                mainViewModel.locales.value = MainViewModel.textToSpeech.availableLanguages
                mainViewModel.choseDefaultLocale(
                    MainViewModel.textToSpeech.language?.isO3Country,
                    MainViewModel.textToSpeech.language?.isO3Language
                )
                MainViewModel.textToSpeech.setSpeechRate(mainViewModel.speechRate.value!!)
                MainViewModel.textToSpeech.setPitch(mainViewModel.pitch.value!!)
                MainViewModel.textToSpeech.language = mainViewModel.choseLocale.value
                MainViewModel.textToSpeech.voice = mainViewModel.choseVoice.value
            }
        })
    }


    private fun observeData() {
        mainViewModel.speechRate.observe(this) {
            binding.sldSpeechRate.value = it
            binding.txvSpeechRate.text = it.toString()
            MainViewModel.textToSpeech?.setSpeechRate(it)
        }

        mainViewModel.pitch.observe(this) {
            binding.sldPitch.value = it
            binding.txvPitch.text = it.toString()
            MainViewModel.textToSpeech?.setPitch(it)
        }

        mainViewModel.locales.observe(this) {
            mainViewModel.languages.value = it.map {
                it.displayName
            } as ArrayList<String>
            mainViewModel.languages.value!!.sort()
            val adapter = ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, mainViewModel.languages.value!!
            )
            binding.aclChoseLanguage.setAdapter(adapter)
        }

        mainViewModel.choseLocale.observe(this) {
            binding.aclChoseLanguage.setText(it.displayName, false)
            MainViewModel.textToSpeech?.language = it
            mainViewModel.setVoices(MainViewModel.textToSpeech?.voices)
        }

        mainViewModel.voices.observe(this) {
            mainViewModel.voiceNames.value = it.map {
                it.name
            } as ArrayList<String>
            mainViewModel.voiceNames.value!!.sort()
            val adapter = ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, mainViewModel.voiceNames.value!!
            )
            binding.aclChoseVoice.setAdapter(adapter)
        }

        mainViewModel.choseVoice.observe(this) {
            MainViewModel.textToSpeech?.voice = it
            binding.aclChoseVoice.setText(it.name, false)
        }
    }

    private fun createAction() {
        binding.sldSpeechRate.addOnChangeListener { slider, value, fromUser ->
            mainViewModel.setSpeechRate(value)
        }

        binding.sldPitch.addOnChangeListener { slider, value, fromUser ->
            mainViewModel.setPitch(value)
        }

        binding.aclChoseLanguage.setOnClickListener {
            if (!binding.aclChoseLanguage.isPopupShowing) {
                binding.aclChoseLanguage.clearFocus()
            }
        }

        binding.aclChoseLanguage.setOnItemClickListener { parent, view, position, id ->
            mainViewModel.choseLocale(mainViewModel.languages.value!!.get(position))
            binding.aclChoseLanguage.clearFocus()
        }

        binding.aclChoseVoice.setOnItemClickListener { parent, view, position, id ->
            mainViewModel.choseVoice(mainViewModel.voiceNames.value!!.get(position))
            binding.aclChoseVoice.clearFocus()
        }

        binding.aclChoseVoice.setOnClickListener {
            if (!binding.aclChoseVoice.isPopupShowing) {
                binding.aclChoseVoice.clearFocus()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = findNavController(R.id.nav_host_fragment_content_main)
        return navHostFragment.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_file -> {
                mainViewModel.onExportMp3Listener?.let {
                    it.export()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}