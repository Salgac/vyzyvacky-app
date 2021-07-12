package sk.vyzyvacky.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import sk.vyzyvacky.model.LogEntry;
import sk.vyzyvacky.model.Model;

public class Data implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };
    private final ArrayList<LogEntry> log;
    private final ArrayList<Model> participants;

    public Data() {
        log = new ArrayList<>();
        participants = new ArrayList<>();
    }

    protected Data(Parcel in) {
        if (in.readByte() == 0x01) {
            log = new ArrayList<LogEntry>();
            in.readList(log, LogEntry.class.getClassLoader());
        } else {
            log = null;
        }
        if (in.readByte() == 0x01) {
            participants = new ArrayList<Model>();
            in.readList(participants, Model.class.getClassLoader());
        } else {
            participants = null;
        }
    }

    public void addEntry(LogEntry entry) {
        log.add(entry);
    }

    public ArrayList<Model> getParticipants() {
        return participants;
    }

    public String getFullParticipantNameByID(String id) {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).getId().equals(id))
                return participants.get(i).getLastname() + " " + participants.get(i).getFirstname();
        }
        return null;
    }

    public String[] getNamesForAdapter() {
        String[] names = new String[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            names[i] = participants.get(i).getLastname() + " " + participants.get(i).getFirstname();
        }
        return names;
    }

    public ArrayList<LogEntry> getLog() {
        return log;
    }

    public void resetLog() {
        log.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (log == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(log);
        }
        if (participants == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(participants);
        }
    }
}