package com.example.ingles.local;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ingles.ApiClient;
import com.example.ingles.ApiResponse;
import com.example.ingles.ApiService;
import com.example.ingles.local.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class SyncManager {

    public interface SyncCallback {
        void onTerminado(boolean exito, String mensaje);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void sincronizarTodo(Context context, SyncCallback callback) {
        Context appContext = context.getApplicationContext();

        executor.execute(() -> {
            EstudioDao dao = AppDatabase.getInstance(appContext).estudioDao();
            ApiService api = ApiClient.getAuthClient(appContext);

            try {
                // 1. Subir resultados pendientes guardados sin internet
                List<ResultadoPendienteEntity> pendientes = dao.obtenerPendientesSinSincronizar();
                for (ResultadoPendienteEntity item : pendientes) {
                    Response<ApiResponse> resp = api.guardarProgreso(
                            new ApiService.ProgresoRequest(item.categoriaId, item.calificacion)
                    ).execute();

                    if (resp.isSuccessful()) {
                        dao.marcarSincronizado(item.idLocal);
                    }
                }

                // 2. Descargar categorías con su estado y calificación cerrada por categoría
                Response<ApiService.CategoriasResponse> catResp = api.obtenerCategorias().execute();
                if (!catResp.isSuccessful() || catResp.body() == null || catResp.body().data == null) {
                    notificar(callback, false, "Sin conexión o error al actualizar categorías. Usando datos locales.");
                    return;
                }

                List<CategoriaEntity> entidadesCat = new ArrayList<>();
                List<Integer> idsDesbloqueadas = new ArrayList<>();

                for (ApiService.CategoriaItem c : catResp.body().data) {
                    entidadesCat.add(new CategoriaEntity(
                            c.id,
                            c.nombre,
                            c.nivel,
                            c.orden,
                            c.bloqueadaPorDefecto,
                            c.aprobada,
                            c.calificacionCerrada
                    ));
                    if (!c.bloqueadaPorDefecto) {
                        idsDesbloqueadas.add(c.id);
                    }
                }

                dao.limpiarCategorias();
                dao.guardarCategorias(entidadesCat);

                // 3. Descargar flashcards de cada categoría habilitada
                for (int catId : idsDesbloqueadas) {
                    Response<ApiService.FlashcardsResponse> fcResp = api.obtenerFlashcards(catId).execute();
                    if (fcResp.isSuccessful() && fcResp.body() != null && fcResp.body().data != null) {
                        List<FlashcardEntity> entidadesFc = new ArrayList<>();
                        for (ApiService.FlashcardItem f : fcResp.body().data) {
                            FlashcardEntity entidad = new FlashcardEntity(
                                    f.id,
                                    f.categoriaId,
                                    f.palabraIngles,
                                    f.traduccion,
                                    f.imagenUrl
                            );
                            entidad.imagenLocal = ImagenLocal.obtener(appContext, f.id, f.imagenUrl);
                            entidadesFc.add(entidad);
                        }
                        dao.limpiarFlashcardsDeCategoria(catId);
                        dao.guardarFlashcards(entidadesFc);
                    }
                }

                notificar(callback, true, "Contenido sincronizado para modo sin internet.");

            } catch (Exception e) {
                notificar(callback, false, "Modo sin conexión activo (usando caché local).");
            }
        });
    }

    public static void registrarResultadoTest(Context context, int categoriaId, double calificacion, SyncCallback callback) {
        Context appContext = context.getApplicationContext();

        executor.execute(() -> {
            EstudioDao dao = AppDatabase.getInstance(appContext).estudioDao();
            ApiService api = ApiClient.getAuthClient(appContext);

            try {
                Response<ApiResponse> resp = api.guardarProgreso(
                        new ApiService.ProgresoRequest(categoriaId, calificacion)
                ).execute();

                if (resp.isSuccessful()) {
                    dao.insertarResultadoPendiente(new ResultadoPendienteEntity(
                            categoriaId,
                            calificacion,
                            System.currentTimeMillis(),
                            true
                    ));
                    notificar(callback, true, "Calificación registrada en el servidor.");
                    return;
                }
            } catch (Exception ignored) {
            }

            dao.insertarResultadoPendiente(new ResultadoPendienteEntity(
                    categoriaId,
                    calificacion,
                    System.currentTimeMillis(),
                    false
            ));
            notificar(callback, true, "Sin internet: calificación guardada localmente y se subirá al reconectar.");
        });
    }

    private static void notificar(SyncCallback callback, boolean exito, String mensaje) {
        if (callback != null) {
            mainHandler.post(() -> callback.onTerminado(exito, mensaje));
        }
    }
}