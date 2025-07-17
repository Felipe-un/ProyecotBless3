package com.poo.proyectobless3.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poo.proyectobless3.R;
import com.poo.proyectobless3.models.Pedido;
import com.poo.proyectobless3.models.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ProductSelectionViewHolder> {

    private List<Producto> productList;
    // Mapa para almacenar la cantidad seleccionada para cada producto (ID de Producto -> Cantidad)
    private Map<String, Integer> selectedQuantities;
    // Mapa para almacenar si un producto está seleccionado (ID de Producto -> Booleano)
    private Map<String, Boolean> productCheckedState;

    public ProductSelectionAdapter(List<Producto> productList) {
        this.productList = productList;
        this.selectedQuantities = new HashMap<>();
        this.productCheckedState = new HashMap<>();
        // Inicializar con 1 para todos los productos y no seleccionados
        for (Producto p : productList) {
            selectedQuantities.put(p.getId(), 1); // Cantidad por defecto
            productCheckedState.put(p.getId(), false); // No seleccionado por defecto
        }
    }

    public void setProductList(List<Producto> newProductList) {
        this.productList = newProductList;
        // Re-inicializar mapas si la lista de productos cambia
        selectedQuantities.clear();
        productCheckedState.clear();
        for (Producto p : productList) {
            selectedQuantities.put(p.getId(), 1);
            productCheckedState.put(p.getId(), false);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_selection, parent, false);
        return new ProductSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSelectionViewHolder holder, int position) {
        Producto producto = productList.get(position);

        holder.tvProductName.setText(producto.getNombre());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
        holder.tvProductStock.setText(String.format(Locale.getDefault(), "Stock: %d", producto.getStock()));

        // Manejar el estado del CheckBox
        holder.cbSelectProduct.setChecked(productCheckedState.getOrDefault(producto.getId(), false));
        holder.cbSelectProduct.setOnCheckedChangeListener(null); // Limpiar listener para evitar bucles
        holder.cbSelectProduct.setChecked(productCheckedState.getOrDefault(producto.getId(), false));
        holder.cbSelectProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
            productCheckedState.put(producto.getId(), isChecked);
            // Si se desmarca, la cantidad vuelve a 1
            if (!isChecked) {
                selectedQuantities.put(producto.getId(), 1);
                holder.etProductQuantity.setText("1");
            }
        });

        // Manejar la cantidad del EditText
        holder.etProductQuantity.removeTextChangedListener(holder.textWatcher); // Eliminar listener anterior
        holder.etProductQuantity.setText(String.valueOf(selectedQuantities.getOrDefault(producto.getId(), 1)));
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int quantity = 0;
                if (!s.toString().isEmpty()) {
                    try {
                        quantity = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        // Manejar error si no es un número válido
                        Toast.makeText(holder.itemView.getContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
                        holder.etProductQuantity.setText("1"); // Restablecer a 1
                        quantity = 1;
                    }
                }

                // Validar que la cantidad no exceda el stock
                if (quantity > producto.getStock()) {
                    Toast.makeText(holder.itemView.getContext(), "Cantidad excede el stock disponible (" + producto.getStock() + ")", Toast.LENGTH_SHORT).show();
                    quantity = producto.getStock();
                    holder.etProductQuantity.setText(String.valueOf(quantity));
                }
                if (quantity <= 0 && productCheckedState.getOrDefault(producto.getId(), false)) {
                    Toast.makeText(holder.itemView.getContext(), "La cantidad debe ser al menos 1 para productos seleccionados", Toast.LENGTH_SHORT).show();
                    quantity = 1;
                    holder.etProductQuantity.setText("1");
                }

                selectedQuantities.put(producto.getId(), quantity);
            }
        };
        holder.etProductQuantity.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Método para obtener los productos seleccionados con sus cantidades
    public List<Pedido.PedidoItem> getSelectedProducts() {
        List<Pedido.PedidoItem> items = new ArrayList<>();
        for (Producto p : productList) {
            if (productCheckedState.getOrDefault(p.getId(), false)) { // Si el producto está marcado
                int quantity = selectedQuantities.getOrDefault(p.getId(), 1);
                if (quantity > 0) { // Asegurarse de que la cantidad sea válida
                    items.add(new Pedido.PedidoItem(p.getId(), p.getNombre(), quantity, p.getPrecio()));
                }
            }
        }
        return items;
    }

    public static class ProductSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelectProduct;
        TextView tvProductName, tvProductPrice, tvProductStock;
        EditText etProductQuantity;
        TextWatcher textWatcher; // Para manejar el listener del EditText

        public ProductSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectProduct = itemView.findViewById(R.id.cb_select_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name_selection);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price_selection);
            tvProductStock = itemView.findViewById(R.id.tv_product_stock_selection);
            etProductQuantity = itemView.findViewById(R.id.et_product_quantity);
        }
    }
}
