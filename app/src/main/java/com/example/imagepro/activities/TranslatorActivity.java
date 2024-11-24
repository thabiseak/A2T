package com.example.imagepro.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.imagepro.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import java.util.ArrayList;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class TranslatorActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private EditText sourceText;
    private ImageView micIV;
    private Button translateBtn;
    private TextView translateTV;

    // Remove the duplicate "Korean" entry
    String[] fromLanguage = {"From", "English", "Afrikaans", "Irish", "Albanian", "Italian", "Arabic", "Japanese",
            "Kannada", "Bengali", "Belarusian", "Latvian", "Bulgarian", "Lithuanian", "Catalan", "Macedonian",
            "Chinese Simplified", "Malay", "Chinese Traditional", "Maltese", "Croatian", "Norwegian", "Czech",
            "Persian", "Danish", "Polish", "Dutch", "Portuguese", "Romanian", "Esperanto", "Russian", "Estonian",
            "Filipino", "Slovak", "Finnish", "Slovenian", "French", "Spanish", "Galician", "Swahili",  "Sinhalese", "Georgian",
            "Swedish", "German", "Tamil", "Greek", "Telugu", "Gujarati", "Haitian Creole", "Turkish", "Ukrainian",
            "Hindi", "Urdu", "Hungarian", "Vietnamese", "Icelandic", "Welsh", "Indonesian"};

    String[] toLanguage = {"to", "English", "Afrikaans", "Irish", "Albanian", "Italian", "Arabic", "Japanese",
            "Kannada", "Bengali", "Belarusian", "Latvian", "Bulgarian", "Lithuanian", "Catalan", "Macedonian",
            "Chinese Simplified", "Malay", "Chinese Traditional", "Maltese", "Croatian", "Norwegian", "Czech",
            "Persian", "Danish", "Polish", "Dutch", "Portuguese", "Romanian", "Esperanto", "Russian", "Estonian",
            "Filipino", "Slovak", "Finnish", "Slovenian", "French", "Spanish", "Galician", "Swahili",  "Sinhalese", "Georgian",
            "Swedish", "German", "Tamil", "Greek", "Telugu", "Gujarati", "Haitian Creole", "Turkish", "Ukrainian",
            "Hindi", "Urdu", "Hungarian", "Vietnamese", "Icelandic", "Welsh", "Indonesian"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int fromLanguageCode, toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);

        // Initialize views
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslation);
        translateTV = findViewById(R.id.idTranslatedTV);

        // Set up fromSpinner
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        // Set up toSpinner
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguage[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        // Mic click listener for speech recognition
        micIV.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate");
            try {
                startActivityForResult(intent, REQUEST_PERMISSION_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TranslatorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Translate button click listener
        translateBtn.setOnClickListener(view -> {
            translateTV.setVisibility(View.VISIBLE);
            translateTV.setText("");
            if (sourceText.getText().toString().isEmpty()) {
                Toast.makeText(TranslatorActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
            } else if (fromLanguageCode == 0) {
                Toast.makeText(TranslatorActivity.this, "Please select Source Language", Toast.LENGTH_SHORT).show();
            } else if (toLanguageCode == 0) {
                Toast.makeText(TranslatorActivity.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
            } else {
                translateText(fromLanguageCode, toLanguageCode, sourceText.getText().toString());
            }
        });
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        translateTV.setText("Downloading model, please wait...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        // Download model and translate text
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(aVoid -> {
            translateTV.setText("Translating...");
            translator.translate(source).addOnSuccessListener(translation -> {
                translateTV.setText(translation);
            }).addOnFailureListener(e -> {
                Toast.makeText(TranslatorActivity.this, "Failed to translate! Please try again.", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(TranslatorActivity.this, "Failed to download model! Check your internet connection.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                sourceText.setText(result.get(0)); // Set the recognized speech to the source text field
            }
        }
    }

    private int getLanguageCode(String language) {
        switch (language) {
            case "Afrikaans":
                return FirebaseTranslateLanguage.FA;
            case "Albanian":
                return FirebaseTranslateLanguage.SQ;
            case "Arabic":
                return FirebaseTranslateLanguage.AR;
            case "Azerbaijani":
                return FirebaseTranslateLanguage.AF;
            case "Basque":
                return FirebaseTranslateLanguage.ET;
            case "Belarusian":
                return FirebaseTranslateLanguage.BE;
            case "Bulgarian":
                return FirebaseTranslateLanguage.BG;
            case "Catalan":
                return FirebaseTranslateLanguage.CA;
            case "Chinese Simplified":
                return FirebaseTranslateLanguage.HI;
            case "Chinese Traditional":
                return FirebaseTranslateLanguage.HT;
            case "Croatian":
                return FirebaseTranslateLanguage.HR;
            case "Czech":
                return FirebaseTranslateLanguage.CS;
            case "Danish":
                return FirebaseTranslateLanguage.DA;
            case "Dutch":
                return FirebaseTranslateLanguage.NL;
            case "English":
                return FirebaseTranslateLanguage.EN;
            case "Estonian":
                return FirebaseTranslateLanguage.ET;
            case "Finnish":
                return FirebaseTranslateLanguage.FI;
            case "French":
                return FirebaseTranslateLanguage.FR;
            case "Galician":
                return FirebaseTranslateLanguage.GL;
            case "Georgian":
                return FirebaseTranslateLanguage.KA;
            case "German":
                return FirebaseTranslateLanguage.DE;
            case "Greek":
                return FirebaseTranslateLanguage.EL;
            case "Haitian Creole":
                return FirebaseTranslateLanguage.HT;
            case "Hebrew":
                return FirebaseTranslateLanguage.HE;
            case "Hindi":
                return FirebaseTranslateLanguage.HI;
            case "Hungarian":
                return FirebaseTranslateLanguage.HU;
            case "Icelandic":
                return FirebaseTranslateLanguage.IS;
            case "Indonesian":
                return FirebaseTranslateLanguage.ID;
            case "Irish":
                return FirebaseTranslateLanguage.GA;
            case "Italian":
                return FirebaseTranslateLanguage.IT;
            case "Japanese":
                return FirebaseTranslateLanguage.JA;
            case "Kannada":
                return FirebaseTranslateLanguage.KN;
            case "Korean":
                return FirebaseTranslateLanguage.KO;
            case "Latvian":
                return FirebaseTranslateLanguage.LV;
            case "Lithuanian":
                return FirebaseTranslateLanguage.LT;
            case "Macedonian":
                return FirebaseTranslateLanguage.MK;
            case "Malay":
                return FirebaseTranslateLanguage.MS;
            case "Maltese":
                return FirebaseTranslateLanguage.MT;
            case "Norwegian":
                return FirebaseTranslateLanguage.NO;
            case "Persian":
                return FirebaseTranslateLanguage.PT;
            case "Polish":
                return FirebaseTranslateLanguage.PL;
            case "Portuguese":
                return FirebaseTranslateLanguage.PT;
            case "Romanian":
                return FirebaseTranslateLanguage.RO;
            case "Russian":
                return FirebaseTranslateLanguage.RU;
            case "Slovak":
                return FirebaseTranslateLanguage.SK;
            case "Slovenian":
                return FirebaseTranslateLanguage.SL;
            case "Spanish":
                return FirebaseTranslateLanguage.ES;
            case "Swahili":
                return FirebaseTranslateLanguage.SW;
            case "Swedish":
                return FirebaseTranslateLanguage.SV;
            case "Tamil":
                return FirebaseTranslateLanguage.TA;
            case "Telugu":
                return FirebaseTranslateLanguage.TE;
            case "Turkish":
                return FirebaseTranslateLanguage.TR;
            case "Ukrainian":
                return FirebaseTranslateLanguage.UK;
            case "Urdu":
                return FirebaseTranslateLanguage.UR;
            case "Vietnamese":
                return FirebaseTranslateLanguage.VI;
            default:
                return 0; // Return 0 for unsupported languages
        }
    }
}
