package com.jaakkomantyla.voicecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.jaakkomantyla.voicecode.VoiceParsingUtils.VoiceParser;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import edu.cmu.pocketsphinx.*;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private static final int SPEECH_REQUEST_CODE = 0;
    private CodeEditText codeText;
    private TextView infoText;
    private ToggleButton speechRecognition;
    private HashMap<String, Integer> captions;
    private RecognizerViewModel recognizerViewModel;
    public static final String JAVA_STATEMENT = "java";
    public static final String PHONE_SEARCH = "phone";
    private VoiceParser voiceParser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        captions = new HashMap<>();
        captions.put(JAVA_STATEMENT, R.string.java_caption);

        setContentView(R.layout.activity_main);
        codeText = findViewById(R.id.code_text);
        codeText.setShowSoftInputOnFocus(false);
        infoText = findViewById(R.id.info_text);

        voiceParser = new VoiceParser(this);
        recognizerViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RecognizerViewModel.class);
        createObservers();

        //recognitionListener = new VoiceCodeRecognitionListener(this);

        speechRecognition = findViewById(R.id.speak_button);
        speechRecognition.setEnabled(recognizerViewModel.getReady().getValue());

        speechRecognition.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                recognizerViewModel.switchSearch(JAVA_STATEMENT);
            } else {
                recognizerViewModel.getRecognizer().stop();
            }
        });


        checkRecordingPermission();

    }

    private void createObservers(){
        recognizerViewModel.getUtterance().observe(this, (utterance)->{
            voiceParser.parseToCode(utterance);
        });

        recognizerViewModel.getInfo().observe(this, (info)->{
            infoText.setText(info);
        });
        recognizerViewModel.getToastText().observe(this, (text)->{
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        });

        recognizerViewModel.getReady().observe(this, (b)->{
            findViewById(R.id.speak_button).setEnabled(b);
        });

    }


    private void checkRecordingPermission(){
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
    }


    public TextView getInfoText() {
        return infoText;
    }


    public CodeEditText getCodeText() {
        return codeText;
    }

    public void setCodeText(CodeEditText codeText) {
        this.codeText = codeText;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizerViewModel.getRecognizer() != null) {
            recognizerViewModel.getRecognizer().stop();
        }
    }
}
