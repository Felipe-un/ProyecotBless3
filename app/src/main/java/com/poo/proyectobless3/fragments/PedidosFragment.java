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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.activities.ClienteFormActivity;
import com.poo.proyectobless3.activities.ClientePedidosActivity;
import com.poo.proyectobless3.activities.PedidoFormActivity;
import com.poo.proyectobless3.adapters.PedidoAdapter;
import com.poo.proyectobless3.databinding.FragmentPedidosBinding;
import com.poo.proyectobless3.models.Cliente;
import com.poo.proyectobless3.models.Pedido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidosFragment extends Fragment implements PedidoAdapter.OnItemClickListener {

    private static final String TAG = "PedidosFragment";
    private FragmentPedidosBinding binding;
    private RecyclerView recyclerView;
    private PedidoAdapter pedidoAdapter;
    private List<Pedido> pedidoList;
    private Map<String, Cliente> clienteMap;
    private DatabaseReference mDatabasePedidos;
    private DatabaseReference mDatabaseClientes;
    private String userRole;

    public PedidosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (getArguments() != null) {
            userRole = getArguments().getString("userRole");
            Log.d(TAG, "Rol de usuario en PedidosFragment: " + userRole);
        }

        mDatabasePedidos = FirebaseDatabase.getInstance().getReference("pedidos");
        mDatabaseClientes = FirebaseDatabase.getInstance().getReference("clientes");

        pedidoList = new ArrayList<>();
        clienteMap = new HashMap<>();
        pedidoAdapter = new PedidoAdapter(pedidoList, clienteMap, userRole, this); // Pasar userRole al adaptador
        recyclerView = binding.recyclerViewPedidos;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(pedidoAdapter);

        loadClientesAndPedidos();

        if (userRole != null && userRole.equals("admin")) {
            binding.fabAddCliente.setVisibility(View.VISIBLE);
            binding.fabAddCliente.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ClienteFormActivity.class);
                startActivity(intent);
            });

            binding.fabAddPedido.setVisibility(View.VISIBLE);
            binding.fabAddPedido.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), PedidoFormActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fabAddCliente.setVisibility(View.GONE);
            binding.fabAddPedido.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadClientesAndPedidos() {
        mDatabaseClientes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clienteMap.clear();
                for (DataSnapshot clientSnapshot : snapshot.getChildren()) {
                    Cliente cliente = clientSnapshot.getValue(Cliente.class);
                    if (cliente != null) {
                        cliente.setId(clientSnapshot.getKey());
                        clienteMap.put(cliente.getId(), cliente);
                    }
                }
                pedidoAdapter.setClienteMap(clienteMap);
                Log.d(TAG, "Clientes cargados: " + clienteMap.size());
                loadPedidos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar clientes: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar clientes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPedidos() {
        mDatabasePedidos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pedidoList.clear();
                for (DataSnapshot pedidoSnapshot : snapshot.getChildren()) {
                    Pedido pedido = pedidoSnapshot.getValue(Pedido.class);
                    if (pedido != null) {
                        pedido.setId(pedidoSnapshot.getKey());
                        pedidoList.add(pedido);
                    }
                }
                pedidoAdapter.setPedidoList(pedidoList);
                Log.d(TAG, "Pedidos cargados: " + pedidoList.size());
                if (pedidoList.isEmpty()) {
                    binding.tvNoPedidos.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoPedidos.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar pedidos: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetailsClick(Pedido pedido) {
        Toast.makeText(getContext(), "Detalles del pedido ID: " + pedido.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewClientOrdersClick(String clienteId) {
        Intent intent = new Intent(getContext(), ClientePedidosActivity.class);
        intent.putExtra("clienteId", clienteId);
        intent.putExtra("userRole", userRole); // Pasa el rol del usuario
        startActivity(intent);
    }

    @Override
    public void onEditStatusClick(Pedido pedido) {
        String[] statusOptions = getResources().getStringArray(R.array.order_status_array);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Actualizar Estado del Pedido");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_order_status, null);
        Spinner statusSpinner = dialogView.findViewById(R.id.spinner_dialog_order_status);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        int currentStatusPosition = -1;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(pedido.getEstado())) {
                currentStatusPosition = i;
                break;
            }
        }
        if (currentStatusPosition != -1) {
            statusSpinner.setSelection(currentStatusPosition);
        }

        builder.setView(dialogView);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            updatePedidoStatus(pedido, newStatus);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDeleteOrderClick(Pedido pedido) {
        // Confirmación antes de eliminar
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este pedido?")
                .setPositiveButton("Eliminar", (dialog, which) -> deletePedido(pedido))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updatePedidoStatus(Pedido pedido, String newStatus) {
        if (pedido.getId() == null || pedido.getClienteId() == null) {
            Toast.makeText(getContext(), "Error: ID de pedido o cliente no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", newStatus);

        mDatabasePedidos.child(pedido.getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    mDatabaseClientes.child(pedido.getClienteId()).child("pedidos").child(pedido.getId()).updateChildren(updates)
                            .addOnSuccessListener(bVoid -> {
                                Toast.makeText(getContext(), "Estado del pedido actualizado.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al actualizar estado en colección de cliente: " + e.getMessage());
                                Toast.makeText(getContext(), "Error al actualizar estado en cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar estado en pedido global: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deletePedido(Pedido pedido) {
        if (pedido.getId() == null || pedido.getClienteId() == null) {
            Toast.makeText(getContext(), "Error: ID de pedido o cliente no válido para eliminar.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference mDatabasePedidosGlobal = FirebaseDatabase.getInstance().getReference("pedidos"); // Obtener referencia global aquí

        // 1. Eliminar de la colección global de pedidos
        mDatabasePedidosGlobal.child(pedido.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // 2. Eliminar también de la colección del cliente
                    mDatabaseClientes.child(pedido.getClienteId()).child("pedidos").child(pedido.getId()).removeValue()
                            .addOnSuccessListener(bVoid -> {
                                Toast.makeText(getContext(), "Pedido eliminado exitosamente.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al eliminar pedido de la colección del cliente: " + e.getMessage());
                                Toast.makeText(getContext(), "Error al eliminar de cliente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar pedido de la colección global: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al eliminar pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}