package com.bangkit.crabify.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_classification -> {
                    navController.navigate(R.id.navigation_classification)
                    true
                }

                R.id.navigation_settings -> {
                    navController.navigate(R.id.navigation_settings)
                    true
                }

                else -> false
            }
        }
    }

    fun onNotificationIconClick(view: View) {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

}


