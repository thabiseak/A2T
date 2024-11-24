package com.example.imagepro.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.imagepro.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class TranslateToSignActivity extends AppCompatActivity {

    private EditText editText;
    private Button translateButton;
    private ImageView signImageView;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private List<String> imageUrls = new ArrayList<>();
    private int currentIndex = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_sign);

        editText = findViewById(R.id.idEditSource);
        translateButton = findViewById(R.id.btn_Translate_to_sign);
        signImageView = findViewById(R.id.signImageView);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("Sign/");

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = editText.getText().toString().toLowerCase();
                if (inputText.isEmpty()) {
                    Toast.makeText(TranslateToSignActivity.this, "Please enter text", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageUrls.clear();
                currentIndex = 0;
                fetchImages(inputText);
            }
        });
    }

    private void fetchImages(String text) {
        for (char letter : text.toCharArray()) {
            if (Character.isLetter(letter)) {
                String imageName = letter + ".png";
                StorageReference imageRef = storageRef.child(imageName);
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    if (imageUrls.size() == text.length()) {
                        displayImagesSequentially();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(TranslateToSignActivity.this, "Failed to load " + imageName, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void displayImagesSequentially() {
        if (currentIndex < imageUrls.size()) {
            String imageUrl = imageUrls.get(currentIndex);
            Glide.with(this).load(imageUrl).into(signImageView);

            // Schedule the next image to show after a delay (e.g., 1 second)
            signImageView.postDelayed(() -> {
                currentIndex++;
                displayImagesSequentially();
            }, 1000);
        }
    }

    // Optional: Back button functionality
    public void backToMainPage(View view) {
        finish();
    }
}