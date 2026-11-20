package com.example.ingles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;

// Saca el mensaje de error que manda el backend (por ejemplo "Esa matrícula ya está registrada.").
public class ApiError {

    public static String mensaje(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                JsonObject json = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();

                if (json.has("messages") && json.get("messages").isJsonObject()) {
                    JsonObject messages = json.getAsJsonObject("messages");
                    if (messages.has("error")) {
                        return messages.get("error").getAsString();
                    }
                }
                if (json.has("message")) {
                    return json.get("message").getAsString();
                }
            }
        } catch (Exception e) {
            // Si la respuesta no es el JSON esperado, se usa el mensaje genérico de abajo
        }
        return "Error del servidor (código " + response.code() + ")";
    }
}
