package com.jaakkomantyla.voicecode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;

import com.jaakkomantyla.voicecode.VoiceParsingUtils.VoiceParser;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

//TODO: fix sub directory files can't be loaded bug
public class MainActivity extends AppCompatActivity  {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private static final int SPEECH_REQUEST_CODE = 0;
    private CodeEditText codeText;
    private TextView infoText;
    private ToggleButton speechRecognition;
    private ToggleButton keyboardTgl;
    private Button saveButton;
    private Button loadButton;
    private HashMap<String, Integer> captions;
    private RecognizerViewModel recognizerViewModel;
    private CodeTextViewModel codeTextViewModel;
    public static final String JAVA_STATEMENT = "java";
    public static final String PHONE_SEARCH = "phone";
    private VoiceParser voiceParser;
    private String fileName = "gg.java";
    private Uri fileUri;


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

        codeTextViewModel = new ViewModelProvider(this).get(CodeTextViewModel.class);
        setupSaveButton();
        setupLoadButton();
        setupSpeechRecogBtn();
        setupKeyboardBtn();
        checkRecordingPermission();

    }

    private void setupKeyboardBtn() {

        keyboardTgl = findViewById(R.id.keyboard_button);

        keyboardTgl.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                codeText.setShowSoftInputOnFocus(true);
            } else {
                codeText.setShowSoftInputOnFocus(false);
            }
        });
    }

    private void setupSpeechRecogBtn(){

        speechRecognition = findViewById(R.id.speak_button);
        speechRecognition.setEnabled(recognizerViewModel.getReady().getValue());
        speechRecognition.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                recognizerViewModel.switchSearch(JAVA_STATEMENT);
            } else {
                recognizerViewModel.getRecognizer().stop();
            }
        });

    }

    private void setupSaveButton(){
        saveButton =  findViewById(R.id.saveExternalStorage);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            saveButton.setEnabled(false);
            displayToast("Can't use external memory");
        }

        saveButton.setOnClickListener((v)->{
            if(fileName == null || fileUri == null){
                createFile();
            }
            else{
                writeInFile(fileUri, getCodeText().getText().toString());
            }
        });


    }

    private void setupLoadButton(){
        loadButton = (Button) findViewById(R.id.getExternalStorage);
        loadButton.setOnClickListener((v) -> {

               openFile();

        });
    }

    private void createObservers(){
        recognizerViewModel.getUtterance().observe(this, (utterance)->{
            voiceParser.parseToCode(utterance);
        });

        recognizerViewModel.getInfo().observe(this, (info)->{
            infoText.setText(info);
        });
        recognizerViewModel.getToastText().observe(this, this::displayToast);

        recognizerViewModel.getReady().observe(this, (b)->{
            findViewById(R.id.speak_button).setEnabled(b);
        });

    }

    public void displayToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }


    private void checkRecordingPermission(){
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-java-source,java");


        startActivityForResult(intent, 69);
    }


    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-java-source,java");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, 420);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 69 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                System.out.println(uri);
                readFromFile(uri);
            }

        }else if (requestCode == 420 && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                Uri uri = resultData.getData();
                // Perform operations on the document using its URI.
                System.out.println(uri);
                writeInFile(uri, codeText.getText().toString());

            }
        }
    }

    private void writeInFile(@NonNull Uri uri, @NonNull String text) {
        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(text);
            bw.flush();
            bw.close();

            setFileNameFromUri(uri);
            fileUri = uri;
            displayToast(fileName + " saved to "+uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            displayToast("error: "+e);
        }

    }

    public void readFromFile(@NonNull Uri uri){


        try {
            String data="";
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                data = data + strLine + "\n";
            }
            in.close();
            codeText.setText(data);


            setFileNameFromUri(uri);
            fileUri = uri;
            displayToast(fileName + " loaded");
        } catch (IOException e) {
            e.printStackTrace();
            displayToast("Error loading file: " + e);
        }

    }

    private void setFileNameFromUri(Uri uri){
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        fileName =  result;
    }


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
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

    public CodeTextViewModel getCodeTextViewModel() {
        return codeTextViewModel;
    }

    public void setCodeTextViewModel(CodeTextViewModel codeTextViewModel) {
        this.codeTextViewModel = codeTextViewModel;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizerViewModel.getRecognizer() != null) {
            recognizerViewModel.getRecognizer().stop();
        }
    }
}
