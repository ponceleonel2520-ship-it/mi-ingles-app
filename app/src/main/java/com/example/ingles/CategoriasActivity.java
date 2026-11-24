package com.example.ingles;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.CategoriaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CategoriasActivity extends AppCompatActivity {

    private RecyclerView rvCategorias;
    private TextView tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        rvCategorias = findViewById(R.id.rv_categorias);
        tvVacio = findViewById(R.id.tv_categorias_vacio);
        rvCategorias.setLayoutManager(new LinearLayoutManager(this));

        cargarCategoriasLocales();
    }

    private void cargarCategoriasLocales() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoriaEntity> lista = AppDatabase.getInstance(getApplicationContext())
                    .estudioDao()
                    .obtenerCategorias();

            runOnUiThread(() -> {
                if (lista == null || lista.isEmpty()) {
                    tvVacio.setVisibility(View.VISIBLE);
                    rvCategorias.setVisibility(View.GONE);
                } else {
                    tvVacio.setVisibility(View.GONE);
                    rvCategorias.setVisibility(View.VISIBLE);
                    rvCategorias.setAdapter(new CategoriasAdapter(lista));
                }
            });
        });
    }

    private class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.ViewHolder> {

        private final List<CategoriaEntity> categorias;

        CategoriasAdapter(List<CategoriaEntity> categorias) {
            this.categorias = categorias != null ? categorias : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_categoria, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoriaEntity cat = categorias.get(position);
            holder.tvNombre.setText(cat.nombre);
            holder.tvNivel.setText("Nivel: " + (cat.nivel != null ? cat.nivel : "General"));

            if (cat.bloqueada) {
                holder.tvEstado.setText("Bloqueada");
                holder.tvEstado.setBackgroundColor(Color.parseColor("#E2E8F0"));
                holder.tvEstado.setTextColor(Color.parseColor("#64748B"));
                holder.itemView.setAlpha(0.75f);
            } else if (cat.aprobada) {
                holder.tvEstado.setText("Aprobada");
                holder.tvEstado.setBackgroundColor(Color.parseColor("#DCFCE7"));
                holder.tvEstado.setTextColor(Color.parseColor("#166534"));
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.tvEstado.setText("Disponible");
                holder.tvEstado.setBackgroundColor(Color.parseColor("#E8F0FE"));
                holder.tvEstado.setTextColor(Color.parseColor("#1D63ED"));
                holder.itemView.setAlpha(1.0f);
            }

            holder.itemView.setOnClickListener(v -> {
                if (cat.bloqueada) {
                    Toast.makeText(CategoriasActivity.this,
                            "Aprueba la categoría anterior para desbloquear esta.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(CategoriasActivity.this, EstudioFlashcardsActivity.class);
                    intent.putExtra("categoria_id", cat.id);
                    intent.putExtra("categoria_nombre", cat.nombre);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categorias.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvNivel, tvEstado;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tv_cat_nombre);
                tvNivel = itemView.findViewById(R.id.tv_cat_nivel);
                tvEstado = itemView.findViewById(R.id.tv_cat_estado);
            }
        }
    }
}