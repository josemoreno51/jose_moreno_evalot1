package com.example.pps;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewNoticiaActivity extends AppCompatActivity {

    private TextView tvTituloCompleto, tvContenido, tvAutor, tvFecha;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_noticia);

        Log.d("DEBUG_VIEW", "=== VIEW NOTICIA ACTIVITY INICIADA ===");

        db = FirebaseFirestore.getInstance();
        initViews();
        loadNoticiaData();
    }

    private void initViews() {
        tvTituloCompleto = findViewById(R.id.tvTituloCompleto);
        tvContenido = findViewById(R.id.tvContenido);
        tvAutor = findViewById(R.id.tvAutor);
        tvFecha = findViewById(R.id.tvFecha);

        Log.d("DEBUG_VIEW", "✅ Vistas inicializadas");
    }

    private void loadNoticiaData() {
        String noticiaId = getIntent().getStringExtra("noticia_id");

        Log.d("DEBUG_VIEW", "🔍 ID de noticia recibido: " + noticiaId);

        if (noticiaId == null || noticiaId.isEmpty()) {
            Log.e("DEBUG_VIEW", "❌ ERROR: noticia_id es NULL o vacío");
            Toast.makeText(this, "Error: ID de noticia no válido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("DEBUG_VIEW", "📡 Consultando Firestore para ID: " + noticiaId);

        db.collection("noticia")
                .document(noticiaId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Log.d("DEBUG_VIEW", "📄 Documento obtenido: " + document);

                        if (document != null && document.exists()) {
                            Log.d("DEBUG_VIEW", "✅ Documento EXISTE en Firestore");
                            Log.d("DEBUG_VIEW", "📊 Datos del documento: " + document.getData());

                            displayNoticiaData(document);
                        } else {
                            Log.e("DEBUG_VIEW", "❌ Documento NO EXISTE en Firestore");
                            Toast.makeText(ViewNoticiaActivity.this, "Noticia no encontrada", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Log.e("DEBUG_VIEW", "❌ ERROR en consulta Firestore: " + task.getException().getMessage());
                        Toast.makeText(ViewNoticiaActivity.this, "Error cargando noticia: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void displayNoticiaData(DocumentSnapshot document) {
        try {
            // Obtener datos individualmente para evitar problemas
            String titulo = document.getString("titulo");
            String contenido = document.getString("contenido");
            String resumen = document.getString("resumen");
            String autor = document.getString("autor");
            Object fechaObj = document.get("fecha");

            Log.d("DEBUG_VIEW", "🎨 Mostrando datos:");
            Log.d("DEBUG_VIEW", "   Título: " + titulo);
            Log.d("DEBUG_VIEW", "   Contenido: " + contenido);
            Log.d("DEBUG_VIEW", "   Resumen: " + resumen);
            Log.d("DEBUG_VIEW", "   Autor: " + autor);
            Log.d("DEBUG_VIEW", "   Fecha: " + fechaObj);

            // Mostrar título
            if (titulo != null && !titulo.isEmpty()) {
                tvTituloCompleto.setText(titulo);
            } else {
                tvTituloCompleto.setText("Título no disponible");
                Log.w("DEBUG_VIEW", "⚠️ Título vacío o nulo");
            }

            // Mostrar contenido (usar contenido si existe, sino usar resumen)
            String textoContenido = contenido;
            if (textoContenido == null || textoContenido.isEmpty()) {
                textoContenido = resumen;
                Log.d("DEBUG_VIEW", "📝 Usando resumen como contenido");
            }
            if (textoContenido != null && !textoContenido.isEmpty()) {
                tvContenido.setText(textoContenido);
            } else {
                tvContenido.setText("Contenido no disponible");
                Log.w("DEBUG_VIEW", "⚠️ Contenido y resumen vacíos");
            }

            // Mostrar autor
            if (autor != null && !autor.isEmpty()) {
                tvAutor.setText("Por: " + autor);
            } else {
                tvAutor.setText("Por: Autor desconocido");
                Log.w("DEBUG_VIEW", "⚠️ Autor vacío o nulo");
            }

            // Mostrar fecha
            if (fechaObj != null) {
                try {
                    // Firebase Timestamp se convierte a Date
                    com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) fechaObj;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault());
                    String fechaFormateada = dateFormat.format(timestamp.toDate());
                    tvFecha.setText(fechaFormateada);
                } catch (Exception e) {
                    Log.e("DEBUG_VIEW", "❌ Error formateando fecha: " + e.getMessage());
                    tvFecha.setText("Fecha no disponible");
                }
            } else {
                tvFecha.setText("Fecha no disponible");
                Log.w("DEBUG_VIEW", "⚠️ Fecha vacía o nula");
            }

            Log.d("DEBUG_VIEW", "✅ Noticia mostrada exitosamente");

        } catch (Exception e) {
            Log.e("DEBUG_VIEW", "❌ ERROR mostrando datos: " + e.getMessage());
            Toast.makeText(this, "Error mostrando noticia", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}