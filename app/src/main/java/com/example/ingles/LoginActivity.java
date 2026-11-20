package com.example.ingles;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.et_usuario);
        etPassword = findViewById(R.id.et_password_login);
        btnLogin = findViewById(R.id.btn_login);
        tvIrRegistro = findViewById(R.id.tv_ir_registro);

        // Si llegamos desde un registro, se deja lista la matrícula recién creada
        String usuarioRecibido = getIntent().getStringExtra("usuario");
        if (usuarioRecibido != null) {
            etUsuario.setText(usuarioRecibido);
        }

        btnLogin.setOnClickListener(v -> iniciarSesion());

        // Regresa a la pantalla de inicio para registrarse
        tvIrRegistro.setOnClickListener(v -> finish());
    }

    private void iniciarSesion() {
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Escribe tu matrícula y tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        ApiClient.getClient().login(usuario, password, "alumno").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    ApiResponse datos = response.body();
                    String rol = datos.getRol() != null ? datos.getRol() : "alumno";

                    Sesion.guardar(LoginActivity.this, datos.getToken(), rol, datos.getNombre(), datos.getId());
                    Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, ApiError.mensaje(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}