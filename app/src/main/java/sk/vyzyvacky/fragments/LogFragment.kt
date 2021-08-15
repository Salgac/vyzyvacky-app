package sk.vyzyvacky.fragments

import sk.vyzyvacky.R
import sk.vyzyvacky.model.LogEntry
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class LogFragment : TableFragment() {

    private var listSize: Int = 0

    override fun getHeaderStrings(): Array<String?> {
        return resources.getStringArray(R.array.log_header)
    }

    override fun getDataObjects(): MutableList<Any> {
        @Suppress("UNCHECKED_CAST")
        val list = dataHandler.getEntries() as MutableList<Any>
        listSize = list.size
        return list.asReversed()
    }

    override fun getStringsFromObject(i: Int, current: Any): Array<String?> {
        val strings = arrayOfNulls<String>(5)
        val logObject = current as LogEntry

        strings[0] = (listSize - i).toString()
        strings[1] = timeInReadableFormat(logObject.time)
        strings[2] = dataHandler.getFullParticipantNameByID(logObject.winner)
        strings[3] = dataHandler.getFullParticipantNameByID(logObject.looser)
        strings[4] = logObject.sent.toString()
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