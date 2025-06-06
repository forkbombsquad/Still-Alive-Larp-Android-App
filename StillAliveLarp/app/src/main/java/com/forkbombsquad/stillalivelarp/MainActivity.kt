package com.forkbombsquad.stillalivelarp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.VersionService
import com.forkbombsquad.stillalivelarp.services.managers.PlayerManager
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication.Companion.context
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

        StillAliveLarpApplication.activity = this

        usernameField = findViewById(R.id.username_edit_text)
        passwordField = findViewById(R.id.password_edit_text)

        var u = UserAndPassManager.shared.getTempU(context)
        var p = UserAndPassManager.shared.getTempP(context)

        if (u == null && p == null && UserAndPassManager.shared.getRemember(context)) {
            u = UserAndPassManager.shared.getU(context)
            p = UserAndPassManager.shared.getP(context)
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
                            context.packageManager.getPackageInfo(context.packageName, 0)
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
            if (SharedPrefsManager.shared.getPlayer() == null) {
                AlertUtils.displayOkMessage(this, "Not Available", "You must successfully sign in at least once on this device to store your character and player for offline mode use")
            } else {
                val intent = Intent(this, OfflineMyAccountActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun signIn() {
        logInButton.setLoadingWithText("Fetching Player Info...")
        UserAndPassManager.shared.setUandP(context, usernameField.text.toString(), passwordField.text.toString(), stayLoggedInCheckbox.isChecked)
        logInButton.setLoadingWithText("Fetching Player Info...")
        val service = PlayerService.SignInPlayer()
        lifecycleScope.launch {
            service.successfulResponse().ifLet({ playerModel ->
                UserAndPassManager.shared.clearTemp(this@MainActivity)
                PlayerManager.shared.setPlayer(playerModel)
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
        var u = UserAndPassManager.shared.getTempU(context)
        var p = UserAndPassManager.shared.getTempP(context)

        if (u == null && p == null && UserAndPassManager.shared.getRemember(context)) {
            u = UserAndPassManager.shared.getU(context)
            p = UserAndPassManager.shared.getP(context)
        }

        usernameField.setText(u ?: "")
        passwordField.setText(p ?: "")
    }

}