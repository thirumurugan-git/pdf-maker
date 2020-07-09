package com.example.pdf_project;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import androidx.core.content.FileProvider;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Action extends FileProvider{
    public static String getRealPathFromURI(Context context, Uri uri)
    {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String image_id = cursor.getString(0);
        image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
        cursor.close();
        cursor = context.getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        return path;
    }
    public ArrayList<String> move(ArrayList<String> arrayList,int i,int e)    {
        String string=arrayList.get(i);
        arrayList.remove(i);
        arrayList.add(e,string);
        return arrayList;
    }
    public ArrayList<String> manyPath(ClipData clipData,ArrayList<String> path,Context context){
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                path.add(getRealPathFromURI(context,uri));
            }
        }
        return path;
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getPathFromURI(ContentResolver contentResolver, Uri uri) {
        String path = "";
        if (contentResolver != null) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }
    public String folder_storage(){
        File file=new File(Environment.getExternalStorageDirectory().getPath()+"/pdf project/");
        if(!file.exists()){
            file.mkdir();
        }
        return file.getPath();
    }
    public String get_path_for_file(ArrayList<String> path){
        String file_path=null;
        try {
            PDDocument document = new PDDocument();
            for(int i=0;i<path.size();i++) {
                Bitmap bimap = BitmapFactory.decodeFile(path.get(i));
                int wid = bimap.getWidth();
                int hei = bimap.getHeight();
                while (PDRectangle.A4.getWidth() <= wid || PDRectangle.A4.getHeight() <= hei) {
                    wid = (int) (wid * 0.9);
                    hei = (int) (hei * 0.9);
                }
                int startx = (int) ((PDRectangle.A4.getWidth() - wid) / 2);
                int starty = (int) ((PDRectangle.A4.getHeight() - hei) / 2);
                PDPage page = new PDPage();
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                PDImageXObject ximage = JPEGFactory.createFromImage(document, bimap, (float) 0.75, bimap.getDensity());
                contentStream.drawImage(ximage, startx, starty, wid, hei);
                contentStream.close();
            }
            file_path = folder_storage() + "/" + System.currentTimeMillis() + ".pdf";
            document.save(file_path);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file_path;
    }
     /*File temp=Environment.getExternalStorageDirectory();
            File dir=new File(temp.getAbsolutePath()+"/Demo/");
            dir.mkdir();
            File new_file=new File(dir,System.currentTimeMillis()+".jpg");
            try {
                outputStream=new FileOutputStream(new_file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            path.add(new_file.getAbsolutePath());*/
}
