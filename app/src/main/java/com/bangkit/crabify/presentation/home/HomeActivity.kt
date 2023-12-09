package com.bangkit.crabify.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.ActivityHomeBinding
import com.bangkit.crabify.presentation.notification.NotificationActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = layoutInflater.inflate(R.layout.custom_action_bar, null) as Toolbar
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_activity_main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigationView
        bottomNav.setupWithNavController(navController)
    }

    fun onNotificationIconClick(view: View) {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }
}


