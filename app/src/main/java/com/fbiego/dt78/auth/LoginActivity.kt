package com.fbiego.dt78.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fbiego.dt78.MainActivity
import com.fbiego.dt78.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding
    private var mAuth: FirebaseAuth? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        checkLoginStatus()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setListeners()
    }

    private fun checkLoginStatus(){
        if(mAuth?.currentUser !=null){
            goToHome()
        }
    }

    private fun setListeners(){
        mBinding.fabG.bringToFront();

        mBinding.fabG.setOnClickListener {
            processSignUp()
        }
        mBinding.signupButton.setOnClickListener {
           openSignUpPage()
        }

    }
    private fun processSignUp() {
        if (mBinding.emailText.text.isNullOrBlank()) {
            Toast.makeText(this@LoginActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
        } else {
            if (mBinding.passwordText.text.isNullOrBlank() || mBinding.passwordText.text.length < 6) {

                Toast.makeText(this@LoginActivity, "Invalid Password", Toast.LENGTH_SHORT).show()

            } else {

                    login(mBinding.emailText.text.toString(), mBinding.passwordText.text.toString())

            }
        }
    }

    private fun login(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInWithEmail:success");
                        goToHome()
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.d("signInWithEmail:failure ${task.getException()}");
                        Toast.makeText(this@LoginActivity, "SignIn failed!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private fun openSignUpPage() {
        startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        Handler(Looper.getMainLooper()).postDelayed({
            this.finish()
        }, 1500)
    }
    private fun goToHome() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        Handler(Looper.getMainLooper()).postDelayed({
            this.finish()
        }, 1500)
    }
}