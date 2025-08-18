package com.forkbombsquad.stillalivelarp.views.startpage

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.VersionService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.forkbombsquad.stillalivelarp.views.home.HomeActivity
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.tryOptional
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch


class MainActivity : NoStatusBarActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private lateinit var logInButton: LoadingButton
    private lateinit var createAccountButton: LoadingButton
    private lateinit var contactButton: LoadingButton
    private lateinit var offlineModeButton: LoadingButton
    private lateinit var stayLoggedInCheckbox: CheckBox

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        usernameField = findViewById(R.id.username_edit_text)
        passwordField = findViewById(R.id.password_edit_text)

        var u = UserAndPassManager.shared.getTempU(this)
        var p = UserAndPassManager.shared.getTempP(this)

        if (u == null && p == null && UserAndPassManager.shared.getRemember(this)) {
            u = UserAndPassManager.shared.getU(this)
            p = UserAndPassManager.shared.getP(this)
        }

        usernameField.setText(u ?: "")
        passwordField.setText(p ?: "")

        stayLoggedInCheckbox = findViewById<CheckBox>(R.id.stay_logged_in_checkbox)

        logInButton = findViewById(R.id.log_in_button)
        createAccountButton = findViewById(R.id.create_account_button)
        contactButton = findViewById(R.id.contact_us_button)
        offlineModeButton = findViewById(R.id.offline_mode_button)

        logInButton.setOnClick {
            logInButton.setLoadingWithText("Checking Credentials...")
            val validationResult = Validator.validateMultiple(arrayOf(ValidationGroup(usernameField, ValidationType.EMAIL), ValidationGroup(passwordField, ValidationType.PASSWORD)))
            if (!validationResult.hasError) {
                val versionRequest = VersionService()
                lifecycleScope.launch {
                    versionRequest.successfulResponse().ifLet({ versionModel ->
                        val pInfo = tryOptional {
                            this@MainActivity.packageManager.getPackageInfo(this@MainActivity.packageName, 0)
                        }
                        val currentVersion = tryOptional {
                            pInfo?.longVersionCode?.toInt() ?: 0
                        } ?: 0

                        if (currentVersion < versionModel.androidVersion) {
                            AlertUtils.displayMessage(
                                context = this@MainActivity,
                                title = "Update Required!",
                                message = "Your version of the Still Alive Larp App is outdated. Please visit the Google Play Store to update in order to use the online features! \n\nCurrent Build Number: $currentVersion\nTarget Build Number: ${versionModel.androidVersion}",
                                buttons = arrayOf(
                                    AlertButton("Open Google Play Store", { _, _ ->
                                        try {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                                        } catch (e: ActivityNotFoundException) {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                                        }
                                        logInButton.setLoading(false)
                                    }, ButtonType.POSITIVE),
                                    AlertButton("Cancel", {_, _ ->
                                        logInButton.setLoading(false)
                                    }, ButtonType.NEUTRAL)
                                )
                            )
                        } else {
                            signIn()
                        }
                    }, {
                        logInButton.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayOkMessage(this, "Validation Error(s)", validationResult.getErrorMessages())
                logInButton.setLoading(false)
            }
        }

        createAccountButton.setOnClick {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        contactButton.setOnClick {
            val intent = Intent(this, ContactActivity::class.java)
            startActivity(intent)
        }

        offlineModeButton.setOnClick {
            DataManager.shared.setOfflineModeExternally(true)
            DataManager.shared.load(lifecycleScope, finished = {
                if (DataManager.shared.players.isEmpty()) {
                    AlertUtils.displayOkMessage(this, "Not Available", "You must successfully sign in at least once on this device to store the info required to use offline mode")
                } else {
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            })
        }
    }

    private fun signIn() {
        DataManager.shared.setOfflineModeExternally(false)
        UserAndPassManager.shared.setUandP(usernameField.text.toString().lowercase(), passwordField.text.toString(), stayLoggedInCheckbox.isChecked)
        logInButton.setLoadingWithText("Fetching Player Info...")
        val service = PlayerService.SignInPlayer()
        lifecycleScope.launch {
            service.successfulResponse().ifLet({ playerModel ->
                DataManager.shared.setCurrentPlayerIdExternally(playerModel)
                UserAndPassManager.shared.clearTemp()
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                logInButton.setLoading(false)
                startActivity(intent)
            }, {
                logInButton.setLoading(false)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        var u = UserAndPassManager.shared.getTempU()
        var p = UserAndPassManager.shared.getTempP()

        if (u == null && p == null && UserAndPassManager.shared.getRemember()) {
            u = UserAndPassManager.shared.getU()
            p = UserAndPassManager.shared.getP()
        }

        usernameField.setText(u ?: "")
        passwordField.setText(p ?: "")
    }

}