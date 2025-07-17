package com.poo.proyectobless3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.activities.ProductoFormActivity; // Necesitarás crear esta actividad
import com.poo.proyectobless3.adapters.ProductoAdapter;
import com.poo.proyectobless3.databinding.FragmentInventarioBinding;
import com.poo.proyectobless3.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class InventarioFragment extends Fragment implements ProductoAdapter.OnItemActionListener {

    private static final String TAG = "InventarioFragment";
    private FragmentInventarioBinding binding;
    private RecyclerView recyclerView;
    private ProductoAdapter productoAdapter;
    private List<Producto> productoList;
    private DatabaseReference mDatabase;
    private String userRole; // Rol del usuario logueado

    public InventarioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventarioBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Obtener el rol del usuario pasado desde MainActivity
        if (getArguments() != null) {
            userRole = getArguments().getString("userRole");
            Log.d(TAG, "Rol de usuario en InventarioFragment: " + userRole);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("productos"); // Referencia a la colección 'productos'

        recyclerView = binding.recyclerViewProducts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productoList = new ArrayList<>();
        productoAdapter = new ProductoAdapter(productoList, userRole, this); // 'this' como listener
        recyclerView.setAdapter(productoAdapter);

        // Configurar el botón flotante (FAB) para agregar productos
        // Solo visible para administradores (RF3)
        if (userRole != null && userRole.equals("admin")) {
            binding.fabAddProduct.setVisibility(View.VISIBLE);
            binding.fabAddProduct.setOnClickListener(v -> {
                // Iniciar la actividad para agregar/editar producto
                Intent intent = new Intent(getContext(), ProductoFormActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fabAddProduct.setVisibility(View.GONE);
        }

        loadProducts(); // Cargar productos desde Firebase

        return view;
    }

    private void loadProducts() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productoList.clear(); // Limpiar la lista antes de añadir nuevos datos
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Producto producto = productSnapshot.getValue(Producto.class);
                    if (producto != null) {
                        producto.setId(productSnapshot.getKey()); // Guardar la clave de Firebase como ID
                        productoList.add(producto);
                    }
                }
                productoAdapter.setProductoList(productoList); // Actualizar el adaptador con los nuevos datos
                Log.d(TAG, "Productos cargados: " + productoList.size());
                if (productoList.isEmpty()) {
                    binding.tvNoProducts.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoProducts.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar productos: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Producto producto) {
        Toast.makeText(getContext(), "Editar: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
        // Lógica para ir a la pantalla de edición, pasando el producto
        Intent intent = new Intent(getContext(), ProductoFormActivity.class);
        intent.putExtra("productId", producto.getId()); // Pasar el ID del producto
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Producto producto) {
        Toast.makeText(getContext(), "Eliminar: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
        // Lógica para eliminar el producto de Firebase
        mDatabase.child(producto.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Producto eliminado.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}