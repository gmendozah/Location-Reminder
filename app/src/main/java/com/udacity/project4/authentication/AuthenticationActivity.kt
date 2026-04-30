package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
    }

    private val viewModel by viewModels<LoginViewModel>()

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityAuthenticationBinding>(
            this,
            R.layout.activity_authentication
        )

        binding.authButton.setOnClickListener {
            launchSignInFlow()
        }

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else -> {
                    Log.e(TAG, "Authentication state: $authenticationState")
                }
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.custom_sign_in_layout)
            .setGoogleButtonId(R.id.google_button)
            .setEmailButtonId(R.id.email_button)
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.AppTheme)
            .setAuthMethodPickerLayout(customLayout)
            .build()

        authLauncher.launch(signInIntent)
    }
}