package com.example.ingles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.FlashcardEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EstudioFlashcardsActivity extends AppCompatActivity {

    private TextView tvCategoria, tvContador, tvIngles, tvTraduccion;
    private ImageView ivImagen;
    private Button btnVoltear, btnAnterior, btnSiguiente, btnPronunciar;

    private List<FlashcardEntity> flashcards = new ArrayList<>();
    private int indiceActual = 0;
    private boolean traduccionVisible = false;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estudio_flashcards);

        tvCategoria = findViewById(R.id.tv_estudio_categoria);
        tvContador = findViewById(R.id.tv_estudio_contador);
        tvIngles = findViewById(R.id.tv_flashcard_ingles);
        tvTraduccion = findViewById(R.id.tv_flashcard_traduccion);
        ivImagen = findViewById(R.id.iv_flashcard_imagen);
        btnVoltear = findViewById(R.id.btn_voltear);
        btnAnterior = findViewById(R.id.btn_anterior);
        btnSiguiente = findViewById(R.id.btn_siguiente);
        btnPronunciar = findViewById(R.id.btn_pronunciar);

        int categoriaId = getIntent().getIntExtra("categoria_id", -1);
        String categoriaNombre = getIntent().getStringExtra("categoria_nombre");
        tvCategoria.setText(categoriaNombre != null ? categoriaNombre : "Estudio");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        btnPronunciar.setOnClickListener(v -> {
            if (!flashcards.isEmpty() && tts != null) {
                String palabra = flashcards.get(indiceActual).palabraIngles;
                tts.speak(palabra, TextToSpeech.QUEUE_FLUSH, null, "flashcard_tts");
            }
        });

        btnVoltear.setOnClickListener(v -> {
            if (flashcards.isEmpty()) return;
            traduccionVisible = !traduccionVisible;
            actualizarEstadoTraduccion();
        });

        btnAnterior.setOnClickListener(v -> {
            if (indiceActual > 0) {
                indiceActual--;
                mostrarTarjetaActual();
            }
        });

        btnSiguiente.setOnClickListener(v -> {
            if (indiceActual < flashcards.size() - 1) {
                indiceActual++;
                mostrarTarjetaActual();
            } else {
                Toast.makeText(this, "¡Has repasado todas las tarjetas de esta categoría!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        cargarFlashcards(categoriaId);
    }

    private void cargarFlashcards(int categoriaId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FlashcardEntity> lista = AppDatabase.getInstance(getApplicationContext())
                    .estudioDao()
                    .obtenerFlashcardsPorCategoria(categoriaId);

            runOnUiThread(() -> {
                if (lista == null || lista.isEmpty()) {
                    Toast.makeText(this, "Esta categoría aún no tiene flashcards descargadas.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    flashcards = lista;
                    indiceActual = 0;
                    mostrarTarjetaActual();
                }
            });
        });
    }

    private void mostrarTarjetaActual() {
        FlashcardEntity actual = flashcards.get(indiceActual);
        tvContador.setText("Tarjeta " + (indiceActual + 1) + " de " + flashcards.size());
        tvIngles.setText(actual.palabraIngles != null ? actual.palabraIngles : "");

        traduccionVisible = false;
        actualizarEstadoTraduccion();

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

        btnAnterior.setEnabled(indiceActual > 0);
        btnAnterior.setAlpha(indiceActual > 0 ? 1.0f : 0.5f);
        btnSiguiente.setText(indiceActual == flashcards.size() - 1 ? "Finalizar" : "Siguiente");
    }

    private void actualizarEstadoTraduccion() {
        FlashcardEntity actual = flashcards.get(indiceActual);
        if (traduccionVisible) {
            tvTraduccion.setText(actual.traduccion != null ? actual.traduccion : "");
            tvTraduccion.setTextColor(Color.parseColor("#1D63ED"));
            btnVoltear.setText("Ocultar traducción");
        } else {
            tvTraduccion.setText("Toca el botón para ver significado");
            tvTraduccion.setTextColor(Color.parseColor("#64748B"));
            btnVoltear.setText("Mostrar traducción");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}