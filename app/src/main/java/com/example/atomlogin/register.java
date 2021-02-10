package com.example.atomlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class register extends AppCompatActivity {
    ImageButton back;
    EditText name;
    Boolean auth;
    Button continueButton;
    FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fStore = FirebaseFirestore.getInstance();

        back = findViewById(R.id.backButton);
        name = findViewById(R.id.registerName);
        continueButton = findViewById(R.id.continueButton);

        auth = getIntent().getBooleanExtra("auth",false);

        if(auth)
        {
            String userName = getIntent().getStringExtra("name");
            name.setText(userName);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String enteredName = name.getText().toString();
                if(auth)
                {
                    String userId  = getIntent().getStringExtra("userId");
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    documentReference.update("name",enteredName);
                    Intent intent =  new Intent(getApplicationContext(),home.class);
                    intent.putExtra("userId",userId);
                    intent.putExtra("auth",true);
                    intent.putExtra("guest",false);
                    startActivity(intent);
                }
                else
                {
                    Intent intent =  new Intent(getApplicationContext(),home.class);
                    intent.putExtra("name",enteredName);
                    intent.putExtra("auth",false);
                    intent.putExtra("guest",true);
                    startActivity(intent);
                }
            }
        });
    }
}