package com.example.ingles.local;

import androidx.room.Entity;
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

    public CategoriaEntity(int id, String nombre, String nivel, int orden, boolean bloqueada, boolean aprobada) {
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
        this.orden = orden;
        this.bloqueada = bloqueada;
        this.aprobada = aprobada;
    }
}