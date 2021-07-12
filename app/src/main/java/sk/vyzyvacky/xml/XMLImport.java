package sk.vyzyvacky.xml;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nbsp.materialfilepicker.MaterialFilePicker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import sk.vyzyvacky.R;
import sk.vyzyvacky.activities.MainActivity;
import sk.vyzyvacky.data.Data;
import sk.vyzyvacky.model.Model;

public class XMLImport extends Fragment {

    private final Context context;
    private final MainActivity activity;
    private final Data database;


    public XMLImport(Context context, MainActivity activity, Data database) {
        this.context = context;
        this.activity = activity;
        this.database = database;
    }

    public void execute() {
        // first check for runtime permission
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int grant = ContextCompat.checkSelfPermission(context, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions((Activity) context, permission_list, 1);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.file_pick), Toast.LENGTH_LONG).show();
            pickFile();
        }

        //hardcoded
        ParseXML("sk/vyzyvacky/xml/database2020.xml");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "READ_EXTERNAL_STORAGE permission granted", Toast.LENGTH_SHORT).show();
                pickFile();
            } else {
                Toast.makeText(context, " READ_EXTERNAL_STORAGE permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void pickFile() {
        new MaterialFilePicker()
                .withActivity((Activity) context)
                .withRequestCode(1)
                .withFilterDirectories(true)
                .withHiddenFiles(true)
                .start();
    }

    public void ParseXML(String path) {
        File yourFile = new File(path);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            //reset database
            database.getParticipants().clear();

            //read file
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(yourFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("person");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                //get all the required data
                Model model = new Model();
                model.setId(element.getAttribute("id"));
                model.setFirstname(element.getElementsByTagName("firstname").item(0).getTextContent());
                model.setLastname(element.getElementsByTagName("lastname").item(0).getTextContent());
                model.setAge(element.getElementsByTagName("age").item(0).getTextContent());

                //add it to database
                database.getParticipants().add(model);
            }

            Toast.makeText(context, context.getResources().getString(R.string.import_ok), Toast.LENGTH_LONG).show();
            activity.resetAdapter();

        } catch (SAXException | ParserConfigurationException | IOException e1) {
            Toast.makeText(context, context.getResources().getString(R.string.import_ko), Toast.LENGTH_LONG).show();
            e1.printStackTrace();
        }
    }
}
