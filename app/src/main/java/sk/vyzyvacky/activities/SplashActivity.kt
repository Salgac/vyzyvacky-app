package sk.vyzyvacky.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
                    //launch next activity
                    val newIntent = if (success) {
                        dataHandler.setToken(jsonObject.get("auth_token").toString())
                        splashMessage.text = getString(R.string.splash_logging)
                        Intent(this, MainActivity::class.java)
                    } else {
                        splashMessage.text = getString(R.string.splash_loading)
                        Intent(this, LoginActivity::class.java)
                    }

                    Handler().postDelayed({
                        startActivity(newIntent)
                        finish()
                    }, SPLASH_TIME_OUT)
                })
        }
    }
}