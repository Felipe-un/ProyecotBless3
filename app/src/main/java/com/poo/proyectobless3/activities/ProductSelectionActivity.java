package com.poo.proyectobless3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.adapters.ProductSelectionAdapter;
import com.poo.proyectobless3.databinding.ActivityProductSelectionBinding;
import com.poo.proyectobless3.models.Pedido;
import com.poo.proyectobless3.models.Producto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductSelectionActivity extends AppCompatActivity {

    private static final String TAG = "ProductSelectionAct";
    private ActivityProductSelectionBinding binding;
    private DatabaseReference mDatabaseProductos;

    private List<Producto> availableProducts;
    private ProductSelectionAdapter productSelectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabaseProductos = FirebaseDatabase.getInstance().getReference("productos");

        availableProducts = new ArrayList<>();

        // Configurar RecyclerView
        binding.recyclerViewAvailableProducts.setLayoutManager(new LinearLayoutManager(this));
        productSelectionAdapter = new ProductSelectionAdapter(availableProducts);
        binding.recyclerViewAvailableProducts.setAdapter(productSelectionAdapter);

        loadAvailableProducts();

        binding.btnConfirmSelection.setOnClickListener(v -> confirmSelection());
    }

    private void loadAvailableProducts() {
        mDatabaseProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableProducts.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Producto producto = productSnapshot.getValue(Producto.class);
                    if (producto != null) {
                        producto.setId(productSnapshot.getKey());
                        availableProducts.add(producto);
                    }
                }
                productSelectionAdapter.setProductList(availableProducts); // Actualizar el adaptador
                Log.d(TAG, "Productos disponibles cargados: " + availableProducts.size());

                if (availableProducts.isEmpty()) {
                    binding.tvNoAvailableProducts.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoAvailableProducts.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar productos disponibles: " + error.getMessage());
                Toast.makeText(ProductSelectionActivity.this, "Error al cargar productos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmSelection() {
        List<Pedido.PedidoItem> selectedItems = productSelectionAdapter.getSelectedProducts();

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione al menos un producto.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedProductsResult", (Serializable) selectedItems); // Pasar la lista como Serializable
        setResult(RESULT_OK, resultIntent);
        finish(); // Cierra esta actividad y regresa a PedidoFormActivity
    }
}