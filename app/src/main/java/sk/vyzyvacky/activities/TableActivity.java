package sk.vyzyvacky.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sk.vyzyvacky.R;
import sk.vyzyvacky.data.Data;

public abstract class TableActivity extends AppCompatActivity {

    protected Data database;
    protected String[] header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_table);

        Intent intent = getIntent();
        this.database = intent.getParcelableExtra("data");
        init();
    }

    protected abstract void init();

    protected void newRow(TableLayout table, String[] strings, int i) {
        //new table row
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        TableRow newRow = (TableRow) inflater.inflate(R.layout.table_row_template, null);
        newRow.setGravity(Gravity.CENTER);

        //set color
        if (i % 2 == 0)
            newRow.setBackgroundColor(Color.WHITE);
        else
            newRow.setBackgroundColor(Color.LTGRAY);

        //print to text views
        for (int j = 0; j < newRow.getChildCount(); j++) {
            View view = newRow.getChildAt(j);
            if (view instanceof TextView) {
                ((TextView) view).setText(strings[j]);
            }
        }
        //add row
        table.addView(newRow);
    }

    protected void setHeader() {
        TableRow layout = findViewById(R.id.header_row);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setText(header[i]);
            }
        }
    }

}
