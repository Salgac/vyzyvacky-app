package sk.vyzyvacky.xml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import sk.vyzyvacky.R;
import sk.vyzyvacky.activities.MainActivity;
import sk.vyzyvacky.data.Data;
import sk.vyzyvacky.model.LogEntry;

public class XMLExport {

    private final Context context;
    private final MainActivity activity;
    private final Data database;

    public XMLExport(Context context, MainActivity activity, Data database) {
        this.context = context;
        this.activity = activity;
        this.database = database;
    }

    public void execute() {
        if (database.getLog().size() == 0)
            return;
        try {
            String filename = "log_" + formatMillisecondsTime(System.currentTimeMillis()) + ".xml";
            // Creates a file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            File newFile = new File(context.getExternalFilesDir(null), filename);
            if (!newFile.exists())
                newFile.createNewFile();

            FileOutputStream fOut = new FileOutputStream(newFile);

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fOut, "UTF-8");
            serializer.startDocument(null, Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "log").attribute(null, "model", android.os.Build.MODEL);

            ArrayList<LogEntry> log = database.getLog();

            for (int i = 0; i < log.size(); i++) {
                LogEntry current = log.get(i);

                serializer.startTag(null, "entry");

                serializer.startTag(null, "time");
                serializer.text(current.getTime());
                serializer.endTag(null, "time");

                serializer.startTag(null, "winner");
                serializer.text(current.getWinner());
                serializer.endTag(null, "winner");

                serializer.startTag(null, "looser");
                serializer.text(current.getLooser());
                serializer.endTag(null, "looser");

                serializer.endTag(null, "entry");
            }
            serializer.endTag(null, "log");

            serializer.endDocument();
            serializer.flush();
            fOut.close();

            Toast.makeText(context, context.getResources().getString(R.string.export_ok), Toast.LENGTH_LONG).show();

            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile(context, new String[]{newFile.toString()}, null, null);

            //clear log database
            database.resetLog();

        } catch (IOException e) {
            Toast.makeText(context, context.getResources().getString(R.string.export_ko), Toast.LENGTH_LONG).show();
        }
    }

    private String formatMillisecondsTime(long millis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(millis);
        return simpleDateFormat.format(date).replace(':', '-');
    }
}
