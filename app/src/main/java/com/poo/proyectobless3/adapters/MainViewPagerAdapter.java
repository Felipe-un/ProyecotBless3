package com.poo.proyectobless3.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.poo.proyectobless3.fragments.ComentariosFragment;
import com.poo.proyectobless3.fragments.InventarioFragment;
import com.poo.proyectobless3.fragments.PedidosFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    private String userRole; // Para pasar el rol a los fragments

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String role) {
        super(fragmentActivity);
        this.userRole = role;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Pasar el rol al InventarioFragment
                InventarioFragment inventarioFragment = new InventarioFragment();
                Bundle argsInventario = new Bundle();
                argsInventario.putString("userRole", userRole);
                inventarioFragment.setArguments(argsInventario);
                return inventarioFragment;
            case 1:
                // Pasar el rol al PedidosFragment
                PedidosFragment pedidosFragment = new PedidosFragment();
                Bundle argsPedidos = new Bundle();
                argsPedidos.putString("userRole", userRole);
                pedidosFragment.setArguments(argsPedidos);
                return pedidosFragment;
            case 2:
                // Pasar el rol al ComentariosFragment
                ComentariosFragment comentariosFragment = new ComentariosFragment();
                Bundle argsComentarios = new Bundle();
                argsComentarios.putString("userRole", userRole);
                comentariosFragment.setArguments(argsComentarios);
                return comentariosFragment;
            default:
                // Por defecto, mostrar el fragmento de Inventario
                return new InventarioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tenemos 3 pestañas/fragmentos: Inventario, Pedidos, Comentarios
    }
}