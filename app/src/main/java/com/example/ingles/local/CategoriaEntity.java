package com.example.ingles.local;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class CategoriaEntity {

    @PrimaryKey
    public int id;

    public String nombre;
    public String nivel;
    public int orden;
    public boolean bloqueada;
    public boolean aprobada;

    @Nullable
    public Double calificacionCerrada;

    public CategoriaEntity(int id, String nombre, String nivel, int orden, boolean bloqueada, boolean aprobada, @Nullable Double calificacionCerrada) {
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
        this.orden = orden;
        this.bloqueada = bloqueada;
        this.aprobada = aprobada;
        this.calificacionCerrada = calificacionCerrada;
    }

    @Ignore
    public CategoriaEntity(int id, String nombre, String nivel, int orden, boolean bloqueada, boolean aprobada) {
        this(id, nombre, nivel, orden, bloqueada, aprobada, null);
    }
}