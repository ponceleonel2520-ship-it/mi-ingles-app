package com.example.ingles;

import com.example.ingles.local.EstudioDao;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.CategoriaEntity;
import com.example.ingles.local.FlashcardEntity;
import com.example.ingles.local.SyncManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class TestActivity extends AppCompatActivity {

    private TextView tvTitulo, tvProgreso, tvPalabra;
    private ImageView ivImagen;
    private Button[] botonesOpciones;

    private int categoriaIdSeleccionada = -1;
    private String categoriaNombreSeleccionada = "";
    private List<FlashcardEntity> preguntas = new ArrayList<>();
    private List<String> bancoTraducciones = new ArrayList<>();

    private int indicePregunta = 0;
    private int aciertos = 0;
    private boolean bloqueado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        tvTitulo = findViewById(R.id.tv_test_titulo);
        tvProgreso = findViewById(R.id.tv_test_progreso);
        tvPalabra = findViewById(R.id.tv_test_palabra);
        ivImagen = findViewById(R.id.iv_test_imagen);

        botonesOpciones = new Button[]{
                findViewById(R.id.btn_opcion_1),
                findViewById(R.id.btn_opcion_2),
                findViewById(R.id.btn_opcion_3),
                findViewById(R.id.btn_opcion_4)
        };

        cargarCategoriasDisponibles();
    }

    private void cargarCategoriasDisponibles() {
        Executors.newSingleThreadExecutor().execute(() -> {
            EstudioDao dao = AppDatabase.getInstance(getApplicationContext()).estudioDao();
            List<CategoriaEntity> todas = dao.obtenerCategorias();
            List<CategoriaEntity> desbloqueadas = new ArrayList<>();

            for (CategoriaEntity c : todas) {
                if (!c.bloqueada) {
                    desbloqueadas.add(c);
                }
            }

            runOnUiThread(() -> {
                if (desbloqueadas.isEmpty()) {
                    Toast.makeText(this, "No hay categorías desbloqueadas para evaluar.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                if (desbloqueadas.size() == 1) {
                    iniciarTestConCategoria(desbloqueadas.get(0));
                } else {
                    String[] nombres = new String[desbloqueadas.size()];
                    for (int i = 0; i < desbloqueadas.size(); i++) {
                        nombres[i] = desbloqueadas.get(i).nombre;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Elige la categoría a evaluar")
                            .setCancelable(false)
                            .setItems(nombres, (dialog, which) -> iniciarTestConCategoria(desbloqueadas.get(which)))
                            .setNegativeButton("Cancelar", (d, w) -> finish())
                            .show();
                }
            });
        });
    }

    private void iniciarTestConCategoria(CategoriaEntity categoria) {
        categoriaIdSeleccionada = categoria.id;
        categoriaNombreSeleccionada = categoria.nombre;
        tvTitulo.setText("Test: " + categoriaNombreSeleccionada);

        Executors.newSingleThreadExecutor().execute(() -> {
            EstudioDao dao = AppDatabase.getInstance(getApplicationContext()).estudioDao();
            List<FlashcardEntity> cardsCat = dao.obtenerFlashcardsPorCategoria(categoria.id);

            // Recolectar traducciones de todas las categorías desbloqueadas como banco de opciones
            Set<String> traduccionesUnicas = new HashSet<>();
            for (CategoriaEntity c : dao.obtenerCategorias()) {
                if (!c.bloqueada) {
                    for (FlashcardEntity f : dao.obtenerFlashcardsPorCategoria(c.id)) {
                        if (f.traduccion != null && !f.traduccion.trim().isEmpty()) {
                            traduccionesUnicas.add(f.traduccion.trim());
                        }
                    }
                }
            }

            runOnUiThread(() -> {
                if (cardsCat == null || cardsCat.isEmpty()) {
                    Toast.makeText(this, "Esta categoría no tiene tarjetas descargadas.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                bancoTraducciones = new ArrayList<>(traduccionesUnicas);
                List<FlashcardEntity> barajadas = new ArrayList<>(cardsCat);
                Collections.shuffle(barajadas);
                int limite = Math.min(5, barajadas.size());
                preguntas = barajadas.subList(0, limite);

                indicePregunta = 0;
                aciertos = 0;
                mostrarPreguntaActual();
            });
        });
    }

    private void mostrarPreguntaActual() {
        bloqueado = false;
        FlashcardEntity actual = preguntas.get(indicePregunta);

        tvProgreso.setText("Pregunta " + (indicePregunta + 1) + " de " + preguntas.size());
        tvPalabra.setText(actual.palabraIngles != null ? actual.palabraIngles : "");

        if (actual.imagenLocal != null && !actual.imagenLocal.isEmpty()) {
            File archivo = new File(actual.imagenLocal);
            if (archivo.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath());
                if (bitmap != null) {
                    ivImagen.setImageBitmap(bitmap);
                } else {
                    ivImagen.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                ivImagen.setImageResource(R.mipmap.ic_launcher);
            }
        } else {
            ivImagen.setImageResource(R.mipmap.ic_launcher);
        }

        String correcta = actual.traduccion != null ? actual.traduccion.trim() : "";
        List<String> opciones = generarOpciones(correcta);

        for (int i = 0; i < botonesOpciones.length; i++) {
            Button btn = botonesOpciones[i];
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            btn.setTextColor(Color.parseColor("#0F294A"));

            if (i < opciones.size()) {
                String textoOpcion = opciones.get(i);
                btn.setText(textoOpcion);
                btn.setEnabled(true);
                btn.setOnClickListener(v -> evaluarRespuesta(btn, textoOpcion, correcta));
            } else {
                btn.setText("-");
                btn.setEnabled(false);
            }
        }
    }

    private List<String> generarOpciones(String correcta) {
        List<String> distractores = new ArrayList<>();
        for (String t : bancoTraducciones) {
            if (!t.equalsIgnoreCase(correcta)) {
                distractores.add(t);
            }
        }
        Collections.shuffle(distractores);

        List<String> resultado = new ArrayList<>();
        resultado.add(correcta);
        for (int i = 0; i < Math.min(3, distractores.size()); i++) {
            resultado.add(distractores.get(i));
        }
        Collections.shuffle(resultado);
        return resultado;
    }

    private void evaluarRespuesta(Button botonElegido, String elegida, String correcta) {
        if (bloqueado) return;
        bloqueado = true;

        boolean esCorrecta = elegida.equalsIgnoreCase(correcta);
        if (esCorrecta) {
            aciertos++;
            botonElegido.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            botonElegido.setTextColor(Color.parseColor("#166534"));
        } else {
            botonElegido.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            botonElegido.setTextColor(Color.parseColor("#991B1B"));

            for (Button b : botonesOpciones) {
                if (b.getText().toString().equalsIgnoreCase(correcta)) {
                    b.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                    b.setTextColor(Color.parseColor("#166534"));
                }
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            indicePregunta++;
            if (indicePregunta < preguntas.size()) {
                mostrarPreguntaActual();
            } else {
                finalizarTest();
            }
        }, 900);
    }

    private void finalizarTest() {
        double calificacion = Math.round(((aciertos * 100.0) / preguntas.size()) * 10.0) / 10.0;
        boolean aprobada = calificacion >= 80.0;

        SyncManager.registrarResultadoTest(this, categoriaIdSeleccionada, calificacion, (exito, mensaje) -> {
            if (aprobada) {
                SyncManager.sincronizarTodo(this, null);
            }

            String resumen = "Aciertos: " + aciertos + " de " + preguntas.size()
                    + "\nCalificación: " + calificacion + "%"
                    + "\nEstado: " + (aprobada ? "¡Aprobada! (≥ 80%)" : "No aprobada (mínimo 80%)")
                    + "\n\n" + mensaje;

            new AlertDialog.Builder(this)
                    .setTitle("Resultado del Test")
                    .setMessage(resumen)
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", (d, w) -> finish())
                    .show();
        });
    }
}