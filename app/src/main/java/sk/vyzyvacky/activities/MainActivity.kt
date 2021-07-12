package sk.vyzyvacky.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.vyzyvacky.R;
import sk.vyzyvacky.data.Data;
import sk.vyzyvacky.model.LogEntry;
import sk.vyzyvacky.model.Model;
import sk.vyzyvacky.model.SKArrayAdapter;
import sk.vyzyvacky.xml.XMLExport;
import sk.vyzyvacky.xml.XMLImport;

public class MainActivity extends AppCompatActivity {
    private Data database;
    private XMLImport importXML;
    private XMLExport exportXML;

    private AutoCompleteTextView mTextInput1;
    private AutoCompleteTextView mTextInput2;
    private Button button;
    private Toolbar mToolbar;

    private boolean imported;
    private boolean isImporting;
    private boolean isViewing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUiViews();

        database = new Data();
        importXML = new XMLImport(this, this, database);
        exportXML = new XMLExport(this, this, database);

        imported = false;
        isImporting = false;
        isViewing = false;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //launch importer
        importXML.execute();
    }

    @Override
    protected void onDestroy() {
        if (!isImporting || !isViewing)
            exportXML.execute();
        super.onDestroy();
    }

    private void initUiViews() {
        mTextInput1 = findViewById(R.id.autoCompleteTextView1);
        mTextInput2 = findViewById(R.id.autoCompleteTextView2);
        button = findViewById(R.id.submit_button);

        String[] nameList = {};
        // Create the adapter and set it to the AutoCompleteTextViews
        SKArrayAdapter<String> adapter = new SKArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);
        mTextInput1.setAdapter(adapter);
        mTextInput2.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            assert filePath != null;
            isImporting = false;
            importXML.ParseXML(filePath);
        }
    }

    public void resetAdapter() {
        imported = true;

        // Get the string array
        String[] nameList = database.getNamesForAdapter();

        // Create the adapter and set it to the AutoCompleteTextViews
        SKArrayAdapter<String> adapter = new SKArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);
        mTextInput1.setAdapter(adapter);
        mTextInput2.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.options) {
            settings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void settings() {
        View view = (View) findViewById(R.id.options);
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        //listen for click
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Import":
                    isImporting = true;
                    importXML.execute();
                    break;
                case "Export":
                    exportXML.execute();
                    break;
                case "Zobraz databázu":
                    isViewing = true;
                    showDatabase();
                    isViewing = false;
                    break;
                case "Zobraz log":
                    isViewing = true;
                    showLog();
                    isViewing = false;
                    break;
                case "Zmaž posledný":
                    deleteLast();
                    break;
                default:
                    return true;
            }
            return true;
        });
        popup.show();
    }

    void showDatabase() {
        Intent databaseIntent = new Intent(MainActivity.this, DatabaseActivity.class);
        databaseIntent.putExtra("data", database);
        startActivity(databaseIntent);
    }

    void showLog() {
        Intent logIntent = new Intent(MainActivity.this, LogActivity.class);
        logIntent.putExtra("data", database);
        startActivity(logIntent);
    }

    void deleteLast() {
        //get last log index
        int index = database.getLog().size() - 1;
        if (index < 0) {
            Toast.makeText(this, "No logs to delete.", Toast.LENGTH_LONG).show();
            return;
        }

        //get Strings
        LogEntry last = database.getLog().get(index);

        String time = last.getTime();
        String winner = database.getFullParticipantNameByID(last.getWinner());
        String looser = database.getFullParticipantNameByID(last.getLooser());

        Context context = this;

        //popup
        new AlertDialog.Builder(context)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?\n" + time + " " + winner + " vs. " + looser)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Continue with delete operation
                    database.getLog().remove(index);
                    Toast.makeText(context, "Last log entry removed successfully.", Toast.LENGTH_LONG).show();
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClickBtn(View v) {
        if (!imported) {
            Toast.makeText(this, this.getResources().getString(R.string.not_imported), Toast.LENGTH_LONG).show();
            return;
        }

        //get strings
        String time = formatMillisecondsTime(System.currentTimeMillis());
        String winner = mTextInput1.getText().toString();
        String looser = mTextInput2.getText().toString();

        //reset values of inputs
        mTextInput1.setText("");
        mTextInput2.setText("");

        //test the input
        String winnerID = null, looserID = null;
        for (int i = 0; i < database.getParticipants().size(); i++) {
            Model current = database.getParticipants().get(i);
            String currentName = current.getLastname() + " " + current.getFirstname();

            if (winner.equals(currentName)) {
                //found winner
                winnerID = current.getId();
            }
            if (looser.equals(currentName)) {
                //found looser
                looserID = current.getId();
            }
        }

        if (winnerID == null || looserID == null) {
            //invalid entry, abort
            Toast.makeText(this, this.getResources().getString(R.string.entry_failed), Toast.LENGTH_LONG).show();
            return;
        }

        //log an entry
        database.addEntry(new LogEntry(time, winnerID, looserID));
        Toast.makeText(this, this.getResources().getString(R.string.entry_added), Toast.LENGTH_LONG).show();

        //change focus
        mTextInput1.setFocusableInTouchMode(true);
        mTextInput1.requestFocus();
    }

    private String formatMillisecondsTime(long millis) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(millis);
        return simpleDateFormat.format(date);
    }
}
