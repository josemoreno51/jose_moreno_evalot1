package com.example.pps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNoticiaActivity extends AppCompatActivity {

    private EditText etTitulo, etResumen, etContenido, etAutor;
    private Button btnGuardar, btnCancelar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_noticia);

        Log.d("DEBUG_NOTICIA", "=== ADD NOTICIA ACTIVITY INICIADA ===");

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitulo = findViewById(R.id.etTitulo);
        etResumen = findViewById(R.id.etResumen);
        etContenido = findViewById(R.id.etContenido);
        etAutor = findViewById(R.id.etAutor);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> guardarNoticia());
        btnCancelar.setOnClickListener(v -> {
            Log.d("DEBUG_NOTICIA", "❌ Cancelado por usuario");
            finish();
        });
    }

    private void guardarNoticia() {
        String titulo = etTitulo.getText().toString().trim();
        String resumen = etResumen.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();

        Log.d("DEBUG_NOTICIA", "🔄 Validando datos...");
        Log.d("DEBUG_NOTICIA", "Título: " + titulo);
        Log.d("DEBUG_NOTICIA", "Resumen: " + resumen);
        Log.d("DEBUG_NOTICIA", "Contenido: " + contenido);
        Log.d("DEBUG_NOTICIA", "Autor: " + autor);

        // Validaciones
        if (titulo.isEmpty()) {
            etTitulo.setError("Ingresa un título");
            return;
        }
        if (resumen.isEmpty()) {
            etResumen.setError("Ingresa un resumen");
            return;
        }
        if (contenido.isEmpty()) {
            etContenido.setError("Ingresa el contenido");
            return;
        }

        // Si el autor está vacío, usar el email del usuario
        if (autor.isEmpty()) {
            if (auth.getCurrentUser() != null) {
                autor = auth.getCurrentUser().getEmail();
                Log.d("DEBUG_NOTICIA", "📧 Usando email del usuario como autor: " + autor);
            } else {
                autor = "Anónimo";
            }
        }

        Log.d("DEBUG_NOTICIA", "💾 Guardando en Firestore...");

        // Crear mapa de datos
        Map<String, Object> noticia = new HashMap<>();
        noticia.put("titulo", titulo);
        noticia.put("resumen", resumen);
        noticia.put("contenido", contenido);
        noticia.put("autor", autor);
        noticia.put("fecha", new Date());
        noticia.put("userId", auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonimo");
        noticia.put("createdAt", new Date());

        Log.d("DEBUG_NOTICIA", "📤 Enviando a colección: noticia");

        // Guardar en Firestore
        db.collection("noticia")
                .add(noticia)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DEBUG_NOTICIA", "✅ NOTICIA GUARDADA EXITOSAMENTE");
                    Log.d("DEBUG_NOTICIA", "📄 ID del documento: " + documentReference.getId());

                    Toast.makeText(this, "✅ Noticia guardada exitosamente", Toast.LENGTH_SHORT).show();

                    // Volver al Home y forzar recarga
                    Intent intent = new Intent(AddNoticiaActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_NOTICIA", "❌ ERROR GUARDANDO NOTICIA: " + e.getMessage());
                    Toast.makeText(this, "❌ Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}