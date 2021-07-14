package sk.vyzyvacky.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sk.vyzyvacky.R

abstract class TableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_table)
        init()
    }

    protected abstract fun init()

    protected fun newRow(table: TableLayout, strings: Array<String?>, i: Int) {
        //new table row
        val inflater = (this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val newRow = inflater.inflate(R.layout.table_row_template, null) as TableRow
        newRow.gravity = Gravity.CENTER

        //set color
        if (i % 2 == 0) newRow.setBackgroundColor(Color.WHITE) else newRow.setBackgroundColor(Color.LTGRAY)

        //print to text views
        for (j in 0 until newRow.childCount) {
            val view = newRow.getChildAt(j)
            if (view is TextView) {
                view.text = strings[j]
            }
        }
        //add row
        table.addView(newRow)
    }

    protected fun setHeader(header: Array<String>) {
        val layout = findViewById<TableRow>(R.id.header_row)
        for (i in 0 until layout.childCount) {
            val view = layout.getChildAt(i)
            if (view is TextView) {
                view.text = header[i]
            }
        }
    }
}