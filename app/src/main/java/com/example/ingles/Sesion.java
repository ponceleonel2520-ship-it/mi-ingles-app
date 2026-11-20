package com.example.ingles;

import android.content.Context;
import android.content.SharedPreferences;

// Guarda en el teléfono el token y los datos de quien inició sesión.
public class Sesion {

    private static final String ARCHIVO = "sesion";

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void guardar(Context contexto, String token, String rol, String nombre) {
        prefs(contexto).edit()
                .putString("token", token)
                .putString("rol", rol)
                .putString("nombre", nombre)
                .apply();
    }

    public static String getToken(Context contexto) {
        return prefs(contexto).getString("token", null);
    }

    public static String getRol(Context contexto) {
        return prefs(contexto).getString("rol", null);
    }

    public static String getNombre(Context contexto) {
        return prefs(contexto).getString("nombre", null);
    }

    public static void cerrar(Context contexto) {
        prefs(contexto).edit().clear().apply();
    }
}
