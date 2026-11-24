package com.example.ingles.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EstudioDao {

    // --- CATEGORÍAS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarCategorias(List<CategoriaEntity> categorias);

    @Query("SELECT * FROM categorias ORDER BY orden ASC")
    List<CategoriaEntity> obtenerCategorias();

    @Query("DELETE FROM categorias")
    void limpiarCategorias();

    // --- FLASHCARDS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarFlashcards(List<FlashcardEntity> flashcards);

    @Query("SELECT * FROM flashcards WHERE categoriaId = :categoriaId ORDER BY id ASC")
    List<FlashcardEntity> obtenerFlashcardsPorCategoria(int categoriaId);

    @Query("SELECT * FROM flashcards ORDER BY RANDOM() LIMIT 1")
    FlashcardEntity obtenerFlashcardAleatoria();

    @Query("DELETE FROM flashcards WHERE categoriaId = :categoriaId")
    void limpiarFlashcardsDeCategoria(int categoriaId);

    @Query("DELETE FROM flashcards")
    void limpiarTodasFlashcards();

    // --- RESULTADOS PENDIENTES (MODO OFFLINE) ---
    @Query("SELECT * FROM resultados_pendientes ORDER BY fechaRegistro DESC")
    List<ResultadoPendienteEntity> obtenerTodosLosResultados();

    @Insert
    void insertarResultadoPendiente(ResultadoPendienteEntity resultado);

    @Query("SELECT * FROM resultados_pendientes WHERE sincronizado = 0 ORDER BY fechaRegistro ASC")
    List<ResultadoPendienteEntity> obtenerPendientesSinSincronizar();

    @Query("UPDATE resultados_pendientes SET sincronizado = 1 WHERE idLocal = :idLocal")
    void marcarSincronizado(int idLocal);
}