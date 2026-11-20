package com.example.ingles;

import com.google.gson.annotations.SerializedName;

public class ConfiguracionData {

    @SerializedName("frecuencia_test")
    public String frecuenciaTest;

    @SerializedName("hora_inicio")
    public String horaInicio;

    @SerializedName("hora_fin")
    public String horaFin;

    public ConfiguracionData() {
    }

    public ConfiguracionData(String frecuenciaTest, String horaInicio, String horaFin) {
        this.frecuenciaTest = frecuenciaTest;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
}