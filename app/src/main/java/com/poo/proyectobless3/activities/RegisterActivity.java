package com.poo.proyectobless3.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.databinding.ActivityRegisterBinding;
import com.poo.proyectobless3.utils.Validator;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Referencia a la raíz para acceder a 'usuarios'

        // Configurar el Spinner de roles
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(adapter);

        binding.btnRegisterUser.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String name = binding.etRegisterName.getText().toString().trim();
        String email = binding.etRegisterEmail.getText().toString().trim();
        String password = binding.etRegisterPassword.getText().toString().trim();
        String role = binding.spinnerRole.getSelectedItem().toString();

        // Validaciones
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Validator.isValidEmail(email)) {
            binding.etRegisterEmail.setError("Email inválido.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            binding.etRegisterPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        Log.d(TAG, "Intentando registrar usuario: " + email + " con rol: " + role);

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Usuario creado en Authentication exitosamente.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 2. Guardar datos adicionales (nombre y rol) en Realtime Database
                                saveUserToDatabase(user.getUid(), name, email, role);
                            } else {
                                Log.e(TAG, "Usuario autenticado es null después de registrar.");
                                Toast.makeText(RegisterActivity.this, "Error: Usuario nulo después del registro.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Si falla la autenticación (ej. email ya registrado, contraseña débil)
                            Log.e(TAG, "Fallo al crear usuario en Authentication: " + task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email, String role) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("nombre", name);
        userMap.put("email", email);
        userMap.put("rol", role);

        mDatabase.child("usuarios").child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Datos de usuario guardados en Realtime Database.");
                        Toast.makeText(RegisterActivity.this, "Usuario registrado y datos guardados.", Toast.LENGTH_SHORT).show();
                        // Opcional: Redirigir de vuelta a MainActivity o LoginActivity
                        finish(); // Cierra esta actividad y regresa a la anterior (MainActivity si fue lanzada desde allí)
                    } else {
                        Log.e(TAG, "Fallo al guardar datos en Realtime Database: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Error al guardar datos del usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        // Considerar eliminar el usuario de Authentication si el guardado en DB falla
                        mAuth.getCurrentUser().delete();
                    }
                });
    }
}