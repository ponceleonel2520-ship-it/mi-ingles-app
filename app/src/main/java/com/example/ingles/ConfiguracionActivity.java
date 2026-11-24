package com.example.ingles;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ingles.flotante.PalabrasDelDiaManager;
import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.CategoriaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracionActivity extends AppCompatActivity {

    private RadioGroup rgFrecuencia;
    private RadioButton rbSemanal, rbMensual;
    private Button btnHoraInicio, btnHoraFin, btnTodasCategorias, btnGuardar, btnVolver;
    private Spinner spCategoriaFlotante;

    private String horaInicio = "07:00";
    private String horaFin = "19:00";

    private final List<Integer> listaIdsCategorias = new ArrayList<>();
    private final List<String> listaNombresCategorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.config_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rgFrecuencia = findViewById(R.id.rg_frecuencia);
        rbSemanal = findViewById(R.id.rb_semanal);
        rbMensual = findViewById(R.id.rb_mensual);
        btnHoraInicio = findViewById(R.id.btn_hora_inicio);
        btnHoraFin = findViewById(R.id.btn_hora_fin);
        spCategoriaFlotante = findViewById(R.id.sp_categoria_flotante);
        btnTodasCategorias = findViewById(R.id.btn_todas_categorias);
        btnGuardar = findViewById(R.id.btn_guardar_config);
        btnVolver = findViewById(R.id.btn_volver_inicio);

        cargarCategoriasSpinner();
        cargarConfiguracionLocal();
        cargarConfiguracionServidor();

        btnHoraInicio.setOnClickListener(v -> abrirReloj(true));
        btnHoraFin.setOnClickListener(v -> abrirReloj(false));
        btnTodasCategorias.setOnClickListener(v -> {
            if (!listaIdsCategorias.isEmpty()) {
                spCategoriaFlotante.setSelection(0);
                Toast.makeText(this, "Modo aleatorio: todas las categorías", Toast.LENGTH_SHORT).show();
            }
        });
        btnGuardar.setOnClickListener(v -> guardarConfiguracion());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarCategoriasSpinner() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoriaEntity> categorias = AppDatabase.getInstance(getApplicationContext())
                    .estudioDao()
                    .obtenerCategorias();

            List<Integer> ids = new ArrayList<>();
            List<String> nombres = new ArrayList<>();

            ids.add(-1);
            nombres.add("Todas las categorías (Aleatorio)");

            if (categorias != null) {
                for (CategoriaEntity c : categorias) {
                    if (!c.bloqueada) {
                        ids.add(c.id);
                        nombres.add(c.nombre);
                    }
                }
            }

            int guardadaId = PalabrasDelDiaManager.obtenerCategoriaFlotante(this);

            runOnUiThread(() -> {
                listaIdsCategorias.clear();
                listaIdsCategorias.addAll(ids);
                listaNombresCategorias.clear();
                listaNombresCategorias.addAll(nombres);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        listaNombresCategorias
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategoriaFlotante.setAdapter(adapter);

                int index = listaIdsCategorias.indexOf(guardadaId);
                spCategoriaFlotante.setSelection(index >= 0 ? index : 0);
            });
        });
    }

    private void abrirReloj(boolean esInicio) {
        String actual = esInicio ? horaInicio : horaFin;
        String[] partes = actual.split(":");
        int h = Integer.parseInt(partes[0]);
        int m = Integer.parseInt(partes[1]);

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String formateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (esInicio) {
                horaInicio = formateada;
                btnHoraInicio.setText(horaInicio);
            } else {
                horaFin = formateada;
                btnHoraFin.setText(horaFin);
            }
        }, h, m, true).show();
    }

    private void cargarConfiguracionLocal() {
        SharedPreferences prefs = getSharedPreferences("config_alumno", MODE_PRIVATE);
        String freq = prefs.getString("frecuencia_test", "semanal");
        horaInicio = recortarHora(prefs.getString("hora_inicio", "07:00"));
        horaFin = recortarHora(prefs.getString("hora_fin", "19:00"));

        PalabrasDelDiaManager.guardarHorario(this, horaInicio, horaFin);

        if ("mensual".equals(freq)) {
            rbMensual.setChecked(true);
        } else {
            rbSemanal.setChecked(true);
        }
        btnHoraInicio.setText(horaInicio);
        btnHoraFin.setText(horaFin);
    }

    private void cargarConfiguracionServidor() {
        ApiClient.getAuthClient(this).obtenerConfiguracion().enqueue(new Callback<ConfiguracionResponse>() {
            @Override
            public void onResponse(Call<ConfiguracionResponse> call, Response<ConfiguracionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ConfiguracionData d = response.body().data;
                    String freq = d.frecuenciaTest != null ? d.frecuenciaTest : "semanal";
                    horaInicio = recortarHora(d.horaInicio != null ? d.horaInicio : "07:00");
                    horaFin = recortarHora(d.horaFin != null ? d.horaFin : "19:00");

                    if ("mensual".equals(freq)) {
                        rbMensual.setChecked(true);
                    } else {
                        rbSemanal.setChecked(true);
                    }
                    btnHoraInicio.setText(horaInicio);
                    btnHoraFin.setText(horaFin);
                    guardarLocal(freq, horaInicio, horaFin);
                }
            }

            @Override
            public void onFailure(Call<ConfiguracionResponse> call, Throwable t) {
                // Si no hay conexión, se mantienen los valores guardados localmente
            }
        });
    }

    private void guardarConfiguracion() {
        if (horaInicio.compareTo(horaFin) >= 0) {
            Toast.makeText(this, "La hora de inicio debe ser anterior a la hora de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        String frecuencia = rbMensual.isChecked() ? "mensual" : "semanal";
        int pos = spCategoriaFlotante.getSelectedItemPosition();
        int categoriaId = (pos >= 0 && pos < listaIdsCategorias.size()) ? listaIdsCategorias.get(pos) : -1;

        PalabrasDelDiaManager.guardarCategoriaFlotante(this, categoriaId);
        guardarLocal(frecuencia, horaInicio, horaFin);

        btnGuardar.setEnabled(false);

        ConfiguracionData body = new ConfiguracionData(frecuencia, horaInicio, horaFin);
        ApiClient.getAuthClient(this).guardarConfiguracion(body).enqueue(new Callback<ConfiguracionResponse>() {
            @Override
            public void onResponse(Call<ConfiguracionResponse> call, Response<ConfiguracionResponse> response) {
                btnGuardar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ConfiguracionActivity.this, "Configuración guardada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ConfiguracionActivity.this, ApiError.mensaje(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConfiguracionResponse> call, Throwable t) {
                btnGuardar.setEnabled(true);
                Toast.makeText(ConfiguracionActivity.this, "Guardado localmente (sin conexión)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void guardarLocal(String frecuencia, String inicio, String fin) {
        getSharedPreferences("config_alumno", MODE_PRIVATE).edit()
                .putString("frecuencia_test", frecuencia)
                .putString("hora_inicio", inicio)
                .putString("hora_fin", fin)
                .apply();
        PalabrasDelDiaManager.guardarHorario(this, inicio, fin);
    }

    private String recortarHora(String hora) {
        if (hora != null && hora.length() >= 5) {
            return hora.substring(0, 5);
        }
        return "07:00";
    }
}