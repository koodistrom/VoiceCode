package com.jaakkomantyla.voicecode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaakkomantyla.voicecode.VoiceParsingUtils.VoiceParser;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

//TODO: fix sub directory files can't be loaded bug, file info rotation bug, fix keyboard covers buttons
public class MainActivity extends AppCompatActivity  {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private static final int SPEECH_REQUEST_CODE = 0;
    private CodeEditText codeText;
    private TextView infoText;
    private FloatingActionButton speechRecognitionTgl;
    private FloatingActionButton keyboardTgl;
    private FloatingActionButton consoleTgl;
    private boolean micOn;
    private boolean showKeyboard;
    private boolean showConsole;

    private Button saveButton;
    private Button openButton;
    private Button newButton;
    private Button runButton;
    private Button deleteButton;
    private HashMap<String, Integer> captions;
    private RecognizerViewModel recognizerViewModel;
    private CodeTextViewModel codeTextViewModel;
    public static final String JAVA_STATEMENT = "java";
    public static final String PHONE_SEARCH = "phone";
    private VoiceParser voiceParser;
    private String fileName = "gg.java";
    private Uri fileUri;
    private final int OPEN = 69;
    private final int SAVE = 420;
    private final int DELETE = 8;
    private final int RUN = 2;
    private FragmentManager fragmentManager;
    private ConsoleFragment consoleFragment;
    private Writer writer;
    private FileUtils fu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        captions = new HashMap<>();
        captions.put(JAVA_STATEMENT, R.string.java_caption);
        writer = new CharArrayWriter();
        setContentView(R.layout.activity_main);
        codeText = findViewById(R.id.code_text);
        codeText.setShowSoftInputOnFocus(false);
        infoText = findViewById(R.id.info_text);

        voiceParser = new VoiceParser(this);
        recognizerViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RecognizerViewModel.class);
        createObservers();

        fu = new FileUtils(this);

        codeTextViewModel = new ViewModelProvider(this).get(CodeTextViewModel.class);
        consoleFragment = new ConsoleFragment();
        setupSaveButton();
        setupOpenButton();
        setupNewButton();
        setupDeleteButton();
        setupRunButton();
        setupSpeechRecogBtn(savedInstanceState);
        setupKeyboardBtn(savedInstanceState);
        setupConsoleBtn(savedInstanceState);
        checkRecordingPermission();




    }

    private void setupKeyboardBtn(Bundle savedState) {
        showKeyboard  = savedState != null ? savedState.getBoolean("keyboard"):false;
        keyboardTgl = findViewById(R.id.keyboard_btn_float);
        setKeyBoardVisible(showKeyboard);
        keyboardTgl.setOnClickListener((v) -> {

            setKeyBoardVisible(!showKeyboard);
        });
    }

    private void setKeyBoardVisible(boolean showKeyboard){
        this.showKeyboard = showKeyboard;
        codeText.setShowSoftInputOnFocus(showKeyboard);
        if (showKeyboard) {
            keyboardTgl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_hide_24px));
        } else {
            keyboardTgl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_24px));
            hideKeyboard(this);
        }
    }

    private void setupConsoleBtn(Bundle savedState){

        showConsole = savedState != null ? savedState.getBoolean("console"):false;
        consoleTgl = findViewById(R.id.console_btn_float);
        setConsoleVisible(showConsole);
        consoleTgl.setOnClickListener((v) -> {
            setConsoleVisible(!showConsole);
        });

    }

    private void setConsoleVisible(boolean showConsole){
        this.showConsole = showConsole;
        if (showConsole) {

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main, consoleFragment, "console");
            fragmentTransaction.commit();

        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(consoleFragment);
            fragmentTransaction.commit();
        }
    }
    private void setupSpeechRecogBtn(Bundle savedState){
        micOn = savedState != null ? savedState.getBoolean("mic"):false;
        speechRecognitionTgl = findViewById(R.id.mic_btn_float);
        speechRecognitionTgl.setEnabled(recognizerViewModel.getReady().getValue());
        speechRecognitionTgl.setOnClickListener((v)-> {
            micOn = !micOn;
            if (micOn) {
                recognizerViewModel.switchSearch(JAVA_STATEMENT);
                speechRecognitionTgl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_24px));
            } else {
                recognizerViewModel.getRecognizer().stop();
                speechRecognitionTgl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mic_off_24px));
            }
        });

    }

    private void setupNewButton(){
        newButton =  findViewById(R.id.newFile);

        newButton.setOnClickListener((v)->{
            getCodeText().zero();
            fileName = null;
            fileUri = null;

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

    private void setupOpenButton(){
        openButton = (Button) findViewById(R.id.getExternalStorage);
        openButton.setOnClickListener((v) -> {
               openFile(OPEN);
        });
    }

    private void setupDeleteButton(){
        deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setOnClickListener((v) -> {
            openFile(DELETE);
        });
    }

    private void setupRunButton(){
        runButton = (Button) findViewById(R.id.run);
        runButton.setOnClickListener((v) -> {
            if(fileUri!=null) {
                FileUtils fu = new FileUtils(this);
                String path = fu.getPath(fileUri);

                if(fragmentManager.findFragmentByTag("console")==null){

                    consoleTgl.callOnClick();

                }

                JavaCompilingUtils.compile(this, path, writer);
                consoleFragment.print(writer.toString());
            }else{
                displayToast("no file selected (maybe you forgot to save?)");
            }
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
            findViewById(R.id.mic_btn_float).setEnabled(b);
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

    private void openFile(int reguestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-java-source,java");


        startActivityForResult(intent, reguestCode);
    }


    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-java-source,java");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, SAVE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == OPEN && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                System.out.println(uri);
                readFromFile(uri);
            }

        }else if (requestCode == SAVE && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                Uri uri = resultData.getData();
                // Perform operations on the document using its URI.
                System.out.println(uri);
                writeInFile(uri, codeText.getText().toString());

            }
        }else if (requestCode == RUN && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                fu = new FileUtils(this);
                String path = fu.getPath(uri);
                JavaCompilingUtils.compile(this, path, writer);

            }
        }else if (requestCode == DELETE && resultCode == Activity.RESULT_OK) {
            Uri uri = resultData.getData();
            File fdelete = new File( fu.getPath(uri));

            if (fdelete.getAbsoluteFile().exists()) {
                String name = fdelete.getName();
                if (fdelete.delete()) {
                    infoText.setText("file Deleted: " + name);
                } else {
                    infoText.setText("Unable to delete file: " + name);
                }
            }else{
                infoText.setText("no file found");
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
            outputStream.close();
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
    protected void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putBoolean("keyboard", showKeyboard);
        savedState.putBoolean("console", showConsole);
        savedState.putBoolean("mic", micOn);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizerViewModel.getRecognizer() != null) {
            recognizerViewModel.getRecognizer().stop();
        }
    }
}
