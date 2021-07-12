package sk.vyzyvacky.activities

import android.os.Build
import android.widget.TableLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import sk.vyzyvacky.R

class DatabaseActivity : TableActivity() {

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun init() {
        val table = findViewById<TableLayout>(R.id.table_main)
        header = arrayOf("ID", "Meno", "Priezvisko", "Vek")
        setHeader()
        val databaseSize = database?.participants?.size
        if (databaseSize == 0) Toast.makeText(this, this.resources.getString(R.string.no_database), Toast.LENGTH_LONG).show()

        //sort database based on ID
        val list = database?.participants
        //list.sort(Comparator.comparing { obj: Model -> obj.id })

        //print entries
        for (i in 0 until databaseSize!!) {
            val current = database?.participants?.get(i)

            //get strings
            val strings = arrayOfNulls<String>(4)
            if (current != null) {
                strings[0] = current.id
                strings[1] = current.firstname
                strings[2] = current.lastname
                strings[3] = current.age
            }
            //add new row into table
            newRow(table, strings, i)
        }
    }
}