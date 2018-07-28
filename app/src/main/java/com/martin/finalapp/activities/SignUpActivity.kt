package com.martin.finalapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.martin.finalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.martin.finalapp.extensions.*
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
            //in animation, out animation
            //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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

        editTextEmail.validate {
            editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        editTextPassword.validate {
            // Necesita Contener -->    1 Num / 1 Minuscula / 1 Mayuscula / 1 Special / Min Caracteres 4
            editTextPassword.error = if (isValidPassword(it)) null else "Password should contain at least 1 lowercase, 1 uppercase, 1 number & 1 special character, and at least 4 characters"
        }

        editTextConfirmPassword.validate {
            editTextConfirmPassword.error = if (isValidConfirmPassword(editTextConfirmPassword.text.toString(), it)) null else "Confirm password does not match with password"
        }

    }

    private fun signUpByEmail(email: String, password: String){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        toast( "An email has been sent to you. Please, confirm before sign in.")
                        goToActivity<LoginActivity>{
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //if I go back or proceed
                        }
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
                password == editTextConfirmPassword.text.toString() ///3= ->chequea la referencia a los objetos
    }
}