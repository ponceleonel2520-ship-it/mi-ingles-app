package com.example.ingles.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class FlashcardEntity {

    @PrimaryKey
    public int id;

    public int categoriaId;
    public String palabraIngles;
    public String traduccion;
    public String imagenUrl;

    // Ruta de la imagen guardada en el teléfono (null si todavía no se descargó)
    public String imagenLocal;

    public FlashcardEntity(int id, int categoriaId, String palabraIngles, String traduccion, String imagenUrl) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.palabraIngles = palabraIngles;
        this.traduccion = traduccion;
        this.imagenUrl = imagenUrl;
    }
}