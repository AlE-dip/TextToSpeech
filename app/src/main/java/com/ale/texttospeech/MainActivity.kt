package com.ale.texttospeech

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
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
    companion object {
        var isCreatedSpeecher : Boolean = false
        lateinit var textToSpeech : TextToSpeech
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if(it == TextToSpeech.SUCCESS){
                isCreatedSpeecher = true
            }
        })

        createAction()
    }

    private fun createAction() {
        binding.sldSpeechRate.addOnChangeListener { slider, value, fromUser ->
            val roundedValue = roundDecimal(value, 1);
            textToSpeech.setSpeechRate(roundedValue)
            binding.txvSpeechRate.setText(roundedValue.toString())
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

    fun roundDecimal(number: Float, decimalPlaces: Int): Float {
        val factor = Math.pow(10.0, decimalPlaces.toDouble()).toFloat()
        return (Math.round(number * factor) / factor)
    }
}