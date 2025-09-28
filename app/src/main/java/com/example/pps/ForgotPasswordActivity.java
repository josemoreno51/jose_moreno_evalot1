package com.example.ppsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private MaterialButton btnRecoverPassword, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        tilEmail = findViewById(R.id.tilEmail);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        // Botón Recuperar Contraseña
        btnRecoverPassword.setOnClickListener(v -> validateAndRecoverPassword());

        // Botón Volver
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndRecoverPassword() {
        String email = etEmail.getText().toString().trim();

        // Reset error
        tilEmail.setError(null);

        boolean isValid = true;

        // Validar email
        if (email.isEmpty()) {
            tilEmail.setError("Ingrese su email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("Ingrese un email válido");
            isValid = false;
        }

        if (isValid) {
            // Simular envío de recuperación
            Toast.makeText(this, "Se ha enviado un enlace de recuperación a su email", Toast.LENGTH_LONG).show();

            // Volver al Login después de mostrar confirmación
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 2000);
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}