package com.taquangkhoi.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    TextView tvSingUp;
    TextView tvSingUp2;
    Button btnLogin;
    EditText edtEmail, edtPassword;
    ImageView ibtnGoogle, ibtnFacebook;
    final static int RC_SIGN_IN = 123;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    SignInClient oneTapClient;
    BeginSignInRequest signInRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvSingUp = findViewById(R.id.tvSingUp);
        tvSingUp2 = findViewById(R.id.tvSingUp2);
        tvSingUp.setOnClickListener(onClickSignup());
        tvSingUp2.setOnClickListener(onClickSignup());
        btnLogin = findViewById(R.id.btn_login);
        edtEmail = findViewById(R.id.etEmail);
        edtPassword = findViewById(R.id.etPassword);
        ibtnGoogle = findViewById(R.id.ibtnGoogle);
        ibtnFacebook = findViewById(R.id.ibtnFacebook);

        mAuth = FirebaseAuth.getInstance();

        createRequest();

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) { // để trống email
                    // check email is wrong
                    String regexPattern = "^(.+)@(\\S+)$";
                    if (!Pattern.matches(regexPattern, email)) {
                        edtEmail.setError("Email is wrong");
                    }
                    return;
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "onComplete: " + task.getResult().getUser().getUid());
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    edtPassword.setError("Password is wrong");
                                    Log.i(TAG, "onComplete: " + task.getException().getMessage());
                                }
                            });
                }
            }
        });

        ibtnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(LoginActivity.this, FacebookLoginActivity.class);
                //startActivity(intent);
            }
        });

        ibtnGoogle.setOnClickListener(v -> SignIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user!=null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Kết nối tới Google signin
     */
    private void createRequest() {
        Log.i(TAG, "createRequest: ");
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Gửi yêu cầu đăng nhập
     */
    private void SignIn() {
        Intent signinIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signinIntent, RC_SIGN_IN);
    }
//
//    /**
//     * Xử lý yêu cầu đăng nhập được trả về
//     * @param requestCode RC_SIGN_IN
//     * @param resultCode RC_SIGN_IN
//     * @param data intent
//     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.getExtras().keySet().forEach(
                    key -> Log.i(TAG, "onActivityResult: key " + key + " " + data.getExtras().get(key))
            );
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i(TAG, "Google sign in failed - ", e);
                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }
//    /**
//     * Hàm định nghĩa xử lý quá trình gửi yêu cầu đưng nhập bằng google
//     * @param idToken token gg
//     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
////        switch (requestCode) {
////            case REQ_ONE_TAP:
////                try {
////                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
////                    String idToken = credential.getGoogleIdToken();
////                    String username = credential.getId();
////                    String password = credential.getPassword();
////                    if (idToken !=  null) {
////                        // Got an ID token from Google. Use it to authenticate
////                        // with your backend.
////                        Log.d(TAG, "Got ID token.");
////                    } else if (password != null) {
////                        // Got a saved username and password. Use them to authenticate
////                        // with your backend.
////                        Log.d(TAG, "Got password.");
////                    }
////                } catch (ApiException e) {
////                    // ...
////                }
////                break;
////        }
//    }

    private View.OnClickListener onClickSignup() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        };
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}