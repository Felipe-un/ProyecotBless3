package com.poo.proyectobless3.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // Importar Glide
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage; // Importar Firebase Storage
import com.google.firebase.storage.StorageReference; // Importar StorageReference
import com.poo.proyectobless3.R;
import com.poo.proyectobless3.databinding.ActivityProductoFormBinding;
import com.poo.proyectobless3.models.Producto;

import java.util.Objects;
import java.util.UUID; // Para generar nombres de archivo únicos

public class ProductoFormActivity extends AppCompatActivity {

    private static final String TAG = "ProductoFormActivity";
    private ActivityProductoFormBinding binding;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef; // Referencia a Firebase Storage
    private String productId = null;
    private Uri selectedImageUri; // URI de la imagen seleccionada
    private String currentImageUrl; // URL de la imagen actual del producto (si se está editando)

    // Launchers para permisos y selección de imagen
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductoFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference("productos");
        mStorageRef = FirebaseStorage.getInstance().getReference("product_images"); // Carpeta en Storage

        // Inicializar ActivityResultLaunchers
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImageChooser();
                    } else {
                        Toast.makeText(this, "Permiso de almacenamiento es necesario para seleccionar imágenes.", Toast.LENGTH_LONG).show();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        selectedImageUri = result.getData().getData();
                        binding.ivProductImagePreview.setImageURI(selectedImageUri); // Mostrar vista previa
                    }
                });

        // Configurar el Spinner de categorías
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.product_categories_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        // Comprobar si se está editando un producto existente
        if (getIntent().hasExtra("productId")) {
            productId = getIntent().getStringExtra("productId");
            binding.tvFormTitle.setText("Editar Producto");
            loadProductData(productId); // Cargar los datos del producto para editar
        } else {
            binding.tvFormTitle.setText("Agregar Producto");
        }

        binding.btnSaveProduct.setOnClickListener(view -> saveProduct());
        binding.btnCancel.setOnClickListener(view -> finish());
        binding.btnSelectImage.setOnClickListener(view -> checkPermissionAndOpenImageChooser());
    }

    private void checkPermissionAndOpenImageChooser() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImageChooser();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void loadProductData(String id) {
        mDatabase.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Producto producto = snapshot.getValue(Producto.class);
                if (producto != null) {
                    binding.etProductName.setText(producto.getNombre());
                    binding.etProductDescription.setText(producto.getDescripcion());
                    binding.etProductStock.setText(String.valueOf(producto.getStock()));
                    binding.etProductPrice.setText(String.valueOf(producto.getPrecio()));

                    currentImageUrl = producto.getImagenURL(); // Guardar la URL actual
                    if (!TextUtils.isEmpty(currentImageUrl)) {
                        Glide.with(ProductoFormActivity.this)
                                .load(currentImageUrl)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                                .into(binding.ivProductImagePreview);
                    } else {
                        binding.ivProductImagePreview.setImageResource(R.drawable.ic_image_placeholder);
                    }

                    // Seleccionar la categoría correcta en el Spinner
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) binding.spinnerCategory.getAdapter();
                    if (adapter != null) {
                        int spinnerPosition = adapter.getPosition(producto.getCategoria());
                        binding.spinnerCategory.setSelection(spinnerPosition);
                    }
                } else {
                    Toast.makeText(ProductoFormActivity.this, "Producto no encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar datos del producto: " + error.getMessage());
                Toast.makeText(ProductoFormActivity.this, "Error al cargar producto: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String description = binding.etProductDescription.getText().toString().trim();
        String stockStr = binding.etProductStock.getText().toString().trim();
        String priceStr = binding.etProductPrice.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();

        // Validaciones
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(stockStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Por favor, complete nombre, stock y precio.", Toast.LENGTH_SHORT).show();
            return;
        }

        int stock;
        double price;
        try {
            stock = Integer.parseInt(stockStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Stock y precio deben ser números válidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (stock < 0 || price < 0) {
            Toast.makeText(this, "Stock y precio no pueden ser negativos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar ProgressBar y deshabilitar botón
        binding.pbImageUpload.setVisibility(View.VISIBLE);
        binding.btnSaveProduct.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(name, stock, price, category, description);
        } else {
            // Si no se seleccionó una nueva imagen, usar la URL existente o null
            saveProductToDatabase(name, stock, price, category, description, currentImageUrl);
        }
    }

    private void uploadImageToFirebaseStorage(String name, int stock, double price, String category, String description) {
        if (selectedImageUri == null) {
            saveProductToDatabase(name, stock, price, category, description, currentImageUrl); // Debería ser manejado por el else en saveProduct()
            return;
        }

        // Crear un nombre de archivo único para la imagen
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = mStorageRef.child(fileName);

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        Log.d(TAG, "Imagen subida exitosamente. URL: " + downloadUrl);
                        saveProductToDatabase(name, stock, price, category, description, downloadUrl);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener URL de descarga: " + e.getMessage());
                        Toast.makeText(ProductoFormActivity.this, "Error al obtener URL de imagen.", Toast.LENGTH_SHORT).show();
                        binding.pbImageUpload.setVisibility(View.GONE);
                        binding.btnSaveProduct.setEnabled(true);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir imagen: " + e.getMessage());
                    Toast.makeText(ProductoFormActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.pbImageUpload.setVisibility(View.GONE);
                    binding.btnSaveProduct.setEnabled(true);
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    Log.d(TAG, "Progreso de subida: " + (int) progress + "%");
                    // Puedes actualizar una barra de progreso si lo deseas
                });
    }

    private void saveProductToDatabase(String name, int stock, double price, String category, String description, String imageUrl) {
        Producto producto = new Producto(
                productId,
                name,
                stock,
                imageUrl,
                price,
                category,
                description
        );

        if (productId == null) {
            // Nuevo producto
            String newProductId = mDatabase.push().getKey();
            if (newProductId != null) {
                producto.setId(newProductId);
                mDatabase.child(newProductId).setValue(producto)
                        .addOnCompleteListener(task -> {
                            binding.pbImageUpload.setVisibility(View.GONE);
                            binding.btnSaveProduct.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(ProductoFormActivity.this, "Producto agregado exitosamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e(TAG, "Error al agregar producto: " + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(ProductoFormActivity.this, "Error al agregar producto: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                binding.pbImageUpload.setVisibility(View.GONE);
                binding.btnSaveProduct.setEnabled(true);
                Toast.makeText(ProductoFormActivity.this, "Error al generar ID de producto.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Editar producto existente
            mDatabase.child(productId).setValue(producto)
                    .addOnCompleteListener(task -> {
                        binding.pbImageUpload.setVisibility(View.GONE);
                        binding.btnSaveProduct.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(ProductoFormActivity.this, "Producto actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "Error al actualizar producto: " + Objects.requireNonNull(task.getException()).getMessage());
                            Toast.makeText(ProductoFormActivity.this, "Error al actualizar producto: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
