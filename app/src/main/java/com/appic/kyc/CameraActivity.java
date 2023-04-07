package com.appic.kyc;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.os.Build.VERSION_CODES.M;
import static androidx.core.graphics.TypefaceCompatUtil.getTempFile;

public class CameraActivity {

    private static Context mContext;
    private static CameraListners listners;
    private static CropImageView.CropShape cropShape;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    public final String APP_TAG = "GulfPharmacyCustomerApp";
    public final static int PICK_PHOTO_CODE = 1046;
    public String base_64_string;


    public static CameraActivity newInstance(Context mcontext, CameraListners cameraListners, CropImageView.CropShape cropShapeValue) {
        CameraActivity cameraActivity = new CameraActivity();
        mContext = mcontext;
        listners = cameraListners;
        cropShape = cropShapeValue;
        return cameraActivity;
    }

    public void openCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(mContext, "com.appic.kyc.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        //if (intent.resolveActivity(mContext.getPackageManager()) != null) {
        // Start the image capture intent to take photo
        ((Activity) mContext).startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        // }
    }

    public void getImageFromGallery() {
        // Create intent for picking a photo from the gallery
//        Intent intent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
//        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
        // Bring up gallery to select a photo
//            startActivityForResult(intent, PICK_PHOTO_CODE);
        try {
            ((Activity) mContext).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }


    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    public static String getBase64String(Bitmap scaledBitmap) {
        if (scaledBitmap != null) {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 10, byteArrayOutputStream);
//            byte[] byteArray = byteArrayOutputStream.toByteArray();
//            return Base64.encodeToString(byteArray, Base64.DEFAULT);

            //added by gourav to set the bitmap quality and reduce the size
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            Bitmap bitmap = Bitmap.createScaledBitmap(scaledBitmap, 480, 854, false);
            Bitmap bitmap = Bitmap.createScaledBitmap(scaledBitmap, 320, 480, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            return encoded;
        }
        return null;
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(mContext.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @SuppressLint("RestrictedApi")
    private void cropImage(File file) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(mContext)));
            Uri contentUri;

            if (Build.VERSION.SDK_INT > M) {

                contentUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", file);//package.provider

                //TODO:  Permission..

                mContext.grantUriPermission("mycasserole.www.mycasserole",
                        contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            } else {

                contentUri = Uri.fromFile(file);

            }


            if (cropShape == CropImageView.CropShape.OVAL) {
                CropImage.activity(contentUri)
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).setFixAspectRatio(true).setCropShape(CropImageView.CropShape.OVAL)
                        .start((Activity) mContext);
            } else if (cropShape == CropImageView.CropShape.RECTANGLE) {
                CropImage.activity(contentUri)
                        .setGuidelines(CropImageView.Guidelines.ON).
                        setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start((Activity) mContext);
            }

        } catch (ActivityNotFoundException a) {
            Log.e("Activity Not Found", "" + a.toString());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            Log.e("onActivityResult:  ", "Camera Crop Result true");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri uriresult = result.getUri();
                    Bitmap bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uriresult));
                    // iv_preview.setImageBitmap(bitmap);
                    base_64_string = getBase64String(bitmap);
                    listners.getImage(base_64_string, bitmap);
                    //   img_ProfileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            try {
                // Load the image located at photoUri into selectedImage
                Bitmap selectedImage = loadFromUri(photoUri);
//                String file_path = mContext.getExternalFilesDir("").getAbsolutePath();
//                File dir = new File(file_path);
//                if (!dir.exists())
//                    dir.mkdirs();
//                File file = new File(dir, "GulfPharmacyCustomer.png");
//                FileOutputStream fOut = new FileOutputStream(file);
//                selectedImage.compress(Bitmap.CompressFormat.JPEG, 10, fOut);
//                fOut.flush();
//                fOut.close();
//                cropImage(file);
                base_64_string = getBase64String(selectedImage);
                listners.getImage(base_64_string, selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
//                String file_path = mContext.getExternalFilesDir("").getAbsolutePath();
//                File dir = new File(file_path);
//                if (!dir.exists())
//                    dir.mkdirs();
//                File file = new File(dir, "GulfPharmacyCustomer.png");
//                try {
//                    FileOutputStream fOut = new FileOutputStream(file);
//                    takenImage.compress(Bitmap.CompressFormat.PNG, 10, fOut);
//                    fOut.flush();
//                    fOut.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                cropImage(file);
                base_64_string = getBase64String(takenImage);
                listners.getImage(base_64_string, takenImage);
            } else { // Result was a failure
                Toast.makeText(mContext, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
