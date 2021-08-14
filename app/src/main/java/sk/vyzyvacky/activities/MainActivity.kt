package sk.vyzyvacky.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.android.synthetic.main.fragment_match.*
import kotlinx.android.synthetic.main.titlebar_main.*
import kotlinx.android.synthetic.main.titlebar_main.view.*
import sk.vyzyvacky.R
import sk.vyzyvacky.fragments.MatchFragment
import sk.vyzyvacky.utilities.ConnectionType
import sk.vyzyvacky.utilities.DataHandler
import sk.vyzyvacky.utilities.NetworkUtil
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var dataHandler: DataHandler
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerHeader: View
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //toolbar
        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //utilities
        dataHandler = DataHandler(this.applicationContext)

        //drawer
        setupDrawer()

        //set fragment
        setNewFragment(MatchFragment())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection in toolbar
        when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setNewFragment(fragment: Fragment) {
        currentFragment = fragment

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun setupDrawer() {
        drawerToggle = object : ActionBarDrawerToggle(this,
            drawer_layout,
            mainToolbar,
            R.string.drawer_open,
            R.string.drawer_close) {
            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (drawer_layout.isDrawerOpen(Gravity.LEFT)) {
                        //closing
                    } else {
                        //opening
                        updateConnectionStatus()
                    }
                }
            }
        }
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        drawer_layout.addDrawerListener(drawerToggle)

        //nav menu setup
        nvView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

        logout_button.setOnClickListener {
            logout()
        }

        //insert data into header
        val game = dataHandler.getGame()!!
        drawerHeader = nvView.getHeaderView(0)

        val codeTextView = drawerHeader.findViewById<TextView>(R.id.headerCode)
        val passwordTextView = drawerHeader.findViewById<TextView>(R.id.headerPassword)

        codeTextView.text = game.code
        passwordTextView.setOnClickListener {
            passwordTextView.text = game.password
            Handler(Looper.getMainLooper()).postDelayed({
                passwordTextView.text = getString(R.string.drawer_pass_placeholder)
            }, 3000)
        }
    }

    private fun selectDrawerItem(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_view_matchmaking -> setNewFragment(MatchFragment())
            R.id.nav_view_database -> showDatabase()
            R.id.nav_view_log -> showLog()
            R.id.nav_import -> importDatabase()
            R.id.nav_export -> exportLogEntries()
            R.id.nav_qr_code -> showQrCode()
            R.id.nav_settings -> showSettings()
            R.id.nav_about -> showAbout()
        }
        drawer_layout.closeDrawers()
    }

    private fun importDatabase() {
        dataHandler.importParticipants()

        if (currentFragment is MatchFragment)
            (currentFragment as MatchFragment).resetAdapter()
    }

    private fun exportLogEntries() {
        dataHandler.exportEntries()
    }

    private fun showDatabase() {
        val databaseIntent = Intent(this@MainActivity, DatabaseActivity::class.java)
        startActivity(databaseIntent)
    }

    private fun showLog() {
        val logIntent = Intent(this@MainActivity, LogActivity::class.java)
        startActivity(logIntent)
    }

    private fun showQrCode() {
        val qrIntent = Intent(this@MainActivity, QrCodeActivity::class.java)
        startActivity(qrIntent)
    }

    private fun showSettings() {
        TODO()
    }

    private fun showAbout() {
        TODO()
    }

    private fun updateConnectionStatus() {
        if (NetworkUtil.getConnectivityStatus(this) != ConnectionType.NOT_CONNECTED) {
            connectionBar.text = getString(R.string.drawer_connection_ok)
            connectionBar.setBackgroundColor(Color.GREEN)
        } else {
            connectionBar.text = getString(R.string.drawer_connection_ko)
            connectionBar.setBackgroundColor(Color.RED)
        }
    }

    private fun logout() {
        dataHandler.removeGame()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}