package com.example.atomlogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    Button googleButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    Button guestButton;
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            Intent intent =  new Intent(getApplicationContext(),home.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        createRequest();

        googleButton = findViewById(R.id.googleButton);
        guestButton = findViewById(R.id.guestButton);


        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                googleButton.setVisibility(View.INVISIBLE);
                guestButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                signIn();
//                Intent intent = new Intent(MainActivity.this,register.class);
//                startActivity(intent);
            }
        });

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent =  new Intent(getApplicationContext(),register.class);
                intent.putExtra("auth",false);
                startActivity(intent);
            }
        });
    }

    private void createRequest()
    {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userName = user.getDisplayName();
                            String userEmail = user.getEmail();
                            String userId = user.getUid();
                            //String userImage =  user.getPhotoUrl();

                            DocumentReference documentReference = fStore.collection("users").document(userId);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot doc = task.getResult();
                                        if(doc.exists())
                                        {
                                            Toast.makeText(MainActivity.this,"welcome back",Toast.LENGTH_SHORT).show();
                                            Intent intent =  new Intent(getApplicationContext(),home.class);
                                            intent.putExtra("name",userName);
                                            intent.putExtra("auth",true);
                                            intent.putExtra("userId",userId);
                                            startActivity(intent);

                                            progressBar.setVisibility(View.INVISIBLE);
                                            googleButton.setVisibility(View.VISIBLE);
                                            guestButton.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            Map<String,Object> userMap = new HashMap<>();
                                            userMap.put("name",userName);
                                            userMap.put("email",userEmail);
                                            documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    Toast.makeText(MainActivity.this,"User Created",Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                            Intent intent =  new Intent(getApplicationContext(),register.class);
                                            intent.putExtra("name",userName);
                                            intent.putExtra("auth",true);
                                            intent.putExtra("userId",userId);

                                            progressBar.setVisibility(View.INVISIBLE);
                                            googleButton.setVisibility(View.VISIBLE);
                                            guestButton.setVisibility(View.VISIBLE);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this,"Authentication Failed!",Toast.LENGTH_SHORT).show();
                            // If sign in fails, display a message to the user.
                        }
                        // ...
                    }
                });
    }
}