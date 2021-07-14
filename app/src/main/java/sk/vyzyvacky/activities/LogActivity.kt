package sk.vyzyvacky.activities

import android.widget.TableLayout
import android.widget.Toast
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.DataHandler

class LogActivity : TableActivity() {

    override fun init() {
        val table = findViewById<TableLayout>(R.id.table_main)

        val header = arrayOf("ID", "Čas", "Víťaz", "Porazený")
        setHeader(header)

        val dataHandler = DataHandler(this.applicationContext)
        val log = dataHandler.getEntries()

        //toast
        val logSize = log.size
        if (logSize == 0) Toast.makeText(
            this,
            this.resources.getString(R.string.no_entries),
            Toast.LENGTH_LONG
        ).show()

        //print entries
        for (i in 0 until logSize) {
            val current = log.get(i)

            //get strings
            val strings = arrayOfNulls<String>(4)
            strings[0] = i.toString()
            strings[1] = current.time
            strings[2] = current.winner
            strings[3] = current.looser
            //add new row into table
            newRow(table, strings, i)
        }
    }
}