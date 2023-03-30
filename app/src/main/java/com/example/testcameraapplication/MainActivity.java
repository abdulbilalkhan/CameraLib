package com.example.testcameraapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraListners {


    ImageView capturedImage;
    static final int REQUEST_GALLERY = 101;
    Button iv_multi_img_default;
    //camera class
    CameraActivity cameraActivity;
    String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraActivity = CameraActivity.newInstance(MainActivity.this, this, CropImageView.CropShape.RECTANGLE);
        iv_multi_img_default = findViewById(R.id.buttonUpload);
        capturedImage = findViewById(R.id.iv_multi_img_default);
        iv_multi_img_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

    }

    public boolean checkGalleryPermission() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_GALLERY);
            return false;
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cameraActivity.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            for (int i = 0, len = permissions.length; i < len; i++)

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i])) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{permissions[i]}, REQUEST_GALLERY);
                    Log.e("denied", permissions[i]);

                } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    uploadImage();
                    break;

                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

    }

    private void uploadImage() {
        if (checkGalleryPermission()) {
            cameraActivity.openCamera();
        }

    }


    private Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    String bitmapinsuranceimg;

    @Override
    public void getImage(String baseImg, Bitmap bitmapImg) {
        bitmapinsuranceimg = baseImg;

        if (bitmapinsuranceimg != null || !bitmapinsuranceimg.isEmpty()) {
         capturedImage.setImageBitmap(bitmapImg);
            Log.e("Base64Image", baseImg);
        }


    }


}