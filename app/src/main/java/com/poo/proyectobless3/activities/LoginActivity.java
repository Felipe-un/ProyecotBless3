package com.poo.proyectobless3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Importar la clase de binding generada automáticamente
import com.poo.proyectobless3.databinding.ActivityLoginBinding;
import com.poo.proyectobless3.utils.Validator;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ActivityLoginBinding binding; // Declarar una variable para el binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar el layout usando View Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Establecer la vista raíz del binding

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        // Listener para el botón de login usando el binding
        binding.btnLogin.setOnClickListener(view -> loginUser());

        // Listener para el texto de registro usando el binding
        // binding.tvRegisterPrompt.setOnClickListener(view -> {
        //    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        //    startActivity(intent);
        //});
    }

    private void loginUser() {
        // Acceder a los EditText a través del objeto binding
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Por favor, ingrese email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Validator.isValidEmail(email)) {
            binding.etEmail.setError("Email inválido.");
            return;
        }
        // Este if es para definir cual es el mínimo de caracteres en la contarseña
        // if (!Validator.isValidPassword(password)) {
        //     binding.etPassword.setError("Contraseña debe tener al menos 6 caracteres.");
        //     return;
        // }


        // Autenticación con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserRole(user.getUid());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Autenticación fallida: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserRole(String uid) {
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("rol").getValue(String.class);
                    if (role != null) {
                        // Aquí puedes guardar el rol en SharedPreferences
                        // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        // intent.putExtra("userRole", role);
                        // startActivity(intent);
                        // finish();

                        // Por ahora, solo mostramos el rol y redirigimos.
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso. Rol: " + role, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userRole", role); // Pasa el rol a MainActivity
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Rol de usuario no definido.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Datos de usuario no encontrados en Realtime Database.", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error al leer datos del usuario: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser.getUid());
        }
    }
}