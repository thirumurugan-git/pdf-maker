package com.example.pdf_project;

import com.example.pdf_project.Action;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.example.pdf_project.DBHelper;

import uk.co.senab.photoview.PhotoViewAttacher;

import static androidx.core.content.FileProvider.getUriForFile;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    NumberPicker numberPicker;
    OutputStream outputStream;
    File photoFile;
    ImageView imageView;
    PhotoViewAttacher photoViewAttacher;
    Action act=new Action();
    ArrayList<String> path=new ArrayList<String>();
    int i=0;
    static final int REQUEST_IMAGE_CAPTURE = 25;
    TextView pg,filename;
    Bitmap bmp;
    String action;
    String[] projection = {MediaStore.Images.Media.DATA};
    private String dir= Environment.getExternalStorageDirectory().getPath()+"/Images/";
    Intent intent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        numberPicker=(NumberPicker)findViewById(R.id.numpicker);
        imageView=(ImageView)findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.image);
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d",i);
            }
        });
        pg=(TextView)findViewById(R.id.page);
        filename=(TextView)findViewById(R.id.filename);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int start, int i1) {
                pg.setText(String.valueOf(i1));
            }
        });
    }

    public void click(View view) {

        int permission= ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission== PackageManager.PERMISSION_GRANTED){
            myFun();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }


    }
    //action + button
    private void myFun() {
            Intent intent = new Intent("android.intent.action.MULTIPLE_PICK");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            PackageManager manager = getApplicationContext().getPackageManager();
            List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
            if (infos.size() > 0) {
                action = "android.intent.action.MULTIPLE_PICK";
            } else {
                action = Intent.ACTION_GET_CONTENT;
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(intent, 1);

    }
    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK&&requestCode==1) {
            if (action.equals("android.intent.action.MULTIPLE_PICK")) {
                final Bundle extras = data.getExtras();
                int count = extras.getInt("selectedCount");
                Object items = extras.getStringArrayList("selectedItems");
            } else {
                if (data != null && data.getData() != null) {

                    Uri selectedImage = data.getData();
                    path.add(act.getRealPathFromURI(this, selectedImage));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clipData = data.getClipData();
                        path = act.manyPath(clipData, path, this);
                    }
                }
                photoViewAttacher = new PhotoViewAttacher(imageView);
                i=path.size()-1;
                afterSelection();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode==RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();
                path.remove(i);
                path.add(i,uri.getPath());
                afterSelection();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_TAKE_PHOTO ) {
            if(resultCode==RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Uri tempUri = act.getImageUri(getApplicationContext(), imageBitmap);
            path.add(act.getPathFromURI(getContentResolver(),tempUri));*/
                camera_taken(photoFile.getAbsolutePath());
            }else{
                if(photoFile.exists()){
                    photoFile.delete();
                }
            }
        }
        if(requestCode==REQUEST_TAKE_PHOTO&&requestCode==RESULT_OK){
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }
    }

    private void camera_taken(String location) {
        Toast.makeText(this, "Camera picture is added", Toast.LENGTH_SHORT).show();
        path.add(location);
        i=path.size()-1;
        photoViewAttacher = new PhotoViewAttacher(imageView);
        afterSelection();
    }

    //action for download button on menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //process your onClick here
            Intent download=new Intent(this,Downloads.class);
            startActivity(download);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //action for left arrow and roght arrow
    private void afterSelection() {
        bmp=BitmapFactory.decodeFile(path.get(i));
        imageView.setImageBitmap(bmp);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(path.size());
        numberPicker.setValue(i+1);
        String fn=path.get(i).substring(path.get(i).lastIndexOf("/")+1);
        filename.setText(fn);
        pg.setText(String.valueOf(i+1));
        photoViewAttacher.update();
    }
    public void right(View view) {
        int page_no = Integer.parseInt(pg.getText().toString());
        if (page_no == (i + 1)) {
            if (i < (path.size() - 1)) {
                i++;
                afterSelection();
            } else {
                Toast.makeText(this, "end of the image", Toast.LENGTH_SHORT).show();
            }
        } else {
            path = act.move(path, i, page_no-1);
            afterSelection();
        }
    }


    public void left(View view) {
        int page_no=Integer.parseInt(pg.getText().toString());
        if(page_no==(i+1)) {
            if (i>0) {
                i--;
                afterSelection();
            } else {
                Toast.makeText(this, "end of the image", Toast.LENGTH_SHORT).show();
            }
        }else{
            path=act.move(path,i,page_no-1);
            afterSelection();
        }
    }

    public void minus(View view) {
        if(path.size()==1){
            path.clear();
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }else if(path.size()>0){
            path.remove(i);
            if (i == path.size()) {
                i--;
            }
            afterSelection();
        }
    }
    //action for crob
    public void crob_image(View view) {
        if(path.size()>0) {
            startCropImageActivity(Uri.fromFile(new File(path.get(i))));
        }
    }
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    public void camera(View view) {
        int permission1= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if(permission1== PackageManager.PERMISSION_GRANTED){
            //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            dispatchTakePictureIntent();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},0);
        }
    }
    public final int REQUEST_TAKE_PHOTO=44;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            String fl_nm=System.currentTimeMillis()+".jpg";
            photoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),fl_nm);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pdf_project",
                        photoFile);
                this.grantUriPermission(this.getPackageName(),photoURI,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void thump_up(View view) throws InterruptedException {
        int permission=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission==PackageManager.PERMISSION_GRANTED){
            if(path.size()>0) {
                Thread.sleep(1000);
                String path_of_file = act.get_path_for_file(path);
                DBHelper dbHelper = new DBHelper(this);
                if (dbHelper.insert(path_of_file)) {
                    Toast.makeText(this, "file saved in:" + path_of_file, Toast.LENGTH_SHORT).show();
                    openPdf(path_of_file);
                } else {
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "no files are selected", Toast.LENGTH_SHORT).show();
            }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

    }

    private void openPdf(String file) {
        int permission= ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission== PackageManager.PERMISSION_GRANTED){
            try {
                File fil_path=new File(file);
                final Uri data = FileProvider.getUriForFile(getApplicationContext(), "com.example.pdf_project", fil_path);
                this.grantUriPermission(getApplicationContext().getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(data, "application/pdf")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "No Application available to view pdf", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
    }
}