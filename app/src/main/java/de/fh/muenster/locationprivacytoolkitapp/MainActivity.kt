package de.fh.muenster.locationprivacytoolkitapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import de.fh.muenster.locationprivacytoolkit.ui.LocationPrivacyConfigActivity
import de.fh.muenster.locationprivacytoolkitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.configButton.setOnClickListener { view: View ->
            startActivity(Intent(this, LocationPrivacyConfigActivity::class.java))
        }
    }
}