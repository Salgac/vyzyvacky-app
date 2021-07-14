package sk.vyzyvacky.activities

import android.os.Build
import android.widget.TableLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.DataHandler

class DatabaseActivity : TableActivity() {

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun init() {
        val table = findViewById<TableLayout>(R.id.table_main)
        header = arrayOf("ID", "Meno", "Priezvisko", "Team")
        setHeader()

        val dataHandler = DataHandler(this.applicationContext)
        val data = dataHandler.getParticipants()

        val dataSize = data.size
        if (dataSize == 0) Toast.makeText(
            this,
            this.resources.getString(R.string.no_database),
            Toast.LENGTH_LONG
        ).show()

        //print entries
        for (i in 0 until dataSize) {
            val current = data[i]

            //get strings
            val strings = arrayOfNulls<String>(4)
            strings[0] = current.id.toString()
            strings[1] = current.firstname
            strings[2] = current.lastname
            strings[3] = current.team.toString()
            //add new row into table
            newRow(table, strings, i)
        }
    }
}