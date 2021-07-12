package sk.vyzyvacky.activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Comparator;

import sk.vyzyvacky.R;
import sk.vyzyvacky.model.Model;

public class DatabaseActivity extends TableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void init() {
        TableLayout table = findViewById(R.id.table_main);

        header = new String[]{"ID", "Meno", "Priezvisko", "Vek"};
        setHeader();

        int databaseSize = database.getParticipants().size();
        if (databaseSize == 0)
            Toast.makeText(this, this.getResources().getString(R.string.no_database), Toast.LENGTH_LONG).show();

        //sort database based on ID
        ArrayList<Model> list = database.getParticipants();
        list.sort(Comparator.comparing(Model::getId));

        //print entries
        for (int i = 0; i < databaseSize; i++) {
            Model current = database.getParticipants().get(i);

            //get strings
            String[] strings = new String[4];
            strings[0] = current.getId();
            strings[1] = current.getFirstname();
            strings[2] = current.getLastname();
            strings[3] = current.getAge();

            //add new row into table
            newRow(table, strings, i);
        }
    }
}
