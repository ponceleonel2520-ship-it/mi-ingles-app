package com.example.ingles.flotante;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.ingles.R;
import com.example.ingles.local.FlashcardEntity;

import java.io.File;

public class TarjetaFlotanteService extends Service {

    private static final String CHANNEL_ID = "english_cards_flotante_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long DURACION_TARJETA_MS = 4000L;

    private WindowManager windowManager;
    private View vistaFlotante;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final java.util.concurrent.ExecutorService executor =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    private long ultimaVezMostradaMs = 0L;

    private final BroadcastReceiver pantallaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String accion = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(accion) || Intent.ACTION_USER_PRESENT.equals(accion)) {
                intentarMostrarTarjeta();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        crearCanalNotificacion();
        startForeground(NOTIFICATION_ID, construirNotificacion());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(pantallaReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    private void intentarMostrarTarjeta() {
        android.util.Log.d("TARJETA", "Evento de pantalla recibido");

        // Evita duplicar la tarjeta si SCREEN_ON y USER_PRESENT ocurren con menos de 5 segundos de diferencia
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaVezMostradaMs < 5000L) {
            android.util.Log.d("TARJETA", "Descartado: se mostró hace menos de 5 segundos");
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            android.util.Log.d("TARJETA", "Descartado: falta el permiso de mostrar sobre otras apps");
            return;
        }

        if (!PalabrasDelDiaManager.estaDentroDeHorario(this)) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            android.util.Log.d("TARJETA", "Descartado: fuera de horario. Hora del emulador = "
                    + c.get(java.util.Calendar.HOUR_OF_DAY) + ":" + c.get(java.util.Calendar.MINUTE));
            return;
        }

        ultimaVezMostradaMs = ahora;

        // La consulta a Room va en un hilo secundario; dibujar la tarjeta, en el principal
        executor.execute(() -> {
            FlashcardEntity palabra = PalabrasDelDiaManager.obtenerPalabraAleatoriaDelDia(this);
            if (palabra == null) {
                android.util.Log.d("TARJETA", "Descartado: no hay flashcards en Room (categorías bloqueadas o sin sincronizar)");
                return;
            }
            android.util.Log.d("TARJETA", "Mostrando: " + palabra.palabraIngles);
            handler.post(() -> mostrarTarjetaFlotante(palabra));
        });
    }

    private void mostrarTarjetaFlotante(FlashcardEntity palabra) {
        removerTarjetaActual();

        LayoutInflater inflater = LayoutInflater.from(this);
        vistaFlotante = inflater.inflate(R.layout.tarjeta_flotante, null);

        TextView tvIngles = vistaFlotante.findViewById(R.id.tvTarjetaIngles);
        TextView tvTraduccion = vistaFlotante.findViewById(R.id.tvTarjetaTraduccion);
        ImageView ivImagen = vistaFlotante.findViewById(R.id.ivTarjetaImagen);

        tvIngles.setText(palabra.palabraIngles != null ? palabra.palabraIngles : "");
        tvTraduccion.setText(palabra.traduccion != null ? palabra.traduccion : "");

        if (palabra.imagenLocal != null && !palabra.imagenLocal.isEmpty()) {
            File archivo = new File(palabra.imagenLocal);
            if (archivo.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath());
                if (bitmap != null) {
                    ivImagen.setImageBitmap(bitmap);
                }
            }
        }

        int tipoVentana = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                tipoVentana,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 80;

        try {
            windowManager.addView(vistaFlotante, params);
            handler.postDelayed(this::removerTarjetaActual, DURACION_TARJETA_MS);
        } catch (Exception ignored) {
            vistaFlotante = null;
        }
    }

    private void removerTarjetaActual() {
        handler.removeCallbacksAndMessages(null);
        if (vistaFlotante != null && windowManager != null) {
            try {
                windowManager.removeView(vistaFlotante);
            } catch (Exception ignored) {
            }
            vistaFlotante = null;
        }
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tarjetas flotantes de inglés",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Mantiene activo el recordatorio de vocabulario al desbloquear el teléfono");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification construirNotificacion() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("English Cards activo")
                .setContentText("Mostrando tus 5 palabras del día dentro de tu horario")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        try {
            unregisterReceiver(pantallaReceiver);
        } catch (Exception ignored) {
        }
        removerTarjetaActual();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}