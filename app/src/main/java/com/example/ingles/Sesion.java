package com.example.ingles;

import android.content.Context;
import android.content.SharedPreferences;

// Guarda en el teléfono el token y los datos de quien inició sesión.
public class Sesion {

    private static final String ARCHIVO = "sesion";

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static void guardar(Context contexto, String token, String rol, String nombre, int id) {
        prefs(contexto).edit()
                .putString("token", token)
                .putString("rol", rol)
                .putString("nombre", nombre)
                .putInt("id", id)
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

    public static int getId(Context contexto) {
        return prefs(contexto).getInt("id", 0);
    }

    public static void guardarCalificacionFinal(Context contexto, String notaFormateada) {
        prefs(contexto).edit()
                .putString("calificacion_final", notaFormateada)
                .apply();
    }

    public static String getCalificacionFinal(Context contexto) {
        return prefs(contexto).getString("calificacion_final", null);
    }

    public static void cerrar(Context contexto) {
        prefs(contexto).edit().clear().apply();
    }
}