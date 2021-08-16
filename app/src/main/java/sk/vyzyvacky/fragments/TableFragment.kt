package sk.vyzyvacky.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_table.view.*
import sk.vyzyvacky.R
import sk.vyzyvacky.utilities.data.DataHandler

abstract class TableFragment : Fragment() {
    private var layout: View? = null
    lateinit var dataHandler: DataHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        layout = inflater.inflate(R.layout.fragment_table, container, false)
        dataHandler = DataHandler(this.requireContext())

        setHeader(getHeaderStrings())
        printData(getDataObjects())

        return layout
    }

    protected abstract fun getHeaderStrings(): Array<String?>

    protected abstract fun getDataObjects(): MutableList<Any>

    protected abstract fun getStringsFromObject(i: Int, current: Any): Array<String?>

    protected abstract fun getErrorString(): String

    @SuppressLint("InflateParams")
    protected fun newRow(table: TableLayout, strings: Array<String?>, i: Int) {
        //new table row
        val inflater =
            (this.activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val newRow = inflater.inflate(R.layout.table_row_template, null) as TableRow
        newRow.gravity = Gravity.CENTER

        //set color
        if (i % 2 == 0) newRow.setBackgroundColor(Color.WHITE) else newRow.setBackgroundColor(Color.LTGRAY)

        //print to text views
        for (j in strings.indices) {
            val view = newRow.getChildAt(j)
            if (view is TextView) {
                view.text = strings[j]
            }
        }
        //add row
        table.addView(newRow)
    }

    private fun setHeader(strings: Array<String?>) {
        val layout = layout!!.header_row
        for (i in strings.indices) {
            val view = layout.getChildAt(i)
            if (view is TextView) {
                view.text = strings[i]
            }
        }
    }

    private fun printData(data: MutableList<Any>) {
        val table = layout!!.table_main
        val dataSize = data.size

        //toast
        if (dataSize == 0) Toast.makeText(
            this.requireContext(),
            getErrorString(),
            Toast.LENGTH_SHORT
        ).show()

        //print entries
        for (i in 0 until dataSize) {
            val strings = getStringsFromObject(i, data[i])
            newRow(table, strings, i)
        }
    }
}