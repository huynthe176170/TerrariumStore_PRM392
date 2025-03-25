package com.example.project_terrarium_prm392.api;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient {
    private static final String TAG = "ApiClient";
    // URL đúng của API backend, dùng HTTPS theo Swagger UI

    // Đảm bảo URL kết thúc bằng dấu "/"
    private static final String BASE_URL = "https://10.0.2.2:7024/api/";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient;
    private static TerrariumApiService apiService = null;

    public static TerrariumApiService getApiService() {
        if (apiService == null) {
            Log.d(TAG, "Creating new API service instance");
            // Create a Gson instance that's lenient
            Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Đảm bảo đúng định dạng ngày tháng
                .create();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
            
            Log.d(TAG, "API Base URL: " + BASE_URL);
            apiService = retrofit.create(TerrariumApiService.class);
        }
        return apiService;
    }

    // Phương thức này có thể được gọi để reset apiService khi cần thiết
    public static void resetApiService() {
        apiService = null;
        okHttpClient = null;
        retrofit = null;
        Log.d(TAG, "API Service has been reset");
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Tạo một trust manager không xác thực certificate
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Tạo một SSLContext cho HTTPS
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Tạo một SSL socket factory từ SSLContext
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, "API Log: " + message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d(TAG, "Verifying hostname: " + hostname);
                    return true; // Bỏ qua hostname verification
                }
            });
            
            // Tăng thời gian timeout để xử lý mạng chậm
            builder.connectTimeout(60, TimeUnit.SECONDS);
            builder.readTimeout(60, TimeUnit.SECONDS);
            builder.writeTimeout(60, TimeUnit.SECONDS);
            builder.addInterceptor(logging);
            
            // Thêm retry cho các lỗi mạng tạm thời
            builder.retryOnConnectionFailure(true);

            Log.d(TAG, "Created custom OkHttpClient with SSL settings");
            okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            Log.e(TAG, "Error creating OkHttpClient", e);
            throw new RuntimeException(e);
        }
    }
} 