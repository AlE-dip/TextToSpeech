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
    private lateinit var lpwLanguage: ListPopupWindow
    private lateinit var languages: ArrayList<String>
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
                mainViewModel.locale.value = MainViewModel.textToSpeech.availableLanguages
            }
        })

        createListPopupLanguage()

        observeData()
        createAction()
    }

    private fun createListPopupLanguage() {
        lpwLanguage = ListPopupWindow(this, null)
        lpwLanguage.anchorView = binding.aclChoseLanguage
        lpwLanguage.isModal = true
    }

    private fun observeData() {
        mainViewModel.speechRate.observe(this){
            binding.sldSpeechRate.value = it
            binding.txvSpeechRate.text = it.toString()
            MainViewModel.textToSpeech.setSpeechRate(it)
        }

        mainViewModel.locale.observe(this){
            languages = it.map {
                it.displayName
            } as ArrayList<String>
            languages.sort()
            val adapter = ArrayAdapter(this, R.layout.list_popup_window_item, languages)
            lpwLanguage.setAdapter(adapter)
        }
    }

    private fun createAction() {
        binding.sldSpeechRate.addOnChangeListener { slider, value, fromUser ->
            mainViewModel.setSpeechRate(value)
        }

        lpwLanguage.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            Toast.makeText(this, languages.get(position), Toast.LENGTH_SHORT).show()
            lpwLanguage.dismiss()
        }
        binding.aclChoseLanguage.setOnClickListener {
            if(lpwLanguage.isShowing){
                lpwLanguage.dismiss()
            } else {
                lpwLanguage.show()
            }
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