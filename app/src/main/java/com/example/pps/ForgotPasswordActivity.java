package com.example.pps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";
    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private MaterialButton btnResetPassword, btnBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Log.d(TAG, "=== ACTIVIDAD DE RECUPERACIÓN INICIADA ===");

        // 1. INICIALIZAR FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();

        if (mAuth == null) {
            Log.e(TAG, "❌ ERROR CRÍTICO: FirebaseAuth es NULL");
            Toast.makeText(this, "Error de configuración de Firebase", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "✅ FirebaseAuth inicializado correctamente");

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        tilEmail = findViewById(R.id.tilEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> validateAndReset());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndReset() {
        String email = etEmail.getText().toString().trim();

        // Resetear errores
        tilEmail.setError(null);

        // Validación básica
        if (email.isEmpty()) {
            tilEmail.setError("Ingrese su correo");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Formato de correo inválido");
            etEmail.requestFocus();
            return;
        }

        // Deshabilitar botón para evitar múltiples envíos
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Enviando...");

        Log.d(TAG, "📤 Intentando enviar email de recuperación a: " + email);

        // 🔥🔥🔥 ESTA ES LA LÍNEA MÁGICA QUE ENVÍA EL CORREO 🔥🔥🔥
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅✅✅ EMAIL ENVIADO EXITOSAMENTE A: " + email);

                        // Mensaje detallado para el usuario
                        String mensaje = "✅ Enlace de recuperación enviado a:\n" + email +
                                "\n\n📌 IMPORTANTE:\n" +
                                "1. Revisa tu BANDEJA DE ENTRADA\n" +
                                "2. Si no lo ves, revisa la carpeta SPAM\n" +
                                "3. El correo viene de: noreply@" + getDomainFromEmail(email) +
                                "\n4. El enlace expira en 24 horas";

                        Toast.makeText(ForgotPasswordActivity.this,
                                mensaje,
                                Toast.LENGTH_LONG).show();

                        // Limpiar campo
                        etEmail.setText("");

                        // Volver automáticamente después de 4 segundos
                        new android.os.Handler().postDelayed(() -> {
                            finish();
                        }, 4000);

                    } else {
                        // Habilitar botón nuevamente
                        btnResetPassword.setEnabled(true);
                        btnResetPassword.setText("Enviar enlace");

                        // Obtener el error
                        Exception exception = task.getException();
                        String errorMessage = "Error desconocido";

                        if (exception != null) {
                            String error = exception.getMessage();
                            Log.e(TAG, "❌ Error Firebase: " + error);

                            // Manejar errores específicos
                            if (error.contains("user-not-found")) {
                                // Por seguridad, no decir que el usuario no existe
                                errorMessage = "📩 Si el email está registrado, recibirás el enlace en unos minutos.";

                            } else if (error.contains("invalid-email")) {
                                errorMessage = "✉️ Email inválido. Verifica el formato.";

                            } else if (error.contains("network-request-failed")) {
                                errorMessage = "📡 Error de conexión. Verifica tu internet.";

                            } else if (error.contains("too-many-requests")) {
                                errorMessage = "⏰ Demasiados intentos. Espera unos minutos.";

                            } else if (error.contains("missing-continue-uri")) {
                                errorMessage = "⚠️ Error de configuración en Firebase.";

                            } else {
                                errorMessage = "❌ Error: " + error;
                            }
                        }

                        Toast.makeText(ForgotPasswordActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error de red u otro problema
                    Log.e(TAG, "🔥 Falla completa: " + e.getMessage());
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Enviar enlace");

                    Toast.makeText(ForgotPasswordActivity.this,
                            "Error de conexión: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String getDomainFromEmail(String email) {
        try {
            // Extraer dominio para mostrar en el mensaje
            String[] parts = email.split("@");
            if (parts.length > 1) {
                return parts[1];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extrayendo dominio: " + e.getMessage());
        }
        return "firebaseapp.com";
    }
}