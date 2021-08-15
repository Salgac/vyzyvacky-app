package sk.vyzyvacky.fragments

import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class LogFragment : TableFragment() {

    override fun getHeaderStrings(): Array<String?> {
        return resources.getStringArray(R.array.log_header)
    }

    override fun getDataObjects(): ArrayList<Any> {
        @Suppress("UNCHECKED_CAST")
        return dataHandler.getEntries() as ArrayList<Any>
    }

    override fun getStringsFromObject(i: Int, current: Any): Array<String?> {
        val strings = arrayOfNulls<String>(4)
        val logObject = current as LogEntry

        strings[0] = i.toString()
        strings[1] = timeInReadableFormat(logObject.time)
        strings[2] = dataHandler.getFullParticipantNameByID(logObject.winner)
        strings[3] = dataHandler.getFullParticipantNameByID(logObject.looser)
        return strings
    }

    override fun getErrorString(): String {
        return resources.getString(R.string.log_error)
    }

    private fun timeInReadableFormat(time: String): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = Timestamp.valueOf(time).time
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}