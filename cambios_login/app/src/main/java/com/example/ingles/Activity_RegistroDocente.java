package com.example.ingles;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_RegistroDocente extends AppCompatActivity {

    private EditText etNombre, etClave, etPassword, etCodigo;
    private Button btnRegistrar;
    private TextView tvVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_docente);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.doc_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNombre = findViewById(R.id.et_nombre_doc);
        etClave = findViewById(R.id.et_clave_doc);
        etPassword = findViewById(R.id.et_password_doc);
        etCodigo = findViewById(R.id.et_codigo_doc);
        btnRegistrar = findViewById(R.id.btn_register_doc);
        tvVolver = findViewById(R.id.tv_volver_doc);

        btnRegistrar.setOnClickListener(v -> registrarDocente());
        tvVolver.setOnClickListener(v -> finish());
    }

    private void registrarDocente() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String clave = etClave.getText() != null ? etClave.getText().toString().trim() : "";
        String contrasena = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String codigo = etCodigo.getText() != null ? etCodigo.getText().toString().trim() : "";

        if (nombre.isEmpty() || clave.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (clave.length() != 9) {
            Toast.makeText(this, "La clave de docente debe tener 9 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contrasena.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);

        Docente docente = new Docente(nombre, clave, contrasena, codigo);

        ApiClient.getClient().guardaDocente(docente).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnRegistrar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(Activity_RegistroDocente.this, response.body().getMessage(), Toast.LENGTH_LONG).show();

                    // Registro exitoso: pasa directo al login, con la clave ya escrita
                    Intent intent = new Intent(Activity_RegistroDocente.this, LoginActivity.class);
                    intent.putExtra("tipo", "docente");
                    intent.putExtra("usuario", clave);
                    startActivity(intent);
                    finish();
                } else {
                    String mensaje = ApiError.mensaje(response);
                    Log.e("ERROR_API", "Rechazo del servidor: " + mensaje);
                    Toast.makeText(Activity_RegistroDocente.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnRegistrar.setEnabled(true);
                Toast.makeText(Activity_RegistroDocente.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR_API", "Falla en Retrofit: " + t.getMessage());
            }
        });
    }
}
