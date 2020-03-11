package com.jaakkomantyla.voicecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import edu.cmu.pocketsphinx.*;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private static final int SPEECH_REQUEST_CODE = 0;
    private EditText codeText;
    private TextView infoText;
    private ToggleButton speechRecognition;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;


    private static final String JAVA_STATEMENT = "java";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        captions = new HashMap<>();
        captions.put(JAVA_STATEMENT, R.string.java_caption);


        setContentView(R.layout.activity_main);
        codeText = findViewById(R.id.code_text);
        codeText.setShowSoftInputOnFocus(false);
        codeText.setText(R.string.test_code);

        String testCode = getString(R.string.test_code);


        CompilationUnit testParse = StaticJavaParser.parse(testCode);

        Node node = testParse.findRootNode();
        System.out.println(node.toString());



        infoText = findViewById(R.id.info_text);

        speechRecognition = findViewById(R.id.speak_button);
        speechRecognition.setEnabled(false);

        speechRecognition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSearch(JAVA_STATEMENT);
                } else {
                    recognizer.stop();
                }
            }
        });


        checkRecordingPermission();

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();

    }



    public  void startSpeechRecognizer(View v){

    }

    private void checkRecordingPermission(){
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
    }


    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;
        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                System.out.println("virhe!!! "+e);
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            String initInfo;
            if (result != null) {
                initInfo = "Failed to init recognizer " + result;

            } else {

                initInfo = "Recognizer initialized";

            }

            activityReference.get().getInfoText()
                    .setText(initInfo);

            activityReference.get().findViewById(R.id.speak_button)
                    .setEnabled(true);
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "java.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */



        // Create grammar-based search for digit recognition
        File javaGrammar = new File(assetsDir, "java_keywords.jsgf");
        recognizer.addGrammarSearch(JAVA_STATEMENT, javaGrammar);


    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds). ???

        recognizer.startListening(searchName);

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.info_text)).setText(caption);
    }


    @Override
    public void onBeginningOfSpeech() {
        Toast toast=Toast.makeText(getApplicationContext(),"started",Toast.LENGTH_SHORT);

        toast.show();
    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if (hypothesis != null) {


            Toast toast = Toast.makeText(getApplicationContext(), "partial result: " + hypothesis.getHypstr(), Toast.LENGTH_SHORT);
            toast.show();

            switchSearch(JAVA_STATEMENT);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = VoiceParsingUtils.checkForKeyWords(hypothesis.getHypstr())+" ";

            int start = Math.max(codeText.getSelectionStart(), 0);
            int end = Math.max(codeText.getSelectionEnd(), 0);
            codeText.getText().replace(Math.min(start, end), Math.max(start, end),
                    text, 0, text.length());


        }
    }

    @Override
    public void onError(Exception e) {
        ((TextView) findViewById(R.id.info_text)).setText(e.getMessage());
    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }


    public TextView getInfoText() {
        return infoText;
    }




}
