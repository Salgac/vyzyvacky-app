package sk.vyzyvacky.data

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import sk.vyzyvacky.model.LogEntry
import sk.vyzyvacky.model.Model
import java.util.*

class Data : Parcelable {
    val log: ArrayList<LogEntry?>?
    val participants: ArrayList<Model?>?

    constructor() {
        log = ArrayList()
        participants = ArrayList()
    }

    protected constructor(`in`: Parcel) {
        if (`in`.readByte().toInt() == 0x01) {
            log = ArrayList()
            `in`.readList(log, LogEntry::class.java.classLoader)
        } else {
            log = null
        }
        if (`in`.readByte().toInt() == 0x01) {
            participants = ArrayList()
            `in`.readList(participants, Model::class.java.classLoader)
        } else {
            participants = null
        }
    }

    fun addEntry(entry: LogEntry?) {
        log!!.add(entry)
    }

    fun getFullParticipantNameByID(id: String): String? {
        for (i in participants!!.indices) {
            if (participants[i]!!.id == id) return participants[i]!!.lastname + " " + participants[i]!!.firstname
        }
        return null
    }

    val namesForAdapter: Array<String?>
        get() {
            val names = arrayOfNulls<String>(participants!!.size)
            for (i in participants.indices) {
                names[i] = participants[i]!!.lastname + " " + participants[i]!!.firstname
            }
            return names
        }

    fun resetLog() {
        log!!.clear()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (log == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeList(log)
        }
        if (participants == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeList(participants)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Data?> = object : Creator<Data?> {
            override fun createFromParcel(`in`: Parcel): Data? {
                return Data(`in`)
            }

            override fun newArray(size: Int): Array<Data?> {
                return arrayOfNulls(size)
            }
        }
    }
}