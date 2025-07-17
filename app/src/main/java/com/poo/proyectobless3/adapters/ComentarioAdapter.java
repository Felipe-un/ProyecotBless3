package com.poo.proyectobless3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poo.proyectobless3.R;
import com.poo.proyectobless3.models.Comentario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    private List<Comentario> comentarioList;

    public ComentarioAdapter(List<Comentario> comentarioList) {
        this.comentarioList = comentarioList;
    }

    public void setComentarioList(List<Comentario> newComentarioList) {
        this.comentarioList = newComentarioList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarioList.get(position);

        holder.tvCommentText.setText(comentario.getTexto());
        holder.tvCommentRating.setText(String.format(Locale.getDefault(), "Calificación: %d/5", comentario.getCalificacion()));
        holder.tvCommentCategory.setText(String.format("(Categoría: %s)", comentario.getCategoria()));

        // Formatear la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvCommentDate.setText(String.format("Fecha: %s", sdf.format(new Date(comentario.getFecha()))));
    }

    @Override
    public int getItemCount() {
        return comentarioList.size();
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentText, tvCommentRating, tvCommentCategory, tvCommentDate;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            tvCommentRating = itemView.findViewById(R.id.tv_comment_rating);
            tvCommentCategory = itemView.findViewById(R.id.tv_comment_category);
            tvCommentDate = itemView.findViewById(R.id.tv_comment_date);
        }
    }
}