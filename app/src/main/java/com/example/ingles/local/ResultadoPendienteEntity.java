package com.example.ingles.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resultados_pendientes")
public class ResultadoPendienteEntity {

    @PrimaryKey(autoGenerate = true)
    public int idLocal;

    public int categoriaId;
    public double calificacion;
    public long fechaRegistro;
    public boolean sincronizado;

    public ResultadoPendienteEntity(int categoriaId, double calificacion, long fechaRegistro, boolean sincronizado) {
        this.categoriaId = categoriaId;
        this.calificacion = calificacion;
        this.fechaRegistro = fechaRegistro;
        this.sincronizado = sincronizado;
    }
}