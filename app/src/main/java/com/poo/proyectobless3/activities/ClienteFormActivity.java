package com.poo.proyectobless3.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.poo.proyectobless3.databinding.ActivityClienteFormBinding;
import com.poo.proyectobless3.models.Cliente;
import com.poo.proyectobless3.utils.Validator; // Asegúrate de que esta clase exista y tenga validaciones de email/teléfono

import java.util.Objects;

public class ClienteFormActivity extends AppCompatActivity {

    private static final String TAG = "ClienteFormActivity";
    private ActivityClienteFormBinding binding;
    private DatabaseReference mDatabaseClientes;
    private String clienteId = null; // Guardará el ID del cliente si estamos editando

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabaseClientes = FirebaseDatabase.getInstance().getReference("clientes");

        // Comprobar si se está editando un cliente existente
        if (getIntent().hasExtra("clienteId")) {
            clienteId = getIntent().getStringExtra("clienteId");
            binding.tvClienteFormTitle.setText("Editar Cliente");
            loadClienteData(clienteId); // Cargar los datos del cliente para editar
        } else {
            binding.tvClienteFormTitle.setText("Registrar Cliente");
        }

        binding.btnGuardarCliente.setOnClickListener(view -> saveCliente());
        binding.btnCancelarCliente.setOnClickListener(view -> finish()); // Simplemente cierra la actividad
    }

    private void loadClienteData(String id) {
        mDatabaseClientes.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cliente cliente = snapshot.getValue(Cliente.class);
                if (cliente != null) {
                    binding.etClienteNombre.setText(cliente.getNombre());
                    binding.etClienteEmail.setText(cliente.getEmail());
                    binding.etClienteTelefono.setText(cliente.getTelefono());
                } else {
                    Toast.makeText(ClienteFormActivity.this, "Cliente no encontrado.", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad si el cliente no existe
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar datos del cliente: " + error.getMessage());
                Toast.makeText(ClienteFormActivity.this, "Error al cargar cliente: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveCliente() {
        String nombre = binding.etClienteNombre.getText().toString().trim();
        String email = binding.etClienteEmail.getText().toString().trim();
        String telefono = binding.etClienteTelefono.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(telefono)) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Validator.isValidEmail(email)) {
            binding.etClienteEmail.setError("Email inválido.");
            return;
        }
        // Puedes añadir una validación más robusta para el teléfono si es necesario
        if (!Validator.isValidPhone(telefono)) { // Usando el nuevo método del validador
            binding.etClienteTelefono.setError("Número de teléfono inválido.");
            return;
        }

        Cliente cliente = new Cliente(clienteId, nombre, email, telefono);

        if (clienteId == null) {
            // Nuevo cliente: Generar una nueva clave en Firebase
            String newClienteId = mDatabaseClientes.push().getKey();
            if (newClienteId != null) {
                cliente.setId(newClienteId);
                mDatabaseClientes.child(newClienteId).setValue(cliente)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ClienteFormActivity.this, "Cliente registrado exitosamente.", Toast.LENGTH_SHORT).show();
                                finish(); // Cerrar la actividad
                            } else {
                                Log.e(TAG, "Error al registrar cliente: " + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(ClienteFormActivity.this, "Error al registrar cliente: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(ClienteFormActivity.this, "Error al generar ID de cliente.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Editar cliente existente
            mDatabaseClientes.child(clienteId).setValue(cliente)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ClienteFormActivity.this, "Cliente actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                            finish(); // Cerrar la actividad
                        } else {
                            Log.e(TAG, "Error al actualizar cliente: " + Objects.requireNonNull(task.getException()).getMessage());
                            Toast.makeText(ClienteFormActivity.this, "Error al actualizar cliente: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}