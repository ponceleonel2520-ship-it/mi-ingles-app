package com.example.ingles;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Dirección del backend de CodeIgniter (el que se enciende con: php spark serve).
    //
    // Emulador de Android Studio: 10.0.2.2 es la forma en que el emulador "ve" tu laptop.
    private static final String BASE_URL =  "http://192.168.1.67/ingles-api/public/";
    //
    // Teléfono real (laptop y teléfono en la misma red Wi-Fi): cambia la línea de arriba por la
    // IP de tu laptop, por ejemplo "http://192.168.1.50:8080/". La IP se ve con el comando ipconfig.
    // El backend debe encenderse con: php spark serve --host 0.0.0.0

    private static Retrofit retrofit = null;

    public static ApiService getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
