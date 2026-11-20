package com.example.ingles;

// Datos que se mandan al backend para registrar a un docente (POST api/docente).
public class Docente {
    private String nombre, clave, contrasena, codigo;

    public Docente(String nombre, String clave, String contrasena, String codigo) {
        this.nombre = nombre;
        this.clave = clave;
        this.contrasena = contrasena;
        this.codigo = codigo;
    }

    public String getNombre() { return nombre; }

    public String getClave() { return clave; }

    public String getContrasena() { return contrasena; }

    public String getCodigo() { return codigo; }
}
