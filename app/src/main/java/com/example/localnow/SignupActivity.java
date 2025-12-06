package com.example.localnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        android.widget.EditText etEmail = findViewById(R.id.et_signup_email);
        android.widget.EditText etId = findViewById(R.id.et_signup_id);
        android.widget.EditText etPassword = findViewById(R.id.et_signup_password);
        android.widget.EditText etConfirm = findViewById(R.id.et_signup_password_confirm);
        Button btnSignupComplete = findViewById(R.id.btn_signup_complete);

        btnSignupComplete.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String id = etId.getText().toString();
            String password = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (email.isEmpty() || id.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                android.widget.Toast.makeText(this, "모든 필드를 입력해주세요", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                android.widget.Toast.makeText(this, "비밀번호가 일치하지 않습니다", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.localnow.model.RegisterRequest request = new com.example.localnow.model.RegisterRequest(id,
                    password, email);
            com.example.localnow.api.RetrofitClient.getApiService().register(request)
                    .enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                android.widget.Toast
                                        .makeText(SignupActivity.this, "회원가입 성공", android.widget.Toast.LENGTH_SHORT)
                                        .show();
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class); // Go to login
                                                                                                      // after signup
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                android.widget.Toast.makeText(SignupActivity.this, "회원가입 실패: " + response.code(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            android.widget.Toast.makeText(SignupActivity.this, "네트워크 오류: " + t.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
