package com.example.pps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNoticiaActivity extends AppCompatActivity {

    private static final String TAG = "AddNoticia";
    private static final int PICK_IMAGE_REQUEST = 1001;

    // Views
    private EditText etTitulo, etResumen, etContenido, etAutor, etUrlImagen;
    private ImageView ivPreview;
    private Button btnSeleccionarFoto, btnGuardar, btnCancelar, btnSalir;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Variables
    private Uri imagenUri;
    private String imagenUrl = "";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_noticia);

        Log.d(TAG, "=== ADD NOTICIA ACTIVITY INICIADA ===");

        // Inicializar Firebase
        inicializarFirebase();

        // Inicializar Views
        initViews();

        // Configurar listeners
        setupClickListeners();

        // Inicializar ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Guardando noticia");
        progressDialog.setMessage("Por favor espera...");
        progressDialog.setCancelable(false);
    }

    private void inicializarFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();

            Log.d(TAG, "✅ Firebase inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Firebase: " + e.getMessage());
            Toast.makeText(this, "Error de configuración de Firebase", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        etTitulo = findViewById(R.id.etTitulo);
        etResumen = findViewById(R.id.etResumen);
        etContenido = findViewById(R.id.etContenido);
        etAutor = findViewById(R.id.etAutor);
        etUrlImagen = findViewById(R.id.etUrlImagen);
        ivPreview = findViewById(R.id.ivPreview);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnSalir = findViewById(R.id.btnSalir);

        // Ocultar vista previa inicialmente
        ivPreview.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        // Botón Salir (X)
        btnSalir.setOnClickListener(v -> {
            Log.d(TAG, "Botón salir presionado");
            finish();
        });

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> {
            Log.d(TAG, "Botón cancelar presionado");
            finish();
        });

        // Botón Seleccionar Foto
        btnSeleccionarFoto.setOnClickListener(v -> {
            Log.d(TAG, "Seleccionar foto presionado");
            abrirSelectorImagen();
        });

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> {
            guardarNoticia();
        });
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            imagenUri = data.getData();

            try {
                // Mostrar vista previa
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
                ivPreview.setImageBitmap(bitmap);
                ivPreview.setVisibility(View.VISIBLE);

                // Limpiar URL si se selecciona imagen local
                etUrlImagen.setText("");
                imagenUrl = "";

                Toast.makeText(this, "✅ Imagen seleccionada", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Log.e(TAG, "❌ Error al cargar imagen: " + e.getMessage());
                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarNoticia() {
        // Obtener datos
        String titulo = etTitulo.getText().toString().trim();
        String resumen = etResumen.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();
        String urlImagen = etUrlImagen.getText().toString().trim();

        // Validaciones básicas
        if (titulo.isEmpty()) {
            etTitulo.setError("El título es obligatorio");
            etTitulo.requestFocus();
            return;
        }

        if (resumen.isEmpty()) {
            etResumen.setError("El resumen es obligatorio");
            etResumen.requestFocus();
            return;
        }

        if (contenido.isEmpty()) {
            etContenido.setError("El contenido es obligatorio");
            etContenido.requestFocus();
            return;
        }

        if (autor.isEmpty()) {
            etAutor.setError("El autor es obligatorio");
            etAutor.requestFocus();
            return;
        }

        // Mostrar ProgressDialog
        progressDialog.show();

        // Verificar si hay imagen para subir
        if (imagenUri != null) {
            // Subir imagen a Firebase Storage primero
            subirImagenAFirebase(titulo, resumen, contenido, autor);
        } else if (!urlImagen.isEmpty() && urlImagen.startsWith("http")) {
            // Usar URL proporcionada
            imagenUrl = urlImagen;
            guardarEnFirestore(titulo, resumen, contenido, autor, imagenUrl);
        } else {
            // No hay imagen
            guardarEnFirestore(titulo, resumen, contenido, autor, "");
        }
    }

    private void subirImagenAFirebase(String titulo, String resumen, String contenido, String autor) {
        // Crear referencia única para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nombreArchivo = "noticia_" + timeStamp + ".jpg";
        StorageReference imagenRef = storageRef.child("noticias/" + nombreArchivo);

        // Comprimir imagen
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            // Subir imagen
            UploadTask uploadTask = imagenRef.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de descarga
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imagenUrl = uri.toString();
                    Log.d(TAG, "✅ Imagen subida: " + imagenUrl);

                    // Ahora guardar en Firestore con la URL
                    guardarEnFirestore(titulo, resumen, contenido, autor, imagenUrl);

                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "❌ Error obteniendo URL: " + e.getMessage());
                    Toast.makeText(AddNoticiaActivity.this,
                            "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show();
                });

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Log.e(TAG, "❌ Error subiendo imagen: " + e.getMessage());
                Toast.makeText(AddNoticiaActivity.this,
                        "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Log.e(TAG, "❌ Error procesando imagen: " + e.getMessage());
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarEnFirestore(String titulo, String resumen, String contenido,
                                    String autor, String urlImagen) {

        // Obtener usuario actual
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        String usuarioId = usuarioActual != null ? usuarioActual.getUid() : "anonimo";
        String usuarioEmail = usuarioActual != null ? usuarioActual.getEmail() : "anonimo@email.com";

        // Crear objeto noticia
        Map<String, Object> noticia = new HashMap<>();
        noticia.put("titulo", titulo);
        noticia.put("resumen", resumen);
        noticia.put("contenido", contenido);
        noticia.put("autor", autor);
        noticia.put("imagenUrl", urlImagen);
        noticia.put("usuarioId", usuarioId);
        noticia.put("usuarioEmail", usuarioEmail);
        noticia.put("fechaCreacion", new Date());
        noticia.put("fechaActualizacion", new Date());
        noticia.put("visitas", 0);
        noticia.put("likes", 0);

        // Guardar en Firestore
        db.collection("noticias")
                .add(noticia)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();

                    Log.d(TAG, "✅ Noticia guardada con ID: " + documentReference.getId());
                    Toast.makeText(AddNoticiaActivity.this,
                            "✅ Noticia publicada exitosamente", Toast.LENGTH_LONG).show();

                    // Limpiar formulario
                    limpiarFormulario();

                    // Opcional: Volver a la actividad anterior después de 2 segundos
                    new android.os.Handler().postDelayed(() -> {
                        finish();
                    }, 2000);

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "❌ Error guardando noticia: " + e.getMessage());
                    Toast.makeText(AddNoticiaActivity.this,
                            "Error al guardar noticia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void limpiarFormulario() {
        etTitulo.setText("");
        etResumen.setText("");
        etContenido.setText("");
        etAutor.setText("");
        etUrlImagen.setText("");
        ivPreview.setImageBitmap(null);
        ivPreview.setVisibility(View.GONE);
        imagenUri = null;
        imagenUrl = "";
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
}