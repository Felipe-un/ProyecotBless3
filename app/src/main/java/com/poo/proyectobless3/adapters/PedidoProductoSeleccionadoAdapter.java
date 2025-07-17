package com.poo.proyectobless3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poo.proyectobless3.R;
import com.poo.proyectobless3.models.Pedido; // Usamos Pedido.PedidoItem

import java.util.List;
import java.util.Locale;

public class PedidoProductoSeleccionadoAdapter extends RecyclerView.Adapter<PedidoProductoSeleccionadoAdapter.SelectedProductViewHolder> {

    private List<Pedido.PedidoItem> selectedProducts;
    private OnItemRemoveListener listener;

    public interface OnItemRemoveListener {
        void onRemoveItem(int position);
    }

    public PedidoProductoSeleccionadoAdapter(List<Pedido.PedidoItem> selectedProducts, OnItemRemoveListener listener) {
        this.selectedProducts = selectedProducts;
        this.listener = listener;
    }

    public void setSelectedProducts(List<Pedido.PedidoItem> newSelectedProducts) {
        this.selectedProducts = newSelectedProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido_producto_seleccionado, parent, false);
        return new SelectedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedProductViewHolder holder, int position) {
        Pedido.PedidoItem item = selectedProducts.get(position);
        holder.tvSelectedProductName.setText(String.format(Locale.getDefault(), "%s (x%d)", item.getNombreProducto(), item.getCantidad()));
        holder.tvSelectedProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getCantidad() * item.getPrecioUnitario()));

        holder.btnRemoveProduct.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedProducts.size();
    }

    public static class SelectedProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvSelectedProductName, tvSelectedProductPrice;
        ImageButton btnRemoveProduct;

        public SelectedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSelectedProductName = itemView.findViewById(R.id.tv_selected_product_name);
            tvSelectedProductPrice = itemView.findViewById(R.id.tv_selected_product_price);
            btnRemoveProduct = itemView.findViewById(R.id.btn_remove_product);
        }
    }
}