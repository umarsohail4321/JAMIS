package com.app.jamis;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {
    @Multipart
    @POST("post-image.php") // Ensure this matches your PHP file path
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);
}
