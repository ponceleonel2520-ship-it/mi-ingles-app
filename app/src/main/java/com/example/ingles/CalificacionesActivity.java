package com.example.ingles;

import com.example.ingles.local.EstudioDao;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingles.local.AppDatabase;
import com.example.ingles.local.CategoriaEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CalificacionesActivity extends AppCompatActivity {

    private RecyclerView rvCalificaciones;
    private TextView tvVacio, tvResumen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificaciones);

        rvCalificaciones = findViewById(R.id.rv_calificaciones);
        tvVacio = findViewById(R.id.tv_cal_vacio);
        tvResumen = findViewById(R.id.tv_cal_resumen);

        rvCalificaciones.setLayoutManager(new LinearLayoutManager(this));
        cargarCalificaciones();
    }

    private void cargarCalificaciones() {
        Executors.newSingleThreadExecutor().execute(() -> {
            EstudioDao dao = AppDatabase.getInstance(getApplicationContext()).estudioDao();
            List<CategoriaEntity> categorias = dao.obtenerCategorias();

            List<CategoriaEntity> aprobadas = new ArrayList<>();
            for (CategoriaEntity c : categorias) {
                if (c.aprobada || c.calificacionCerrada != null) {
                    aprobadas.add(c);
                }
            }

            int totalCategorias = categorias.size();
            int totalAprobadas = aprobadas.size();

            runOnUiThread(() -> {
                tvResumen.setText("Categorías aprobadas: " + totalAprobadas + " de " + totalCategorias);

                if (aprobadas.isEmpty()) {
                    tvVacio.setText("Aún no tienes categorías aprobadas.");
                    tvVacio.setVisibility(View.VISIBLE);
                    rvCalificaciones.setVisibility(View.GONE);
                } else {
                    tvVacio.setVisibility(View.GONE);
                    rvCalificaciones.setVisibility(View.VISIBLE);
                    rvCalificaciones.setAdapter(new CalificacionesAdapter(aprobadas));
                }
            });
        });
    }

    private static class CalificacionesAdapter extends RecyclerView.Adapter<CalificacionesAdapter.ViewHolder> {

        private final List<CategoriaEntity> lista;

        CalificacionesAdapter(List<CategoriaEntity> lista) {
            this.lista = lista != null ? lista : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calificacion, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoriaEntity cat = lista.get(position);
            holder.tvCategoria.setText(cat.nombre);
            holder.tvFecha.setText("Nivel " + cat.nivel + " • Categoría aprobada");

            if (cat.calificacionCerrada != null) {
                holder.tvNota.setText(String.format(Locale.US, "%.1f", cat.calificacionCerrada));
                if (cat.calificacionCerrada >= 6.0) {
                    holder.tvNota.setBackgroundColor(Color.parseColor("#DCFCE7"));
                    holder.tvNota.setTextColor(Color.parseColor("#166534"));
                } else {
                    holder.tvNota.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    holder.tvNota.setTextColor(Color.parseColor("#991B1B"));
                }
            } else {
                holder.tvNota.setText("Pendiente");
                holder.tvNota.setBackgroundColor(Color.parseColor("#F1F5F9"));
                holder.tvNota.setTextColor(Color.parseColor("#475569"));
            }
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoria, tvFecha, tvNota;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoria = itemView.findViewById(R.id.tv_cal_categoria);
                tvFecha = itemView.findViewById(R.id.tv_cal_fecha);
                tvNota = itemView.findViewById(R.id.tv_cal_nota);
            }
        }
    }
}