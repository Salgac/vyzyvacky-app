package sk.vyzyvacky.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import org.json.JSONArray
import sk.vyzyvacky.R
import sk.vyzyvacky.data.Data
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.Participant
import sk.vyzyvacky.model.SKArrayAdapter
import sk.vyzyvacky.utilities.DataHandler
import sk.vyzyvacky.utilities.HttpRequestManager
import sk.vyzyvacky.utilities.RequestType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var database: Data = Data()
    private lateinit var dataHandler: DataHandler

    private lateinit var mTextInput1: AutoCompleteTextView
    private lateinit var mTextInput2: AutoCompleteTextView
    private lateinit var button: Button
    private lateinit var mToolbar: Toolbar

    private var imported = false
    private var isViewing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextInput1 = findViewById(R.id.autoCompleteTextView1)
        mTextInput2 = findViewById(R.id.autoCompleteTextView2)
        button = findViewById(R.id.submit_button)

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
    }

    fun resetAdapter() {
        // Get the string array
        val nameList = database.namesForAdapter

        // Create the adapter and set it to the AutoCompleteTextViews
        val adapter = SKArrayAdapter(this, android.R.layout.simple_list_item_1, nameList)
        mTextInput1.setAdapter(adapter)
        mTextInput2.setAdapter(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.options) {
            settings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun settings() {
        val view = findViewById<View>(R.id.options)
        val popup = PopupMenu(this@MainActivity, view)
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

        //listen for click
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.title.toString()) {
                "Import" -> {
                    importDatabase()
                }
                "Export" -> {
                    //TODO
                    Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
                }
                "Zobraz databázu" -> {
                    isViewing = true
                    showDatabase()
                    isViewing = false
                }
                "Zobraz log" -> {
                    isViewing = true
                    showLog()
                    isViewing = false
                }
                "Zmaž posledný" -> deleteLast()
                else -> return@setOnMenuItemClickListener true
            }
            true
        }
        popup.show()
    }

    private fun importDatabase() {
        dataHandler = DataHandler(this.applicationContext)

        HttpRequestManager.sendRequestForArray(
            this, RequestType.PARTICIPANT,
            { response: JSONArray, success: Boolean ->
                if (success) {
                    val participantArr = ArrayList<Participant>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)

                        val firstname = obj.get("firstName") as String
                        val lastname = obj.get("lastName") as String
                        val id = obj.get("id") as Int
                        val team = obj.get("team_id") as Int

                        participantArr.add(Participant(id, firstname, lastname, team))
                    }
                    dataHandler.setParticipants(participantArr)
                } else {
                    System.out.println("Error: $response")
                }
            },
        )

        Toast.makeText(this, "Database imported.", Toast.LENGTH_SHORT).show()
        //resetAdapter()
    }

    private fun showDatabase() {
        val databaseIntent = Intent(this@MainActivity, DatabaseActivity::class.java)
        databaseIntent.putExtra("data", database)
        startActivity(databaseIntent)
    }

    private fun showLog() {
        val logIntent = Intent(this@MainActivity, LogActivity::class.java)
        logIntent.putExtra("data", database)
        startActivity(logIntent)
    }

    private fun deleteLast() {
        //get last log index
        val index = database.log!!.size - 1
        if (index < 0) {
            Toast.makeText(this, "No logs to delete.", Toast.LENGTH_LONG).show()
            return
        }

        //get Strings
        val last = database.log?.get(index)
        val time = last?.time
        val winner = last?.let { database.getFullParticipantNameByID(it.winner) }
        val looser = last?.let { database.getFullParticipantNameByID(it.looser) }
        val context: Context = this

        //popup
        AlertDialog.Builder(context)
            .setTitle("Delete entry")
            .setMessage("Are you sure you want to delete this entry?\n$time $winner vs. $looser")
            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                // Continue with delete operation
                database.log!!.removeAt(index)
                Toast.makeText(context, "Last log entry removed successfully.", Toast.LENGTH_LONG)
                    .show()
            } // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    fun onClickBtn(v: View?) {
        if (!imported) {
            Toast.makeText(this, "Database not imported.", Toast.LENGTH_LONG).show()
            return
        }

        //get strings
        val time = formatMillisecondsTime(System.currentTimeMillis())
        val winner = mTextInput1.text.toString()
        val looser = mTextInput2.text.toString()

        //reset values of inputs
        mTextInput1.setText("")
        mTextInput2.setText("")

        //test the input
        var winnerID: String? = null
        var looserID: String? = null
        for (i in database.participants?.indices!!) {
            val current = database.participants?.get(i)
            val currentName = current?.lastname + " " + current?.firstname
            if (winner == currentName) {
                //found winner
                if (current != null) {
                    winnerID = current.id
                }
            }
            if (looser == currentName) {
                //found looser
                if (current != null) {
                    looserID = current.id
                }
            }
        }
        if (winnerID == null || looserID == null) {
            //invalid entry, abort
            Toast.makeText(this, this.resources.getString(R.string.entry_failed), Toast.LENGTH_LONG)
                .show()
            return
        }

        //log an entry
        database.addEntry(LogEntry(time, winnerID, looserID))
        Toast.makeText(this, this.resources.getString(R.string.entry_added), Toast.LENGTH_LONG)
            .show()

        //change focus
        mTextInput1.isFocusableInTouchMode = true
        mTextInput1.requestFocus()
    }

    private fun formatMillisecondsTime(millis: Long): String {
        @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        val date = Date(millis)
        return simpleDateFormat.format(date)
    }
}