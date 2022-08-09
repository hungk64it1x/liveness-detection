package com.vndc.faces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ShowCaptureActivity extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);
        imageView = findViewById(R.id.imgCapture);

        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra("imageUri"));
        Bitmap bitmap;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
        DeleteImage(imageUri);



    }

    private void DeleteImage(Uri imagePath) {
        getContentResolver().delete(imagePath, null, null);
    }
}