package com.poo.proyectobless3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

// Importar la clase de binding generada automáticamente
import com.poo.proyectobless3.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 segundos
    private ActivitySplashBinding binding; // Declarar una variable para el binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar el layout usando View Binding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Establecer la vista raíz del binding

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Después de SPLASH_TIME_OUT segundos, se inicia LoginActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Cierra SplashActivity para que el usuario no pueda volver a ella
            }
        }, SPLASH_TIME_OUT);
    }
}