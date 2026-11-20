package com.example.ingles;

// Respuesta del backend. Los campos que no vienen en una respuesta quedan en null o 0.
public class ApiResponse {

    private int status;

    private String message;

    private String token;

    private String rol;

    private String nombre;

    private int id;

    public int getStatus() { return status; }

    public String getMessage() { return message; }

    public String getToken() { return token; }

    public String getRol() { return rol; }

    public String getNombre() { return nombre; }

    public int getId() { return id; }
}
