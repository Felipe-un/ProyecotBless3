package com.poo.proyectobless3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.adapters.PedidoProductoSeleccionadoAdapter;
import com.poo.proyectobless3.databinding.ActivityPedidoFormBinding;
import com.poo.proyectobless3.models.Cliente;
import com.poo.proyectobless3.models.Pedido;
import com.poo.proyectobless3.models.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PedidoFormActivity extends AppCompatActivity implements PedidoProductoSeleccionadoAdapter.OnItemRemoveListener {

    private static final String TAG = "PedidoFormActivity";
    private ActivityPedidoFormBinding binding;
    private DatabaseReference mDatabasePedidos;
    private DatabaseReference mDatabaseClientes;
    private DatabaseReference mDatabaseProductos;

    private List<Cliente> clientesList;
    private Map<String, Cliente> clientesMap; // Para un acceso rápido por ID
    private Cliente selectedClient;

    private List<Producto> availableProducts; // Productos disponibles en el inventario
    private List<Pedido.PedidoItem> selectedProductsInOrder; // Productos que se añadirán a este pedido

    private PedidoProductoSeleccionadoAdapter selectedProductsAdapter;

    private String pedidoId = null; // Para modo edición de pedido

    // Request code para la actividad de selección de productos
    private static final int REQUEST_CODE_SELECT_PRODUCTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPedidoFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabasePedidos = FirebaseDatabase.getInstance().getReference("pedidos");
        mDatabaseClientes = FirebaseDatabase.getInstance().getReference("clientes");
        mDatabaseProductos = FirebaseDatabase.getInstance().getReference("productos");

        clientesList = new ArrayList<>();
        clientesMap = new HashMap<>();
        availableProducts = new ArrayList<>();
        selectedProductsInOrder = new ArrayList<>();

        // Configurar RecyclerView para productos seleccionados
        binding.recyclerViewSelectedProducts.setLayoutManager(new LinearLayoutManager(this));
        selectedProductsAdapter = new PedidoProductoSeleccionadoAdapter(selectedProductsInOrder, this);
        binding.recyclerViewSelectedProducts.setAdapter(selectedProductsAdapter);

        // Configurar Spinner de estados de pedido
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.order_status_array,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerOrderStatus.setAdapter(statusAdapter);

        // Cargar clientes y productos al iniciar la actividad
        loadClientes();
        loadAvailableProducts();

        // Comprobar si es modo edición
        if (getIntent().hasExtra("pedidoId")) {
            pedidoId = getIntent().getStringExtra("pedidoId");
            binding.tvPedidoFormTitle.setText("Editar Pedido");
            loadPedidoDataForEdit(pedidoId);
        } else {
            binding.tvPedidoFormTitle.setText("Crear Nuevo Pedido");
            updateNoProductsSelectedVisibility();
        }

        binding.btnGuardarPedido.setOnClickListener(v -> savePedido());
        binding.btnCancelarPedido.setOnClickListener(v -> finish());
        binding.btnAddProducts.setOnClickListener(v -> openProductSelection());
    }

    private void loadClientes() {
        mDatabaseClientes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clientesList.clear();
                clientesMap.clear();
                List<String> clientNames = new ArrayList<>();
                clientNames.add("Seleccione un cliente"); // Opción por defecto

                for (DataSnapshot clientSnapshot : snapshot.getChildren()) {
                    Cliente cliente = clientSnapshot.getValue(Cliente.class);
                    if (cliente != null) {
                        cliente.setId(clientSnapshot.getKey());
                        clientesList.add(cliente);
                        clientesMap.put(cliente.getId(), cliente);
                        clientNames.add(cliente.getNombre() + " (" + cliente.getEmail() + ")");
                    }
                }

                ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(
                        PedidoFormActivity.this,
                        android.R.layout.simple_spinner_item,
                        clientNames
                );
                clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerClientes.setAdapter(clientAdapter);

                binding.spinnerClientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) { // Evitar la opción "Seleccione un cliente"
                            selectedClient = clientesList.get(position - 1);
                            Log.d(TAG, "Cliente seleccionado: " + selectedClient.getNombre());
                        } else {
                            selectedClient = null;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedClient = null;
                    }
                });

                // Si estamos en modo edición y ya tenemos un cliente seleccionado, establecerlo en el spinner
                if (pedidoId != null && selectedClient != null) {
                    int clientPosition = -1;
                    for (int i = 0; i < clientesList.size(); i++) {
                        if (clientesList.get(i).getId().equals(selectedClient.getId())) {
                            clientPosition = i + 1; // +1 por la opción por defecto
                            break;
                        }
                    }
                    if (clientPosition != -1) {
                        binding.spinnerClientes.setSelection(clientPosition);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar clientes: " + error.getMessage());
                Toast.makeText(PedidoFormActivity.this, "Error al cargar clientes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                Log.d(TAG, "Productos disponibles cargados: " + availableProducts.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar productos disponibles: " + error.getMessage());
                Toast.makeText(PedidoFormActivity.this, "Error al cargar productos disponibles: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPedidoDataForEdit(String id) {
        mDatabasePedidos.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Pedido pedido = snapshot.getValue(Pedido.class);
                if (pedido != null) {
                    // Cargar cliente
                    if (pedido.getClienteId() != null && clientesMap.containsKey(pedido.getClienteId())) {
                        selectedClient = clientesMap.get(pedido.getClienteId());
                        // Actualizar spinner de clientes después de que los clientes se hayan cargado
                        // Esto se manejará en el listener de loadClientes()
                    }

                    // Cargar estado
                    ArrayAdapter<CharSequence> statusAdapter = (ArrayAdapter<CharSequence>) binding.spinnerOrderStatus.getAdapter();
                    if (statusAdapter != null) {
                        int statusPosition = statusAdapter.getPosition(pedido.getEstado());
                        binding.spinnerOrderStatus.setSelection(statusPosition);
                    }

                    // Cargar productos del pedido
                    selectedProductsInOrder.clear();
                    if (pedido.getProductos() != null) {
                        for (Map.Entry<String, Pedido.PedidoItem> entry : pedido.getProductos().entrySet()) {
                            selectedProductsInOrder.add(entry.getValue());
                        }
                    }
                    selectedProductsAdapter.setSelectedProducts(selectedProductsInOrder);
                    updateNoProductsSelectedVisibility();

                } else {
                    Toast.makeText(PedidoFormActivity.this, "Pedido no encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar datos del pedido para edición: " + error.getMessage());
                Toast.makeText(PedidoFormActivity.this, "Error al cargar pedido: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void savePedido() {
        if (selectedClient == null) {
            Toast.makeText(this, "Por favor, seleccione un cliente.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedProductsInOrder.isEmpty()) {
            Toast.makeText(this, "Por favor, añada al menos un producto al pedido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = binding.spinnerOrderStatus.getSelectedItem().toString();
        long timestamp = System.currentTimeMillis();

        // Convertir la lista de PedidoItem a un Map<String, PedidoItem> para Firebase
        Map<String, Pedido.PedidoItem> productsMap = new HashMap<>();
        for (Pedido.PedidoItem item : selectedProductsInOrder) {
            productsMap.put(item.getIdProducto(), item); // Usar el ID del producto como clave
        }

        Pedido pedido = new Pedido(
                pedidoId,
                selectedClient.getId(),
                status,
                timestamp,
                productsMap
        );

        if (pedidoId == null) {
            // Nuevo pedido
            String newPedidoId = mDatabasePedidos.push().getKey();
            if (newPedidoId != null) {
                pedido.setId(newPedidoId);
                // Guardar en la colección global de pedidos
                mDatabasePedidos.child(newPedidoId).setValue(pedido)
                        .addOnSuccessListener(aVoid -> {
                            // Guardar también en la colección del cliente
                            mDatabaseClientes.child(selectedClient.getId()).child("pedidos").child(newPedidoId).setValue(pedido)
                                    .addOnSuccessListener(bVoid -> {
                                        Toast.makeText(PedidoFormActivity.this, "Pedido creado exitosamente.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al guardar pedido en colección de cliente: " + e.getMessage());
                                        Toast.makeText(PedidoFormActivity.this, "Error al guardar pedido en cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al crear pedido global: " + e.getMessage());
                            Toast.makeText(PedidoFormActivity.this, "Error al crear pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "Error al generar ID de pedido.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Editar pedido existente
            // Actualizar en la colección global de pedidos
            mDatabasePedidos.child(pedidoId).setValue(pedido)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar también en la colección del cliente
                        mDatabaseClientes.child(selectedClient.getId()).child("pedidos").child(pedidoId).setValue(pedido)
                                .addOnSuccessListener(bVoid -> {
                                    Toast.makeText(PedidoFormActivity.this, "Pedido actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al actualizar pedido en colección de cliente: " + e.getMessage());
                                    Toast.makeText(PedidoFormActivity.this, "Error al actualizar pedido en cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar pedido global: " + e.getMessage());
                        Toast.makeText(PedidoFormActivity.this, "Error al actualizar pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void updateNoProductsSelectedVisibility() {
        if (selectedProductsInOrder.isEmpty()) {
            binding.tvNoProductsSelected.setVisibility(View.VISIBLE);
            binding.recyclerViewSelectedProducts.setVisibility(View.GONE);
        } else {
            binding.tvNoProductsSelected.setVisibility(View.GONE);
            binding.recyclerViewSelectedProducts.setVisibility(View.VISIBLE);
        }
    }

    // Interfaz de PedidoProductoSeleccionadoAdapter.OnItemRemoveListener
    @Override
    public void onRemoveItem(int position) {
        selectedProductsInOrder.remove(position);
        selectedProductsAdapter.notifyItemRemoved(position);
        updateNoProductsSelectedVisibility();
        Toast.makeText(this, "Producto eliminado del pedido.", Toast.LENGTH_SHORT).show();
    }

    // --- Lógica para abrir la selección de productos (próximo paso) ---
    private void openProductSelection() {
        // Aquí lanzaremos una nueva actividad o un diálogo para seleccionar productos
        // Por ahora, solo un Toast
        Toast.makeText(this, "Abriendo selección de productos...", Toast.LENGTH_SHORT).show();
        // Intent para lanzar ProductSelectionActivity
        Intent intent = new Intent(this, ProductSelectionActivity.class);
        // Puedes pasarle los productos ya seleccionados si quieres que aparezcan pre-seleccionados
        // intent.putExtra("selectedProducts", new ArrayList<>(selectedProductsInOrder)); // Necesitarían ser Parcelable/Serializable
        startActivityForResult(intent, REQUEST_CODE_SELECT_PRODUCTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PRODUCTS && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedProductsResult")) {
                // Recuperar la lista de productos seleccionados
                ArrayList<Pedido.PedidoItem> newSelectedItems = (ArrayList<Pedido.PedidoItem>) data.getSerializableExtra("selectedProductsResult");
                if (newSelectedItems != null) {
                    selectedProductsInOrder.clear();
                    selectedProductsInOrder.addAll(newSelectedItems);
                    selectedProductsAdapter.setSelectedProducts(selectedProductsInOrder); // Actualizar el adaptador
                    updateNoProductsSelectedVisibility();
                    Toast.makeText(this, "Productos añadidos al pedido.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}