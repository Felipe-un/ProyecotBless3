package com.poo.proyectobless3.models;

import java.io.Serializable; // Importar Serializable
import java.util.List;
import java.util.Map;

public class Pedido {
    public String id;
    public String clienteId;
    public String estado;
    public long fechaPedido;
    public Map<String, PedidoItem> productos;

    public Pedido() {
        // Constructor vacío requerido para Firebase Realtime Database
    }

    public Pedido(String id, String clienteId, String estado, long fechaPedido, Map<String, PedidoItem> productos) {
        this.id = id;
        this.clienteId = clienteId;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.productos = productos;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(long fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public Map<String, PedidoItem> getProductos() {
        return productos;
    }

    public void setProductos(Map<String, PedidoItem> productos) {
        this.productos = productos;
    }


    // Clase interna para representar un item dentro del pedido
    public static class PedidoItem implements Serializable { // AÑADIR "implements Serializable"
        public String idProducto;
        public String nombreProducto;
        public int cantidad;
        public double precioUnitario;

        public PedidoItem() {
            // Constructor vacío requerido para Firebase
        }

        public PedidoItem(String idProducto, String nombreProducto, int cantidad, double precioUnitario) {
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        // Getters y Setters para PedidoItem
        public String getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(String idProducto) {
            this.idProducto = idProducto;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public void setNombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public double getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(double precioUnitario) {
            this.precioUnitario = precioUnitario;
        }
    }
}