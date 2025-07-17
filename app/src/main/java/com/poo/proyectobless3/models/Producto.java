package com.poo.proyectobless3.models;

public class Producto {
    // Los campos deben coincidir con los nombres de las propiedades en Firebase Realtime Database
    public String id; // Para almacenar la clave generada por push()
    public String nombre;
    public int stock;
    public String imagenURL;
    public double precio;
    public String categoria; // Añadimos categoría según el documento de proyecto
    public String descripcion; // Añadimos descripción si es relevante

    public Producto() {
        // Constructor vacío requerido para Firebase Realtime Database
    }

    public Producto(String id, String nombre, int stock, String imagenURL, double precio, String categoria, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.imagenURL = imagenURL;
        this.precio = precio;
        this.categoria = categoria;
        this.descripcion = descripcion;
    }

    // Getters y Setters (necesarios para Firebase y para acceder a los datos)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}