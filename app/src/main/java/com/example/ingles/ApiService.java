package com.example.ingles;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    // Registro de alumno (JSON): nombre, matricula, correo, carrera, contrasena
    @POST("api/alumno")
    Call<ApiResponse> gardaAlumno(@Body Alumno alumno);

    // Registro de docente (JSON): nombre, clave, contrasena, codigo
    @POST("api/docente")
    Call<ApiResponse> guardaDocente(@Body Docente docente);

    // Login (formulario): username = matrícula o clave de docente, tipo = "alumno" o "docente"
    @FormUrlEncoded
    @POST("auth/login")
    Call<ApiResponse> login(@Field("username") String username,
                            @Field("password") String password,
                            @Field("tipo") String tipo);
}
