package com.poo.proyectobless3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.R; // Asegúrate de importar R
import com.poo.proyectobless3.adapters.ComentarioAdapter;
import com.poo.proyectobless3.databinding.FragmentComentariosBinding;
import com.poo.proyectobless3.models.Comentario;

import java.util.ArrayList;
import java.util.List;

public class ComentariosFragment extends Fragment {

    private static final String TAG = "ComentariosFragment";
    private FragmentComentariosBinding binding;
    private RecyclerView recyclerView;
    private ComentarioAdapter comentarioAdapter;
    private List<Comentario> comentarioList;
    private DatabaseReference mDatabaseComments;
    private String selectedCategory = "Todas las categorías"; // Categoría seleccionada por defecto

    public ComentariosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentComentariosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mDatabaseComments = FirebaseDatabase.getInstance().getReference("comentarios");

        recyclerView = binding.recyclerViewComments;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        comentarioList = new ArrayList<>();
        comentarioAdapter = new ComentarioAdapter(comentarioList);
        recyclerView.setAdapter(comentarioAdapter);

        // Configurar el Spinner de filtro de categorías
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.product_categories_filter_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCommentCategoryFilter.setAdapter(adapter);

        binding.spinnerCommentCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "Categoría seleccionada: " + selectedCategory);
                loadComments(); // Recargar comentarios con el nuevo filtro
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        loadComments(); // Cargar comentarios iniciales (todas las categorías por defecto)

        return view;
    }

    private void loadComments() {
        Query query;
        if (selectedCategory.equals("Todas las categorías")) {
            query = mDatabaseComments; // Cargar todos los comentarios
        } else {
            // Filtrar por categoría
            query = mDatabaseComments.orderByChild("categoria").equalTo(selectedCategory);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comentarioList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comentario comentario = commentSnapshot.getValue(Comentario.class);
                    if (comentario != null) {
                        comentario.setId(commentSnapshot.getKey()); // Guarda la clave de Firebase como ID
                        comentarioList.add(comentario);
                    }
                }
                comentarioAdapter.setComentarioList(comentarioList);
                Log.d(TAG, "Comentarios cargados para categoría " + selectedCategory + ": " + comentarioList.size());

                if (comentarioList.isEmpty()) {
                    binding.tvNoComments.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoComments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar comentarios: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar comentarios: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}