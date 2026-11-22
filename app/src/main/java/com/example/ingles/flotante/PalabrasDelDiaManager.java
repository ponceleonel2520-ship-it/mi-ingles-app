package com.example.ingles.flotante;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.CategoriaEntity;
import com.example.ingles.local.EstudioDao;
import com.example.ingles.local.FlashcardEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PalabrasDelDiaManager {

    private static final String PREFS_NAME = "ingles_flotante_prefs";
    private static final String KEY_FECHA = "fecha_seleccion";
    private static final String KEY_IDS_DIA = "ids_palabras_dia";
    private static final String KEY_HORA_INICIO = "hora_inicio";
    private static final String KEY_HORA_FIN = "hora_fin";

    public static void guardarHorario(Context context, String horaInicio, String horaFin) {
        if (horaInicio == null || horaFin == null) return;
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_HORA_INICIO, horaInicio)
                .putString(KEY_HORA_FIN, horaFin)
                .apply();
    }

    public static boolean estaDentroDeHorario(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String inicio = prefs.getString(KEY_HORA_INICIO, "07:00");
        String fin = prefs.getString(KEY_HORA_FIN, "19:00");

        int minInicio = aMinutos(inicio, 7 * 60);
        int minFin = aMinutos(fin, 19 * 60);

        Calendar ahora = Calendar.getInstance();
        int minActual = ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE);

        if (minInicio <= minFin) {
            return minActual >= minInicio && minActual <= minFin;
        } else {
            return minActual >= minInicio || minActual <= minFin;
        }
    }

    public static FlashcardEntity obtenerPalabraAleatoriaDelDia(Context context) {
        Context appContext = context.getApplicationContext();
        EstudioDao dao = AppDatabase.getInstance(appContext).estudioDao();

        List<CategoriaEntity> categorias = dao.obtenerCategorias();
        List<FlashcardEntity> disponibles = new ArrayList<>();
        for (CategoriaEntity c : categorias) {
            if (!c.bloqueada) {
                disponibles.addAll(dao.obtenerFlashcardsPorCategoria(c.id));
            }
        }

        if (disponibles.isEmpty()) return null;

        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String fechaGuardada = prefs.getString(KEY_FECHA, "");
        String idsGuardados = prefs.getString(KEY_IDS_DIA, "");

        List<FlashcardEntity> seleccionadas = new ArrayList<>();

        if (hoy.equals(fechaGuardada) && !idsGuardados.isEmpty()) {
            String[] partes = idsGuardados.split(",");
            for (String p : partes) {
                try {
                    int idBuscado = Integer.parseInt(p.trim());
                    for (FlashcardEntity f : disponibles) {
                        if (f.id == idBuscado) {
                            seleccionadas.add(f);
                            break;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (seleccionadas.isEmpty()) {
            List<FlashcardEntity> copia = new ArrayList<>(disponibles);
            Collections.shuffle(copia);
            int limite = Math.min(5, copia.size());
            seleccionadas = copia.subList(0, limite);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < seleccionadas.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(seleccionadas.get(i).id);
            }
            prefs.edit()
                    .putString(KEY_FECHA, hoy)
                    .putString(KEY_IDS_DIA, sb.toString())
                    .apply();
        }

        return seleccionadas.get(new Random().nextInt(seleccionadas.size()));
    }

    private static int aMinutos(String hhmm, int porDefecto) {
        try {
            String[] p = hhmm.trim().split(":");
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            return h * 60 + m;
        } catch (Exception e) {
            return porDefecto;
        }
    }
}