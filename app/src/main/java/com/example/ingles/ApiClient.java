package com.example.ingles;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2/ingles-api/public/";

    private static Retrofit retrofit = null;

    // Cliente público (para Login y Registro, donde aún no hay token)
    public static ApiService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    // Cliente autenticado (adjunta automáticamente el token JWT en cada petición)
    public static ApiService getAuthClient(Context context) {
        Context appContext = context.getApplicationContext();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = Sesion.getToken(appContext);
                    Request original = chain.request();

                    if (token != null && !token.isEmpty()) {
                        Request requestConToken = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(requestConToken);
                    }

                    return chain.proceed(original);
                })
                .build();

        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return authRetrofit.create(ApiService.class);
    }
}