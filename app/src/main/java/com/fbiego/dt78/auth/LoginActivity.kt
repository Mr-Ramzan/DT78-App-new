package com.fbiego.dt78.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fbiego.dt78.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun OpenSignupPage(view: View?) {
        startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
    }
}