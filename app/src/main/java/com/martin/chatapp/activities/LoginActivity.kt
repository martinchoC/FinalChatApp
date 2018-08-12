package com.martin.chatapp.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.martin.chatapp.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.martin.chatapp.MainActivity
import com.martin.chatapp.extensions.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mGoogleApiClient: GoogleApiClient by lazy { getGoogleApiClient() }
    private val RC_GOOGLE_SIGN_IN = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogIn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidEmail(email) && isValidPassword(password)) {
                logInByEmail(email, password)
            }
            else {
                toast("Please make sure all data is correct.")
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

        buttonLogInGoogle.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        editTextEmail.validate {
            editTextEmail.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        editTextPassword.validate {
            // Necesita Contener -->    1 Num / 1 Minuscula / 1 Mayuscula / 1 Special / Min Caracteres 4
            editTextPassword.error = if (isValidPassword(it)) null else "Password should contain at least 1 lowercase, 1 uppercase, 1 number & 1 special character, and at least 4 characters"
        }

    }

    private fun logInByGoogleAccountIntoFirebase(googleAccount: GoogleSignInAccount) {
        //get credentials
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) {
            if (mGoogleApiClient.isConnected) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            goToActivity<MainActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }

    private fun logInByEmail (email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (mAuth.currentUser!!.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            goToActivity<MainActivity> {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        }
                        else {
                            toast("User must confirm email first.")
                        }
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        toast("An unexpected error occurred, please try again")
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                logInByGoogleAccountIntoFirebase(account!!)
            }
        }
    }

    private fun getGoogleApiClient(): GoogleApiClient {
        //google sign in options value
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        toast("Connection has failed")
    }

}

