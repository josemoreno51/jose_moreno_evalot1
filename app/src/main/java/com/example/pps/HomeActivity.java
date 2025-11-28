package com.example.pps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNoticias;
    private Button btnAgregarNoticia, btnCerrarSesion;
    private TextView tvEmptyState;
    private List<Noticia> listaNoticias;
    private NoticiaAdapter noticiaAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("DEBUG_HOME", "=== HOME ACTIVITY INICIADA ===");

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        showUserInfo();
        loadNoticias();
    }

    private void initViews() {
        Log.d("DEBUG_HOME", "🔍 INICIANDO BUSQUEDA DE VISTAS...");

        recyclerViewNoticias = findViewById(R.id.recyclerViewNoticias);
        btnAgregarNoticia = findViewById(R.id.btnAgregarNoticia);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // DIAGNÓSTICO DE VISTAS
        Log.d("DEBUG_HOME", "RecyclerView: " + (recyclerViewNoticias != null ? "✅" : "❌"));
        Log.d("DEBUG_HOME", "Btn Agregar: " + (btnAgregarNoticia != null ? "✅" : "❌"));
        Log.d("DEBUG_HOME", "Btn Cerrar Sesión: " + (btnCerrarSesion != null ? "✅" : "❌"));
        Log.d("DEBUG_HOME", "TV Empty: " + (tvEmptyState != null ? "✅" : "❌"));

        // Configurar RecyclerView
        listaNoticias = new ArrayList<>();
        noticiaAdapter = new NoticiaAdapter(listaNoticias);
        recyclerViewNoticias.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNoticias.setAdapter(noticiaAdapter);

        noticiaAdapter.setOnItemClickListener(noticia -> {
            Intent intent = new Intent(HomeActivity.this, ViewNoticiaActivity.class);
            intent.putExtra("noticia_id", noticia.getId());
            startActivity(intent);
        });

        Log.d("DEBUG_HOME", "🎯 VISTAS INICIALIZADAS");
    }

    private void setupListeners() {
        Log.d("DEBUG_HOME", "🔗 CONFIGURANDO LISTENERS...");

        // Botón Agregar Noticia
        btnAgregarNoticia.setOnClickListener(v -> {
            Log.d("DEBUG_HOME", "➕ Botón Agregar clickeado");
            startActivity(new Intent(HomeActivity.this, AddNoticiaActivity.class));
        });

        // Botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            Log.d("DEBUG_HOME", "🚪 Botón Cerrar Sesión clickeado");
            cerrarSesion();
        });

        Log.d("DEBUG_HOME", "🎯 LISTENERS CONFIGURADOS");
    }

    private void showUserInfo() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            Log.d("DEBUG_HOME", "👤 Usuario actual: " + userEmail);
            Toast.makeText(this, "Bienvenido: " + userEmail, Toast.LENGTH_SHORT).show();
        } else {
            Log.d("DEBUG_HOME", "👤 No hay usuario logueado");
        }
    }

    private void loadNoticias() {
        Log.d("DEBUG_HOME", "📥 INICIANDO CARGA DE NOTICIAS DESDE FIRESTORE");

        if (db == null) {
            Log.e("DEBUG_HOME", "❌ ERROR: Firestore no inicializado");
            Toast.makeText(this, "Error: Base de datos no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG_HOME", "🔍 Consultando colección: noticia");

        db.collection("noticia").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalDocuments = task.getResult().size();
                        Log.d("DEBUG_HOME", "📊 TOTAL DOCUMENTOS ENCONTRADOS: " + totalDocuments);

                        listaNoticias.clear();
                        int count = 0;

                        // Mostrar info de cada documento
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("DEBUG_HOME", "📄 Documento ID: " + document.getId());
                            Log.d("DEBUG_HOME", "📄 Datos: " + document.getData());

                            try {
                                // Intentar crear noticia con toObject
                                Noticia noticia = document.toObject(Noticia.class);
                                noticia.setId(document.getId());
                                listaNoticias.add(noticia);
                                count++;

                                Log.d("DEBUG_HOME", "✅ Noticia cargada: " + noticia.getTitulo());

                            } catch (Exception e) {
                                Log.e("DEBUG_HOME", "❌ Error con toObject(), intentando manualmente...");

                                // Método alternativo: crear noticia manualmente
                                try {
                                    Noticia noticiaManual = new Noticia();
                                    noticiaManual.setId(document.getId());
                                    noticiaManual.setTitulo(document.getString("titulo"));
                                    noticiaManual.setResumen(document.getString("resumen"));
                                    noticiaManual.setContenido(document.getString("contenido"));
                                    noticiaManual.setAutor(document.getString("autor"));
                                    noticiaManual.setFecha(document.getDate("fecha"));
                                    noticiaManual.setUserId(document.getString("userId"));

                                    listaNoticias.add(noticiaManual);
                                    count++;

                                    Log.d("DEBUG_HOME", "✅ Noticia manual: " + noticiaManual.getTitulo());

                                } catch (Exception e2) {
                                    Log.e("DEBUG_HOME", "❌ ERROR CRÍTICO con documento " + document.getId() + ": " + e2.getMessage());
                                }
                            }
                        }

                        Log.d("DEBUG_HOME", "🎯 TOTAL NOTICIAS CARGADAS: " + listaNoticias.size());
                        noticiaAdapter.notifyDataSetChanged();
                        updateUI();

                        // Mostrar resultado final
                        if (listaNoticias.isEmpty()) {
                            Log.d("DEBUG_HOME", "📭 LISTA VACÍA - No se pudieron cargar noticias");
                            Toast.makeText(this, "No hay noticias disponibles", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, listaNoticias.size() + " noticias cargadas", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e("DEBUG_HOME", "❌ ERROR EN CONSULTA FIRESTORE: " + task.getException().getMessage());
                        Toast.makeText(this, "Error cargando noticias: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_HOME", "❌ FALLA EN CONEXIÓN: " + e.getMessage());
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateUI() {
        if (listaNoticias.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewNoticias.setVisibility(View.GONE);
            Log.d("DEBUG_HOME", "📭 Mostrando estado vacío");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewNoticias.setVisibility(View.VISIBLE);
            Log.d("DEBUG_HOME", "📱 Mostrando " + listaNoticias.size() + " noticias");
        }
    }

    private void cerrarSesion() {
        Log.d("DEBUG_HOME", "🔐 INICIANDO CIERRE DE SESIÓN");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        Log.d("DEBUG_HOME", "🚪 EJECUTANDO LOGOUT...");

        try {
            auth.signOut();
            Log.d("DEBUG_HOME", "✅ Sesión cerrada en Firebase");

            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("DEBUG_HOME", "❌ ERROR en logout: " + e.getMessage());
            Toast.makeText(this, "Error al cerrar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG_HOME", "🔄 onResume - RECARGANDO NOTICIAS");
        loadNoticias();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("DEBUG_HOME", "🎯 onNewIntent - RECARGANDO DESDE ADD NOTICIA");
        loadNoticias();
    }
}