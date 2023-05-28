package com.ale.texttospeech

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Menu
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ale.texttospeech.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.slider.Slider
import java.util.Locale

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

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_setting, R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Create text To Speech
        MainViewModel.textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if(it == TextToSpeech.SUCCESS){
                mainViewModel.locales.value = MainViewModel.textToSpeech.availableLanguages
            }
        })

        observeData()
        createAction()
    }


    private fun observeData() {
        mainViewModel.speechRate.observe(this){
            binding.sldSpeechRate.value = it
            binding.txvSpeechRate.text = it.toString()
            MainViewModel.textToSpeech.setSpeechRate(it)
        }

        mainViewModel.locales.observe(this){
            mainViewModel.languages.value = it.map {
                it.displayName
            } as ArrayList<String>
            mainViewModel.languages.value!!.sort()
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mainViewModel.languages.value!!)
            binding.aclChoseLanguage.setAdapter(adapter)
            mainViewModel.choseLocale(MainViewModel.textToSpeech.language.isO3Country, MainViewModel.textToSpeech.language.isO3Language)
        }

        mainViewModel.choseLocale.observe(this){
            binding.aclChoseLanguage.setText(it.displayName, false)
            MainViewModel.textToSpeech.language = it
        }
    }

    private fun createAction() {
        binding.sldSpeechRate.addOnChangeListener { slider, value, fromUser ->
            mainViewModel.setSpeechRate(value)
        }
        binding.aclChoseLanguage.setOnClickListener {
            if(!binding.aclChoseLanguage.isPopupShowing) {
                binding.aclChoseLanguage.clearFocus()
            }
        }
        binding.aclChoseLanguage.setOnItemClickListener { parent, view, position, id ->
            mainViewModel.choseLocale(mainViewModel.languages.value!!.get(position))
            binding.aclChoseLanguage.clearFocus()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}