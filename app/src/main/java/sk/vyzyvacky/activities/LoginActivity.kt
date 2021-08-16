package sk.vyzyvacky.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.model.Game
import sk.vyzyvacky.utilities.QrCodeScanner
import sk.vyzyvacky.utilities.data.DataHandler
import sk.vyzyvacky.utilities.internet.LoginRequest

class LoginActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler

    private val GAME_CODE_LENGTH: Int = 1 + 5
    private val MIN_PASSWORD_LENGTH: Int = 4

    private val CODE_KEY: String = "c"
    private val PASSWORD_KEY: String = "p"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dataHandler = DataHandler(this.applicationContext)

        //login_toolbar.setTitleTextAppearance(this, R.style.CustomLoginTitle)

        gamecodeField.doAfterTextChanged {
            textWatch()

            if (gamecodeField.text.toString().trim().first() != '#') {
                gamecodeField.text?.insert(0, "#")
            }
        }

        passwordField.doAfterTextChanged {
            textWatch()
        }

        login_button.setOnClickListener {
            if (passwordLengthValidator()) {
                //prevent button spam
                login_button.isEnabled = false

                //TODO require certain code length

                sendRequest(
                    gamecodeField.text.toString().trim(),
                    passwordField.text.toString().trim()
                )
            }
        }

        qr_button.setOnClickListener {
            //prevent button spam
            qr_button.isEnabled = false
            QrCodeScanner.scan(this, resources)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val jsonObject = QrCodeScanner.getOutput(requestCode, resultCode, data)

        if (jsonObject != null) {
            if (jsonObject.length() != 0) {
                try {
                    val code = jsonObject.getString(CODE_KEY)
                    val password = jsonObject.getString(PASSWORD_KEY)
                    return sendRequest(code, password)
                } catch (e: JSONException) {
                    Toast.makeText(this,
                        resources.getText(R.string.error_qr_damaged),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Toast.makeText(this, resources.getText(R.string.error_qr_wrong), Toast.LENGTH_SHORT)
                .show()
        }
        qr_button.isEnabled = true
        return


    }

    private fun passwordLengthValidator(): Boolean {
        return if (passwordField.text.toString().trim().length < MIN_PASSWORD_LENGTH) {
            passwordLayout.error = getString(R.string.error_pass_length)
            false
        } else {
            passwordLayout.error = null
            true
        }
    }

    private fun textWatch() {
        val usernameEmpty: Boolean = gamecodeField.text.toString().trim().isEmpty()
        val passwordEmpty: Boolean = passwordField.text.toString().trim().isEmpty()

        login_button.isEnabled = !(usernameEmpty || passwordEmpty)

        //clear error messages
        gamecodeLayout.error = null
        inputLayout.error = null
    }

    //function that sends requests
    private fun sendRequest(gamecode: String, password: String) {
        LoginRequest.send(
            this.applicationContext,
            gamecode,
            password,
            fun(jsonObject: JSONObject, success: Boolean) {
                //handle errors
                if (!success) {
                    if (jsonObject.has("error")) {
                        //auth error - throw error
                        inputLayout.error = getString(R.string.error_auth)
                    }
                    //enable button
                    login_button.isEnabled = true
                    return
                }
                //set game to shared preferences
                val game = Game(gamecode, password, jsonObject.get("auth_token").toString())
                dataHandler.setGame(game)

                //launch main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            })
    }

}