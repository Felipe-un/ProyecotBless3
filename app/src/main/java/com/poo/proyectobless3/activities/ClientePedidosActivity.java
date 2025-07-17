package com.poo.proyectobless3.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder; // Importar MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.adapters.PedidoAdapter; // Reutilizamos el PedidoAdapter
import com.poo.proyectobless3.databinding.ActivityClientePedidosBinding;
import com.poo.proyectobless3.models.Cliente;
import com.poo.proyectobless3.models.Pedido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientePedidosActivity extends AppCompatActivity implements PedidoAdapter.OnItemClickListener {

    private static final String TAG = "ClientePedidosActivity";
    private ActivityClientePedidosBinding binding;
    private DatabaseReference mDatabaseClientes;
    private DatabaseReference mDatabasePedidosGlobal; // Referencia a la colección global de pedidos
    private String clienteId;
    private String userRole; // Para controlar la visibilidad del botón de eliminar

    private RecyclerView recyclerView;
    private PedidoAdapter pedidoAdapter;
    private List<Pedido> pedidoList;
    private Map<String, Cliente> clienteMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientePedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener el ID del cliente del Intent
        clienteId = getIntent().getStringExtra("clienteId");
        if (clienteId == null) {
            Toast.makeText(this, "ID de cliente no proporcionado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener el rol del usuario actual (necesario para el adaptador)
        // Esto asume que el rol se pasa desde MainActivity o se obtiene de FirebaseAuth/RealtimeDB
        // Para simplificar, lo pasaremos desde MainActivity a PedidosFragment, y de ahí aquí.
        userRole = getIntent().getStringExtra("userRole");
        if (userRole == null) {
            // Si no se pasa el rol, asumimos un rol no-admin para seguridad por defecto
            userRole = "empleado";
            Log.w(TAG, "Rol de usuario no proporcionado a ClientePedidosActivity. Asumiendo 'empleado'.");
        }


        mDatabaseClientes = FirebaseDatabase.getInstance().getReference("clientes");
        mDatabasePedidosGlobal = FirebaseDatabase.getInstance().getReference("pedidos"); // Inicializar referencia global

        recyclerView = binding.recyclerViewClientOrders;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pedidoList = new ArrayList<>();
        clienteMap = new HashMap<>();
        pedidoAdapter = new PedidoAdapter(pedidoList, clienteMap, userRole, this); // Pasamos userRole al adaptador
        recyclerView.setAdapter(pedidoAdapter);

        loadClienteDetailsAndOrders();
    }

    private void loadClienteDetailsAndOrders() {
        mDatabaseClientes.child(clienteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cliente cliente = snapshot.getValue(Cliente.class);
                if (cliente != null) {
                    cliente.setId(snapshot.getKey());
                    binding.tvClientNameHeader.setText(String.format("Pedidos de: %s", cliente.getNombre()));
                    clienteMap.put(cliente.getId(), cliente);
                    pedidoAdapter.setClienteMap(clienteMap);
                    loadClientOrders(cliente.getId());
                } else {
                    Toast.makeText(ClientePedidosActivity.this, "Cliente no encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar detalles del cliente: " + error.getMessage());
                Toast.makeText(ClientePedidosActivity.this, "Error al cargar cliente: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadClientOrders(String idCliente) {
        DatabaseReference clientOrdersRef = mDatabaseClientes.child(idCliente).child("pedidos");

        clientOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pedidoList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Pedido pedido = orderSnapshot.getValue(Pedido.class);
                    if (pedido != null) {
                        pedido.setId(orderSnapshot.getKey());
                        pedidoList.add(pedido);
                    }
                }
                pedidoAdapter.setPedidoList(pedidoList);
                Log.d(TAG, "Pedidos cargados para cliente " + idCliente + ": " + pedidoList.size());

                if (pedidoList.isEmpty()) {
                    binding.tvNoClientOrders.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    binding.tvNoClientOrders.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar pedidos del cliente: " + error.getMessage());
                Toast.makeText(ClientePedidosActivity.this, "Error al cargar pedidos del cliente: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetailsClick(Pedido pedido) {
        Toast.makeText(this, "Detalles del pedido: " + pedido.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewClientOrdersClick(String clienteId) {
        Log.w(TAG, "onViewClientOrdersClick llamado en ClientePedidosActivity. Ignorando.");
    }

    @Override
    public void onEditStatusClick(Pedido pedido) {
        Toast.makeText(this, "La edición de estado se realiza en la vista principal de Pedidos.", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Intento de editar estado en ClientePedidosActivity para pedido: " + pedido.getId());
    }

    @Override
    public void onDeleteOrderClick(Pedido pedido) {
        // Confirmación antes de eliminar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este pedido?")
                .setPositiveButton("Eliminar", (dialog, which) -> deletePedido(pedido))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deletePedido(Pedido pedido) {
        if (pedido.getId() == null || pedido.getClienteId() == null) {
            Toast.makeText(this, "Error: ID de pedido o cliente no válido para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference mDatabasePedidosGlobal = FirebaseDatabase.getInstance().getReference("pedidos"); // Obtener referencia global aquí

        // 1. Eliminar de la colección global de pedidos
        mDatabasePedidosGlobal.child(pedido.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 2. Eliminar también de la colección del cliente
                    mDatabaseClientes.child(pedido.getClienteId()).child("pedidos").child(pedido.getId()).removeValue()
                            .addOnSuccessListener(bVoid -> {
                                Toast.makeText(ClientePedidosActivity.this, "Pedido eliminado exitosamente.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al eliminar pedido de la colección del cliente: " + e.getMessage());
                                Toast.makeText(ClientePedidosActivity.this, "Error al eliminar de cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar pedido de la colección global: " + e.getMessage());
                    Toast.makeText(ClientePedidosActivity.this, "Error al eliminar pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() { // Cambiado de onDestroyView() a onDestroy()
        super.onDestroy();
        binding = null;
    }
}