package com.fbiego.dt78.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fbiego.dt78.databinding.ActivitySignupBinding
import com.fbiego.dt78.firebase.FirebaseMethods
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber


class SignupActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var mBinding: ActivitySignupBinding

    ///////////////////////Firebase////////////////////////
    var firebaseMethods: FirebaseMethods? = null
    var database: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        firebaseMethods = FirebaseMethods(this)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.reference

        setListeners()
    }

    private fun setListeners() {
        mBinding.fab.bringToFront()
        mBinding.fab.setOnClickListener {
            processSignUp()
        }
        mBinding.signinButton.setOnClickListener {
            goToLogin()
        }

    }

    private fun processSignUp() {
        if (mBinding.email.text.isNullOrBlank()) {
            Toast.makeText(this@SignupActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
        } else {
            if (mBinding.passwordText.text.isNullOrBlank() || mBinding.passwordText.text.length < 6) {

                Toast.makeText(this@SignupActivity, "Password should be minimum of 6 characters", Toast.LENGTH_SHORT).show()

            } else {
                if (mBinding.confirmPassword.text.isNullOrBlank() || mBinding.confirmPassword.text.length < 6 || mBinding.confirmPassword.text.toString() != mBinding.passwordText.text.toString()) {
                    Toast.makeText(this@SignupActivity, "Password must match", Toast.LENGTH_SHORT).show()
                } else {
                    signUp(mBinding.email.text.toString(), mBinding.passwordText.text.toString())
                }
            }
        }
    }


    private fun signUp(email: String, password: String) {
        firebaseMethods?.signUp (email, password)
        {
            if(it){
                goToLogin()
            }
        }

    }

    private fun goToLogin() {

        startActivity(Intent(this, LoginActivity::class.java))
        Handler(Looper.getMainLooper()).postDelayed({
            this@SignupActivity.finish()
        }, 1500)

    }
}