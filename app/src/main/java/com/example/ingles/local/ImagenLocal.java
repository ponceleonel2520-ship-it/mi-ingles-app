package com.example.ingles.local;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImagenLocal {

    private static final OkHttpClient cliente = new OkHttpClient();

    /**
     * Devuelve la ruta de la imagen guardada en el teléfono. Si todavía no existe,
     * la descarga. Si falla (por ejemplo, sin internet), devuelve null.
     */
    public static String obtener(Context contexto, int id, String url) {
        if (url == null || url.isEmpty()) return null;

        File carpeta = new File(contexto.getFilesDir(), "imagenes");
        if (!carpeta.exists() && !carpeta.mkdirs()) return null;

        // El nombre incluye el del archivo del servidor: si el docente cambia la imagen, se descarga de nuevo
        String nombreRemoto = url.substring(url.lastIndexOf('/') + 1);
        File destino = new File(carpeta, id + "_" + nombreRemoto);

        if (destino.exists() && destino.length() > 0) {
            return destino.getAbsolutePath();
        }

        Request request = new Request.Builder().url(url).build();

        try (Response respuesta = cliente.newCall(request).execute()) {
            if (!respuesta.isSuccessful() || respuesta.body() == null) return null;

            try (InputStream entrada = respuesta.body().byteStream();
                 FileOutputStream salida = new FileOutputStream(destino)) {
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = entrada.read(buffer)) != -1) {
                    salida.write(buffer, 0, leidos);
                }
            }
            return destino.getAbsolutePath();
        } catch (Exception e) {
            if (destino.exists()) destino.delete();
            return null;
        }
    }
}