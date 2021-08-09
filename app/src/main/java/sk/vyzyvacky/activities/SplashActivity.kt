package sk.vyzyvacky.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.DataHandler
import sk.vyzyvacky.utilities.LoginRequest

class SplashActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler

    private val SPLASH_TIME_OUT: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //try logging in
        dataHandler = DataHandler(this.applicationContext)
        val game = dataHandler.getGame()
        if (game != null) {
            //log in
            LoginRequest.send(this.applicationContext, game.code, game.password,
                fun(jsonObject: JSONObject, success: Boolean) {
                    //set status messages
                    splashMessage.text = getString(R.string.splash_logging)
                    if (success) {
                        dataHandler.setToken(jsonObject.get("auth_token").toString())
                    } else {
                        Toast.makeText(this, getString(R.string.error_server), Toast.LENGTH_SHORT)
                            .show()
                    }
                    //launch next activity
                    launchNext(
                        Intent(this, MainActivity::class.java)
                    )
                })
        } else {
            splashMessage.text = getString(R.string.splash_loading)
            launchNext(Intent(this, LoginActivity::class.java))
        }
    }

    private fun launchNext(intent: Intent) {
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}