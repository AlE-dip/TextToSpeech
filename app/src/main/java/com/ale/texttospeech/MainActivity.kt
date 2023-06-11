package com.ale.texttospeech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ale.texttospeech.core.AskPermission
import com.ale.texttospeech.core.database.Setting
import com.ale.texttospeech.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private val settingViewModel: SettingViewModel by lazy {
        ViewModelProvider(this, SettingViewModel.SettingViewModelFactory(application)).get(
            SettingViewModel::class.java
        )
    }

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
        mainViewModel.createSpeech(this, settingViewModel)
    }


    private fun observeData() {
        mainViewModel.speechRate.observe(this) {
            binding.sldSpeechRate.value = it
            binding.txvSpeechRate.text = it.toString()
            mainViewModel.textToSpeech.value?.setSpeechRate(it)
        }

        mainViewModel.pitch.observe(this) {
            binding.sldPitch.value = it
            binding.txvPitch.text = it.toString()
            mainViewModel.textToSpeech.value?.setPitch(it)
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
            mainViewModel.textToSpeech.value?.language = it
            mainViewModel.setVoices(mainViewModel.textToSpeech.value?.voices)
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
            mainViewModel.textToSpeech.value?.voice = it
            binding.aclChoseVoice.setText(it.name, false)
        }
    }

    private fun createAction() {
        binding.sldSpeechRate.addOnChangeListener { slider, value, fromUser ->
            if(fromUser) {
                mainViewModel.setSpeechRate(value)
                mainViewModel.setting.value!!.speechRate = value
                settingViewModel.updateSetting(mainViewModel.setting.value!!)
            }
        }

        binding.sldPitch.addOnChangeListener { slider, value, fromUser ->
            if(fromUser){
                mainViewModel.setPitch(value)
                mainViewModel.setting.value!!.pitch = value
                settingViewModel.updateSetting(mainViewModel.setting.value!!)
            }
        }

        binding.aclChoseLanguage.setOnClickListener {
            if (!binding.aclChoseLanguage.isPopupShowing) {
                binding.aclChoseLanguage.clearFocus()
            }
        }

        binding.aclChoseLanguage.setOnItemClickListener { parent, view, position, id ->
            var local = mainViewModel.languages.value!!.get(position)
            mainViewModel.choseLocale(local)
            binding.aclChoseLanguage.clearFocus()
            mainViewModel.setting.value!!.language = mainViewModel.localToString(
                mainViewModel.choseLocale.value
            )
            settingViewModel.updateSetting(mainViewModel.setting.value!!)
        }

        binding.aclChoseVoice.setOnItemClickListener { parent, view, position, id ->
            mainViewModel.choseVoice(mainViewModel.voiceNames.value!!.get(position))
            binding.aclChoseVoice.clearFocus()
            mainViewModel.setting.value!!.voice = mainViewModel.choseVoice.value?.name
            settingViewModel.updateSetting(mainViewModel.setting.value!!)
        }

        binding.aclChoseVoice.setOnClickListener {
            if (!binding.aclChoseVoice.isPopupShowing) {
                binding.aclChoseVoice.clearFocus()
            }
        }

        mainViewModel.onUpdateTextListener = object : MainViewModel.OnUpdateTextListener {
            override fun update(text: String) {
                mainViewModel.setting.value!!.currentText = text
                settingViewModel.updateSetting(mainViewModel.setting.value!!)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        val navHostFragment = findNavController(R.id.nav_host_fragment_content_main)
        return navHostFragment.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_export_file -> {
                if (AskPermission.filePermission(this)) {
                    exportFileMp3()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun exportFileMp3() {
        mainViewModel.onExportMp3Listener?.let { it.export() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AskPermission.REQUEST_FILE_CODE) {
            exportFileMp3()
        }
    }
}