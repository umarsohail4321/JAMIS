package com.app.jamis;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {
    public static void replaceFragment(FragmentManager fragmentManager, int containerId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        fragmentTransaction.commit();
    }






    public static void uploadImage(Context context, Uri imageUri, DatabaseReference reference) {

        File file = new File(Utils.getPath(context, imageUri));

        if (!file.exists()) {
            Log.e("Upload", "File not found");
            return;
        }


        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://epsoldevops.com/jamis/") // Change to your domain
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UploadService service = retrofit.create(UploadService.class);
        Call<ResponseBody> call = service.uploadImage(filePart);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getBoolean("success")) {
                            String imageUrl = jsonObject.getString("image_url");
                            reference.setValue(imageUrl);
                            Log.d("UPLOAD", "Image URL: " + imageUrl);
                        } else {
                            Log.e("UPLOAD", "Error: " + jsonObject.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                result = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        if (result == null) {
            Log.e("FileUtils", "File path not found");
        }
        return result;
    }



    private void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES
            }, 100);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }
    }



    public static void checkAndRequestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33) - Check READ_MEDIA_IMAGES permission
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
            } else {
                // Permission already granted
                onStoragePermissionGranted();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 - 12 (API 29 - 32) - Check READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                onStoragePermissionGranted();
            }
        } else {
            // Android 6 - 9 (API 23 - 28) - Check both read & write permissions
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            } else {
                onStoragePermissionGranted();
            }
        }
    }
    public static void onStoragePermissionGranted() {
        // Now you can access images and perform your required task
        Log.d("Permissions", "Now accessing storage...");
    }


}
