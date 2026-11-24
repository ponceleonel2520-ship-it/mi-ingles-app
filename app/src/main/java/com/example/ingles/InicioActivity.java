package com.example.ingles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ingles.flotante.TarjetaFlotanteService;
import com.example.ingles.local.SyncManager;

public class InicioActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICACIONES = 2001;
    private boolean dialogoOverlayMostrado = false;

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
        Button btnEstudiarFlashcards = findViewById(R.id.btn_estudiar_flashcards);
        Button btnRealizarTest = findViewById(R.id.btn_realizar_test);
        Button btnMisCalificaciones = findViewById(R.id.btn_mis_calificaciones);
        Button btnConfiguracion = findViewById(R.id.btn_configuracion);
        Button btnSalir = findViewById(R.id.btn_salir);

        String nombre = Sesion.getNombre(this);
        String rol = Sesion.getRol(this);

        tvSaludo.setText("Hola, " + (nombre != null ? nombre : ""));
        tvRol.setText("Sesión iniciada como " + (rol != null ? rol : ""));

        // Sincronizar categorías, flashcards y resultados pendientes con Room
        SyncManager.sincronizarTodo(this, (exito, mensaje) ->
                Toast.makeText(InicioActivity.this, mensaje, Toast.LENGTH_SHORT).show()
        );

        pedirPermisoNotificaciones();

        btnEstudiarFlashcards.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, CategoriasActivity.class))
        );

        btnRealizarTest.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, TestActivity.class))
        );

        btnMisCalificaciones.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, CalificacionesActivity.class))
        );

        btnConfiguracion.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });

        btnSalir.setOnClickListener(v -> {
            stopService(new Intent(InicioActivity.this, TarjetaFlotanteService.class));
            Sesion.cerrar(InicioActivity.this);
            Intent intent = new Intent(InicioActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarPermisoFlotanteEIniciarServicio();
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICACIONES
                );
            }
        }
    }

    private void verificarPermisoFlotanteEIniciarServicio() {
        if (Settings.canDrawOverlays(this)) {
            Intent serviceIntent = new Intent(this, TarjetaFlotanteService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else if (!dialogoOverlayMostrado) {
            dialogoOverlayMostrado = true;
            new AlertDialog.Builder(this)
                    .setTitle("Permiso para tarjeta flotante")
                    .setMessage("Activa \"Mostrar sobre otras aplicaciones\" para que English Cards te muestre tus 5 palabras del día al desbloquear el teléfono.")
                    .setPositiveButton("Activar", (dialog, which) -> {
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName())
                        );
                        startActivity(intent);
                    })
                    .setNegativeButton("Más tarde", null)
                    .show();
        }
    }
}