package com.example.atomlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class home extends AppCompatActivity {
    Button logout;
    TextView name;
    FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fStore = FirebaseFirestore.getInstance();
        String email;
        Boolean auth = getIntent().getBooleanExtra("auth",true);
        Boolean guest = getIntent().getBooleanExtra("guest",false);
        logout = findViewById(R.id.logoutButton);
        name = findViewById(R.id.name);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount!=null)
            email = signInAccount.getEmail();
        if(guest == false)
        {
            if (auth || signInAccount != null) {
                String userId = getIntent().getStringExtra("userId");
                if (signInAccount != null) {
                    name.setText("Hi!! " + signInAccount.getDisplayName());
                }
            }
        }
        else {
            String displayName = getIntent().getStringExtra("name");
            name.setText("Hi!! " + displayName);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }
}