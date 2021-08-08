package sk.vyzyvacky.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.android.volley.Request
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.DataHandler
import sk.vyzyvacky.utilities.HttpRequestManager
import sk.vyzyvacky.utilities.RequestType

class LoginActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler

    private val GAME_CODE_LENGTH: Int = 1 + 5
    private val MIN_PASSWORD_LENGTH: Int = 4

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
        val jsonObj = JSONObject()
        jsonObj.put("code", gamecode)
        jsonObj.put("password", password)

        //send request and get response
        HttpRequestManager.sendObjectRequest(this, jsonObj,
            RequestType.LOGIN, Request.Method.POST,
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
                //set token and preferences
                dataHandler.setToken(jsonObject.get("auth_token").toString())

                //launch main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            })
    }

}