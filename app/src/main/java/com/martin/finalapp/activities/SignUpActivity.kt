package com.martin.finalapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.martin.finalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.martin.finalapp.extensions.goToActivity
import com.martin.finalapp.extensions.toast
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {

    //check current user if it is currently signed in
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        buttonGoLogIn.setOnClickListener {
            goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //if I go back or proceed
            }
        }

        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidEmailAndPassword(email, password)) {
                signUpByEmail(email, password)
            }
            else {
                toast("Please fill all the data and confirm password.")
            }
        }



    }

    private fun signUpByEmail(email: String, password: String){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        toast( "An email has been sent to you. Please, confirm before sign in.")
                    } else {
                        // If sign in fails, display a message to the user.
                        toast("An unexpected error occurred. Please try again.")
                    }
                }
    }

    private fun isValidEmailAndPassword(email: String, password: String) :Boolean {
        return !email.isNullOrEmpty() &&
               !password.isNullOrEmpty() &&
               //!editTextConfirmPassword.text.isNullOrEmpty() && -->NOT NECESSARY
                password == editTextConfirmPassword.text.toString()
    }
}