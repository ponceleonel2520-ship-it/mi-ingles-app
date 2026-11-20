package com.example.ingles;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

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
}