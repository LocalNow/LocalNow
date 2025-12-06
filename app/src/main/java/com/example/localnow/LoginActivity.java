package com.example.localnow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private com.google.android.gms.auth.api.signin.GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Google Sign-In will be initialized when button is clicked (prevents ANR)

        Button btnLogin = findViewById(R.id.btn_login);
        Button btnGoogleLogin = findViewById(R.id.btn_google_login);
        TextView tvSignup = findViewById(R.id.tv_signup);

        android.widget.EditText etId = findViewById(R.id.et_id);
        android.widget.EditText etPassword = findViewById(R.id.et_password);

        btnLogin.setOnClickListener(v -> {
            String id = etId.getText().toString();
            String password = etPassword.getText().toString();

            if (id.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.localnow.model.LoginRequest request = new com.example.localnow.model.LoginRequest(id, password);
            com.example.localnow.api.RetrofitClient.getApiService().login(request)
                    .enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                android.widget.Toast
                                        .makeText(LoginActivity.this, "로그인 성공", android.widget.Toast.LENGTH_SHORT)
                                        .show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                android.widget.Toast.makeText(LoginActivity.this, "로그인 실패: 아이디 또는 비밀번호 확인",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            android.widget.Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGoogleLogin.setOnClickListener(v -> {
            // Initialize Google Sign-In client only when button is clicked
            initializeGoogleSignIn();
        });

        tvSignup.setOnClickListener(v -> {
            // 회원가입 화면으로 이동
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void initializeGoogleSignIn() {
        // Show loading indicator
        android.widget.Toast.makeText(this, "Google Sign-In 준비 중...", android.widget.Toast.LENGTH_SHORT).show();

        // Initialize in background thread to prevent ANR
        new Thread(() -> {
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("564482737758-mco0jdir6gsh3n7eqgo9heb56ds4a6k0.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

            // Start sign-in on UI thread
            runOnUiThread(() -> signIn());
        }).start();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        // GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(
            com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> completedTask) {
        try {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = completedTask
                    .getResult(com.google.android.gms.common.api.ApiException.class);

            // Signed in successfully, show authenticated UI.
            String idToken = account.getIdToken();
            sendTokenToBackend(idToken);

        } catch (com.google.android.gms.common.api.ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more
            // information.
            android.util.Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            android.widget.Toast
                    .makeText(this, "Google Sign In Failed: " + e.getStatusCode(), android.widget.Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void sendTokenToBackend(String idToken) {
        com.example.localnow.model.GoogleLoginRequest request = new com.example.localnow.model.GoogleLoginRequest(
                idToken);
        com.example.localnow.api.RetrofitClient.getApiService().googleLogin(request)
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Login Success
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            android.widget.Toast.makeText(LoginActivity.this, "Backend Login Failed",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        android.widget.Toast
                                .makeText(LoginActivity.this, "Network Error", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
