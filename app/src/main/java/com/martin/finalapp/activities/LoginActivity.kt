package com.martin.finalapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.martin.finalapp.R
import com.martin.finalapp.extensions.toast
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Toast
import com.martin.finalapp.extensions.goToActivity


class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogIn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if(isValidEmailAndPassword(email, password)) {
                logInByEmail(email, password)
            }
            else {

            }
        }

        textForgotPassword.setOnClickListener {
            goToActivity<ForgotPasswordActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        buttonCreateAccount.setOnClickListener {
            goToActivity<SignUpActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun logInByEmail (email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                       toast("User is now logged in.")
                    } else {
                        // If sign in fails, display a message to the user.
                       toast("An unexpected error occurred, please try again")
                    }
                }
    }

    private fun isValidEmailAndPassword (email: String, password: String): Boolean {
        return !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }
}
