package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
const val RC_SIGN_IN = 123
const val SIGN_IN_RESULT_CODE =321

class AuthenticationActivity : AppCompatActivity() {

    lateinit var fireBaseAuth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        fireBaseAuth = FirebaseAuth.getInstance()
        googleSignIn_button.setOnClickListener {
            signIn()
        }

        signIn_button.setOnClickListener { signInWithEmail() }
    }

    private fun signInWithEmail() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        when(requestCode){
            RC_SIGN_IN->{
                if (requestCode == RC_SIGN_IN) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val exception = task.exception
                    if (task.isSuccessful) {
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)!!
                            Log.d("AuthenticationActivity", "firebaseAuthWithGoogle:" + account.id)
                            firebaseAuthWithGoogle(account.idToken!!)
                        } catch (e: ApiException) {
                            // Google Sign In failed, update UI appropriately
                            Log.w("AuthenticationActivity", "Google sign in failed", e)
                            // ...
                        }
                    } else {
                        Log.w("AuthenticationActivity", exception.toString())
                    }
                }
            }
            SIGN_IN_RESULT_CODE->{
                if (requestCode == SIGN_IN_RESULT_CODE) {
                    val response = IdpResponse.fromResultIntent(data)
                    if (resultCode == Activity.RESULT_OK) {
                        // User successfully signed in
                        Log.i(
                            "AuthenticationActivity",
                            "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                        )
                        Toast.makeText(this,"SignIn Successfull", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, RemindersActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.i("AuthenticationActivity", "Sign in unsuccessful ${response?.error?.errorCode}")
                        Toast.makeText(this,"SignIn failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fireBaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("AuthenticationActivity", "signInWithCredential:success")
                    val user = fireBaseAuth.currentUser
                    Log.d("User",user.toString())
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("AuthenticationActivity", "signInWithCredential:failure", task.exception)

                }

            }
    }
}
