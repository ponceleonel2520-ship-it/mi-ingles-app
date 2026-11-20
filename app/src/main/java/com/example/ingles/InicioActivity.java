package com.example.ingles;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inicio_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvSaludo = findViewById(R.id.tv_saludo);
        TextView tvRol = findViewById(R.id.tv_rol);
        Button btnConfiguracion = findViewById(R.id.btn_configuracion);
        Button btnSalir = findViewById(R.id.btn_salir);

        String nombre = Sesion.getNombre(this);
        String rol = Sesion.getRol(this);

        tvSaludo.setText("Hola, " + (nombre != null ? nombre : ""));
        tvRol.setText("Sesión iniciada como " + (rol != null ? rol : ""));

        btnConfiguracion.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });

        btnSalir.setOnClickListener(v -> {
            Sesion.cerrar(InicioActivity.this);
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}