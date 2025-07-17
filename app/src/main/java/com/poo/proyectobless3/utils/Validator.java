package com.poo.proyectobless3.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class Validator {

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPassword(String password) {
        // La lógica de contraseña puede ser más compleja (mínimo 6 caracteres)
        return password != null && password.length() >= 6;
    }

    // Metodos de validación adicionales
    public static boolean isFieldEmpty(String text) {
        return TextUtils.isEmpty(text);
    }

    public static boolean isValidPhone(CharSequence target) {
        // Puedes usar Patterns.PHONE para una validación más estricta si es necesario
        // Para una validación básica, solo verificamos que no esté vacío y tenga una longitud mínima
        return !TextUtils.isEmpty(target) && target.length() >= 7; // Mínimo 7 dígitos, puedes ajustar
    }
}