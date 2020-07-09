package com.example.pdf_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pdf_project.DBHelper;

import java.io.File;
import java.util.ArrayList;

public class Downloads extends AppCompatActivity {
    public ListView lst;
    public ArrayList<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        lst=(ListView)findViewById(R.id.simpleListView);
        DBHelper dbHelper=new DBHelper(this);
        list =new ArrayList<>();
        list=dbHelper.getAll();
        ArrayList<String> copy = new ArrayList<String>();
        for(int i=list.size()-1;i>=0;i--){
            String fn=list.get(i).substring(list.get(i).lastIndexOf("/")+1);
            copy.add(fn);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, copy);
        lst.setAdapter(arrayAdapter);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,int i, long l) {
                open_pdf(list.size()-1-i);
            }
        });
    }
    public void open_pdf(int i) {
        File fl = new File(list.get(i));
        if (fl.exists()) {
            final Uri data = FileProvider.getUriForFile(getApplicationContext(), "com.example.pdf_project", fl);
            this.grantUriPermission(getApplicationContext().getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(data, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "file moved or deleted", Toast.LENGTH_SHORT).show();
        }
    }

}
