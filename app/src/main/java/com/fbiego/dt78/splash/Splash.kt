package com.fbiego.dt78.splash

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.fbiego.dt78.MainActivity
import com.fbiego.dt78.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    private val SPLASH_SCREEN_TIME_OUT = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        changeStatusBarColor()
        //this will bind your MainActivity.class file with activity_main___.
       findViewById<LottieAnimationView>(R.id.lottie_splash_view).addAnimatorUpdateListener { valueAnimator ->
            // Set animation progress
            val progress = (valueAnimator.animatedValue as Float * 100).toInt()
            if (progress>=99){

                //this will bind your MainActivity.class file with activity_main___.

                    val i = Intent(
                            this@Splash,
                            MainActivity::class.java
                    )
                    //Intent is used to switch from one activity to another.
                    startActivity(i)
                    //invoke the SecondActivity.
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                    //the current activity will get finished.
                }, SPLASH_SCREEN_TIME_OUT.toLong())
            }
        }


    }

    /**
     * Making notification bar transparent
     */
      private fun changeStatusBarColor() {
        CoroutineScope(Dispatchers.Main).launch {

            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = Color.TRANSPARENT
                }
            } catch (e: Exception) {

                Log.i("Splash", "Duringchange status bar color ====>${e.printStackTrace()}")
            }
        }
    }
}