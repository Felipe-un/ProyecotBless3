package com.poo.proyectobless3.models;

public class Comentario {
    public String id; // Opcional, para la clave de Firebase si la necesitas
    public String idProducto; // ID del producto al que se asocia el comentario (si el forms lo captura)
    public String texto;
    public long fecha; // Timestamp en milisegundos
    public int calificacion; // Calificación de 1 a 5
    public String categoria; // Categoría del producto (camisa, camiseta, etc.)

    public Comentario() {
        // Constructor vacío requerido para Firebase
    }

    public Comentario(String id, String idProducto, String texto, long fecha, int calificacion, String categoria) {
        this.id = id;
        this.idProducto = idProducto;
        this.texto = texto;
        this.fecha = fecha;
        this.calificacion = calificacion;
        this.categoria = categoria;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}