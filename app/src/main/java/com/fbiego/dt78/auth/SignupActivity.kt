package com.fbiego.dt78.auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fbiego.dt78.R
import com.fbiego.dt78.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.internal.util.HalfSerializer.onComplete


class SignupActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var mBinding:ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mAuth = FirebaseAuth.getInstance();
    }

    private fun setListeners(){

        mBinding.root
    }



    private fun signup(email:String,password:String){
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this,
            OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Signup", "signInWithEmail:success");
                    var  user: FirebaseUser? = mAuth?.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Signup", "signInWithEmail:failure", task.getException());
                    Toast.makeText(this@SignupActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();

                }
            });}
}