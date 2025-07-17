package com.poo.proyectobless3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.adapters.MainViewPagerAdapter;
import com.poo.proyectobless3.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;
    private String userRole; // Variable para almacenar el rol del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Obtener el rol del usuario pasado desde LoginActivity
        userRole = getIntent().getStringExtra("userRole");
        if (userRole == null) {
            // Si por alguna razón el rol no se pasó (ej. se reinicio la app),
            // se intentará obtener del usuario actual de Firebase
            if (mAuth.getCurrentUser() != null) {
                // Si no se obtiene el rol se usará el rol de empleado
                userRole = "empleado"; // Por defecto, si no se obtiene, asumir el rol más restrictivo
            } else {
                // No debería pasar si ya está logueado, pero por seguridad
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Proyecto Bless Pruebas");
        }

        // Configurar ViewPager2 y TabLayout
        MainViewPagerAdapter viewPagerAdapter = new MainViewPagerAdapter(this, userRole);
        binding.viewPager.setAdapter(viewPagerAdapter);

        // Nombres para las pestañas
        String[] tabTitles = new String[]{"Inventario", "Pedidos", "Comentarios"};

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        // Eliminar los botones de prueba y TextViews ya que los fragments tomarán su lugar.
        // La lógica del botón de registro y logout se moverá al menú de la Toolbar.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú de la Toolbar
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Mostrar/ocultar el botón de registro de usuarios basado en el rol
        MenuItem registerItem = menu.findItem(R.id.action_register_user);
        if (registerItem != null) {
            if (userRole != null && userRole.equals("admin")) {
                registerItem.setVisible(true);
            } else {
                registerItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_register_user) {
            // Solo accesible si el rol es 'admin' (ya controlado por setVisible)
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}