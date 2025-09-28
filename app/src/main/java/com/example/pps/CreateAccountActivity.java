package com.example.ppsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilAddress;
    private MaterialButton btnCreateAccount, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        // Botón Crear Cuenta
        btnCreateAccount.setOnClickListener(v -> validateAndCreateAccount());

        // Botón Volver al Login
        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validateAndCreateAccount() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Reset errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);

        boolean isValid = true;

        // Validar nombre completo
        if (fullName.isEmpty()) {
            tilFullName.setError("Ingrese su nombre completo");
            isValid = false;
        }

        // Validar email
        if (email.isEmpty()) {
            tilEmail.setError("Ingrese su email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("Ingrese un email válido");
            isValid = false;
        }

        // Validar teléfono
        if (phone.isEmpty()) {
            tilPhone.setError("Ingrese su teléfono");
            isValid = false;
        }

        // Validar dirección
        if (address.isEmpty()) {
            tilAddress.setError("Ingrese su dirección");
            isValid = false;
        }

        if (isValid) {
            // Simular creación exitosa de cuenta
            Toast.makeText(this, "¡Cuenta creada exitosamente!", Toast.LENGTH_LONG).show();

            // Volver al Login después de mostrar confirmación
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 2000);
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}