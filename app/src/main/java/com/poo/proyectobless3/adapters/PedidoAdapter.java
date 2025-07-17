package com.poo.proyectobless3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poo.proyectobless3.R;
import com.poo.proyectobless3.models.Cliente;
import com.poo.proyectobless3.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidoList;
    private Map<String, Cliente> clienteMap;
    private OnItemClickListener listener;
    private String userRole; // Para controlar la visibilidad de los botones de acción

    public interface OnItemClickListener {
        void onDetailsClick(Pedido pedido);
        void onViewClientOrdersClick(String clienteId);
        void onEditStatusClick(Pedido pedido);
        void onDeleteOrderClick(Pedido pedido); // Nuevo: para eliminar el pedido
    }

    public PedidoAdapter(List<Pedido> pedidoList, Map<String, Cliente> clienteMap, String userRole, OnItemClickListener listener) {
        this.pedidoList = pedidoList;
        this.clienteMap = clienteMap;
        this.userRole = userRole;
        this.listener = listener;
    }

    public void setPedidoList(List<Pedido> newPedidoList) {
        this.pedidoList = newPedidoList;
        notifyDataSetChanged();
    }

    public void setClienteMap(Map<String, Cliente> newClienteMap) {
        this.clienteMap = newClienteMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);

        holder.tvOrderId.setText(String.format("ID Pedido: #%s", pedido.getId() != null ? pedido.getId().substring(0, Math.min(pedido.getId().length(), 8)) : "N/A"));
        holder.tvOrderStatus.setText(String.format("Estado: %s", pedido.getEstado()));

        if (pedido.getClienteId() != null && clienteMap.containsKey(pedido.getClienteId())) {
            holder.tvOrderClientName.setText(String.format("Cliente: %s", clienteMap.get(pedido.getClienteId()).getNombre()));
        } else {
            holder.tvOrderClientName.setText("Cliente: Desconocido");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvOrderDate.setText(String.format("Fecha: %s", sdf.format(new Date(pedido.getFechaPedido()))));

        holder.llOrderItems.removeAllViews();
        if (pedido.getProductos() != null && !pedido.getProductos().isEmpty()) {
            for (Map.Entry<String, Pedido.PedidoItem> entry : pedido.getProductos().entrySet()) {
                Pedido.PedidoItem item = entry.getValue();
                TextView itemTextView = new TextView(holder.itemView.getContext());
                itemTextView.setText(String.format(Locale.getDefault(), "• %s (%d uds.) - $%.2f c/u",
                        item.getNombreProducto(), item.getCantidad(), item.getPrecioUnitario()));
                itemTextView.setTextSize(14);
                holder.llOrderItems.addView(itemTextView);
            }
        } else {
            TextView noItemsText = new TextView(holder.itemView.getContext());
            noItemsText.setText("No hay productos en este pedido.");
            noItemsText.setTextSize(14);
            holder.llOrderItems.addView(noItemsText);
        }

        // Configurar visibilidad y listeners para botones de acción (Editar Estado y Eliminar)
        if (userRole != null && userRole.equals("admin")) {
            holder.btnEditStatus.setVisibility(View.VISIBLE);
            holder.btnEditStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditStatusClick(pedido);
                }
            });

            holder.btnDeleteOrder.setVisibility(View.VISIBLE);
            holder.btnDeleteOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteOrderClick(pedido);
                }
            });
        } else {
            holder.btnEditStatus.setVisibility(View.GONE);
            holder.btnDeleteOrder.setVisibility(View.GONE);
        }

        holder.btnViewClientOrders.setOnClickListener(v -> {
            if (listener != null && pedido.getClienteId() != null) {
                listener.onViewClientOrdersClick(pedido.getClienteId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidoList.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderClientName, tvOrderStatus, tvOrderDate, tvOrderItemsLabel;
        LinearLayout llOrderItems;
        Button btnViewClientOrders;
        Button btnEditStatus;
        Button btnDeleteOrder; // Nuevo: Botón para eliminar pedido

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderClientName = itemView.findViewById(R.id.tv_order_client_name);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderItemsLabel = itemView.findViewById(R.id.tv_order_items_label);
            llOrderItems = itemView.findViewById(R.id.ll_order_items);
            btnViewClientOrders = itemView.findViewById(R.id.btn_view_client_orders);
            btnEditStatus = itemView.findViewById(R.id.btn_edit_status);
            btnDeleteOrder = itemView.findViewById(R.id.btn_delete_order); // Inicializar el nuevo botón
        }
    }
}