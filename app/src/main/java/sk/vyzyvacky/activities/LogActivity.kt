package sk.vyzyvacky.activities;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.Toast;

import sk.vyzyvacky.R;
import sk.vyzyvacky.model.LogEntry;

public class LogActivity extends TableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        TableLayout table = findViewById(R.id.table_main);

        header = new String[]{"ID", "Čas", "Víťaz", "Porazený"};
        setHeader();
        //toast
        int logSize = database.getLog().size();
        if (logSize == 0)
            Toast.makeText(this, this.getResources().getString(R.string.no_entries), Toast.LENGTH_LONG).show();

        //print entries
        for (int i = 0; i < logSize; i++) {
            LogEntry current = database.getLog().get(i);

            //get strings
            String[] strings = new String[4];
            strings[0] = String.valueOf(i);
            strings[1] = current.getTime();
            strings[2] = database.getFullParticipantNameByID(current.getWinner());
            strings[3] = database.getFullParticipantNameByID(current.getLooser());

            newRow(table, strings, i);
        }
    }
}
