package com.udacity.project4

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_flash_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SplashScreen : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    var user: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_screen)
        firebaseAuth = FirebaseAuth.getInstance()

        login_button.setOnClickListener {
            performLogin()
        }

    }

    private fun performLogin() {
        progressBar.isVisible = true
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                user = firebaseAuth.currentUser
            }
            if (user != null) {
                startActivity(Intent(this@SplashScreen, RemindersActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, AuthenticationActivity::class.java))
            }
            progressBar.isVisible = false
        }
    }


}

