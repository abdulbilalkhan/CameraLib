package com.appic.kyc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewMainActivity extends AppCompatActivity {


    private SurfaceView mSurfaceView;
    private TextView txtView;
    private CameraSource mCameraSource;
    private TextRecognizer mTextRecognizer;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        mSurfaceView = findViewById(R.id.surface_view);
        txtView = findViewById(R.id.txtview);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startTextRecognizer();
        } else {
            askCameraPermission();
        }

    }


    private void startTextRecognizer() {
        mTextRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!mTextRecognizer.isOperational()) {
            Toast.makeText(getApplicationContext(), "Oops ! Not able to start the text recognizer ...", Toast.LENGTH_LONG).show();
        } else {
            mCameraSource = new CameraSource.Builder(getApplicationContext(), mTextRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            mCameraSource.start(mSurfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        askCameraPermission();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    txtView.setText("");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                    mTextRecognizer.release();
                }
            });

//            mTextRecognizer.setProcessor(new OcrDetectorProcessor());

            mTextRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    SparseArray<TextBlock> items = detections.getDetectedItems();

                    String PanCardNumber = "", DObString = "", AdhaarNumber = "", DLNumber = "";
                    if (items.size() != 0) {

                        for (int i = 0; i < items.size(); ++i) {
                            TextBlock item = items.valueAt(i);
                            Log.e("itemData", item.getValue());
                            String strsingle = item.getValue().replace("/n", " ");
                            Pattern patternPan = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
                            Pattern patternAdhar = Pattern.compile("\\s[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}");
                            Pattern patternDOB = Pattern.compile("[0-9]{2}/[0-9]{2}/[0-9]{4}");
                            Pattern patternDL = Pattern.compile("(([A-Z]{2}[0-9]{2})( )|([A-Z]{2}-[0-9]{2}))((19|20)[0-9][0-9])[0-9]{7}");
                            Matcher matcherPan = patternPan.matcher(strsingle);
                            if (matcherPan.find()) {
                                PanCardNumber = matcherPan.group();
                                Log.e("PanCard", " " + PanCardNumber);

                                performCaptureAction();
                                mTextRecognizer.release();
                            }

                            Matcher matcherDOB = patternDOB.matcher(strsingle);

                            if (matcherDOB.find()) {
                                DObString = matcherDOB.group();
                                Log.e("DOB", " " + DObString);
                            }

                            Matcher matcherAdhar = patternAdhar.matcher(strsingle);

                            if (matcherAdhar.find()) {
                                AdhaarNumber = matcherAdhar.group();
                                Log.e("AdhaarNumber", " " + AdhaarNumber.trim());
                            }
                            Matcher matcherDL = patternDL.matcher(strsingle);

                            if (matcherDL.find()) {
                                DLNumber = matcherDL.group();
                                Log.e("DLNumber", " " + DLNumber.trim());
                            }

                        }
                    }
                }
            });
        }
    }

    private void performCaptureAction() {

        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            private File imageFile;
            @Override
            public void onPictureTaken(@NonNull byte[] bytes) {
                try {

                    Bitmap loadedImage = null;
                    Bitmap rotatedBitmap = null;
                    loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length);

                    // rotate Image
                    Matrix rotateMatrix = new Matrix();
                    rotateMatrix.postRotate(90);
                    rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                            loadedImage.getWidth(), loadedImage.getHeight(),
                            rotateMatrix, false);
/*                    String state = Environment.getExternalStorageState();
                    File folder = null;
                    if (state.contains(Environment.MEDIA_MOUNTED)) {
                        folder = new File(Environment
                                .getExternalStorageDirectory() + "/Demo");
                    } else {
                        folder = new File(Environment
                                .getExternalStorageDirectory() + "/Demo");
                    }*/
                    getBase64String(rotatedBitmap);
/*

                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        java.util.Date date = new java.util.Date();
                        imageFile = new File(folder.getAbsolutePath()
                                + File.separator
                                //+ new Timestamp(date.getTime()).toString()
                                + "Image.jpg");

                        imageFile.createNewFile();
                    } else {
                        Toast.makeText(getBaseContext(), "Image Not saved",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                    // save image into gallery
//                    rotatedBitmap = resize(rotatedBitmap, 800, 600);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                    FileOutputStream fout = new FileOutputStream(imageFile);
                    fout.write(ostream.toByteArray());
                    fout.close();
                    ContentValues values = new ContentValues();

                    values.put(MediaStore.Images.Media.DATE_TAKEN,
                            System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA,
                            imageFile.getAbsolutePath());

                    NewMainActivity.this.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
*/

                    Log.e("FilePath",imageFile.getAbsolutePath());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static String getBase64String(Bitmap scaledBitmap) {
        if (scaledBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = Bitmap.createScaledBitmap(scaledBitmap, 320, 640, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            Log.e("Base64",encoded.toString());
            return encoded;
        }
        return null;
    }


    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraSource.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTextRecognizer();
            return;
        }

    }

    private void askCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

    }
}