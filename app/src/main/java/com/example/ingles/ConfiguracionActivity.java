package com.example.ingles;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ingles.flotante.PalabrasDelDiaManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracionActivity extends AppCompatActivity {

    private RadioGroup rgFrecuencia;
    private RadioButton rbSemanal, rbMensual;
    private Button btnHoraInicio, btnHoraFin, btnGuardar, btnVolver;

    private String horaInicio = "07:00";
    private String horaFin = "19:00";

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
        btnGuardar = findViewById(R.id.btn_guardar_config);
        btnVolver = findViewById(R.id.btn_volver_inicio);

        cargarConfiguracionLocal();
        cargarConfiguracionServidor();

        btnHoraInicio.setOnClickListener(v -> abrirReloj(true));
        btnHoraFin.setOnClickListener(v -> abrirReloj(false));
        btnGuardar.setOnClickListener(v -> guardarConfiguracion());
        btnVolver.setOnClickListener(v -> finish());
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
        btnGuardar.setEnabled(false);

        ConfiguracionData body = new ConfiguracionData(frecuencia, horaInicio, horaFin);
        ApiClient.getAuthClient(this).guardarConfiguracion(body).enqueue(new Callback<ConfiguracionResponse>() {
            @Override
            public void onResponse(Call<ConfiguracionResponse> call, Response<ConfiguracionResponse> response) {
                btnGuardar.setEnabled(true);
                if (response.isSuccessful()) {
                    guardarLocal(frecuencia, horaInicio, horaFin);
                    Toast.makeText(ConfiguracionActivity.this, "Configuración guardada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ConfiguracionActivity.this, ApiError.mensaje(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConfiguracionResponse> call, Throwable t) {
                btnGuardar.setEnabled(true);
                Toast.makeText(ConfiguracionActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    // Por si MySQL devuelve HH:MM:SS, recortamos a HH:MM para el PUT
    private String recortarHora(String hora) {
        if (hora != null && hora.length() >= 5) {
            return hora.substring(0, 5);
        }
        return "07:00";
    }
}