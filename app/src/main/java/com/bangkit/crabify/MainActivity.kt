package com.bangkit.crabify

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isCrabifyAppStarted.value
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.8f,
                    0.0f
                )
                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.8f,
                    0.0f
                )

                zoomX.interpolator = OvershootInterpolator()
                zoomY.interpolator = OvershootInterpolator()
                zoomX.duration = 500
                zoomY.duration = 500
                zoomX.doOnEnd {
                    screen.remove()
                    checkSession()
                }
                zoomX.start()
                zoomY.doOnEnd {
                    screen.remove()
                    checkSession()
                }
                zoomY.start()
            }
        }
        setContentView(R.layout.activity_main)
    }

    private fun checkSession() {
        viewModel.getSession { user ->
            if (user != null) {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_onBoardingFragment_to_homeActivity)
//                val intent = Intent(this, HomeActivity::class.java)
//                startActivity(intent)
//                finish()
            } else {
                findNavController(R.id.nav_host_fragment)
            }
        }
    }
}
