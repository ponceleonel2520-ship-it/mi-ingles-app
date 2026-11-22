package com.example.ingles;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/alumno")
    Call<ApiResponse> gardaAlumno(@Body Alumno alumno);

    @FormUrlEncoded
    @POST("auth/login")
    Call<ApiResponse> login(@Field("username") String username,
                            @Field("password") String password,
                            @Field("tipo") String tipo);

    @GET("api/configuracion")
    Call<ConfiguracionResponse> obtenerConfiguracion();

    @PUT("api/configuracion")
    Call<ConfiguracionResponse> guardarConfiguracion(@Body ConfiguracionData config);

    // --- ENDPOINTS DE ESTUDIO Y SINCRONIZACIÓN (BLOQUE 5) ---

    @GET("api/categorias")
    Call<CategoriasResponse> obtenerCategorias();

    @GET("api/flashcards")
    Call<FlashcardsResponse> obtenerFlashcards(@Query("categoria_id") int categoriaId);

    @POST("api/progreso")
    Call<ApiResponse> guardarProgreso(@Body ProgresoRequest request);

    // --- MODELOS DTO PARA RETROFIT ---

    class CategoriasResponse {
        public int status;
        public List<CategoriaItem> data;
    }

    class CategoriaItem {
        public int id;
        public String nombre;
        public String nivel;
        public int orden;
        public boolean aprobada;
        @SerializedName("bloqueada_por_defecto")
        public boolean bloqueadaPorDefecto;
    }

    class FlashcardsResponse {
        public int status;
        public List<FlashcardItem> data;
    }

    class FlashcardItem {
        public int id;
        @SerializedName("categoria_id")
        public int categoriaId;
        @SerializedName("palabra_ingles")
        public String palabraIngles;
        public String traduccion;
        @SerializedName("imagen_url")
        public String imagenUrl;
    }

    class ProgresoRequest {
        @SerializedName("categoria_id")
        public int categoriaId;
        public double calificacion;

        public ProgresoRequest(int categoriaId, double calificacion) {
            this.categoriaId = categoriaId;
            this.calificacion = calificacion;
        }
    }
}