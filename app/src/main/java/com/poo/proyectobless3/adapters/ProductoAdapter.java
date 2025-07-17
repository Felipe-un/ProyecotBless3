package com.poo.proyectobless3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Necesitarás añadir esta dependencia para cargar imágenes
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.models.Producto;

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> productoList;
    private String userRole; // Rol del usuario actual
    private OnItemActionListener listener; // Interfaz para acciones de click

    // Interfaz para manejar eventos de los botones (editar/eliminar)
    public interface OnItemActionListener {
        void onEditClick(Producto producto);
        void onDeleteClick(Producto producto);
    }

    public ProductoAdapter(List<Producto> productoList, String userRole, OnItemActionListener listener) {
        this.productoList = productoList;
        this.userRole = userRole;
        this.listener = listener;
    }

    // Metodo para actualizar la lista de productos (en caso de cambios)
    public void setProductoList(List<Producto> newProductoList) {
        this.productoList = newProductoList;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productoList.get(position);
        holder.tvProductName.setText(producto.getNombre());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
        holder.tvProductStock.setText(String.format(Locale.getDefault(), "Stock: %d unidades", producto.getStock()));

        // Cargar imagen usando Glide (necesita la dependencia)
        if (producto.getImagenURL() != null && !producto.getImagenURL().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(producto.getImagenURL())
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder si la imagen no carga
                    .error(R.drawable.ic_launcher_background) // Imagen de error
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_background); // Imagen por defecto
        }

        // Mostrar u ocultar botones de acción según el rol del usuario
        if (userRole != null && userRole.equals("admin")) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.btnEditProduct.setOnClickListener(v -> listener.onEditClick(producto));
            holder.btnDeleteProduct.setOnClickListener(v -> listener.onDeleteClick(producto));
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductStock;
        LinearLayout layoutAdminActions;
        Button btnEditProduct;
        Button btnDeleteProduct;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductStock = itemView.findViewById(R.id.tv_product_stock);
            layoutAdminActions = itemView.findViewById(R.id.layout_admin_actions);
            btnEditProduct = itemView.findViewById(R.id.btn_edit_product);
            btnDeleteProduct = itemView.findViewById(R.id.btn_delete_product);
        }
    }
}