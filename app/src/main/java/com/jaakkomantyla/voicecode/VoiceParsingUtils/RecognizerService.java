package com.jaakkomantyla.voicecode.VoiceParsingUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.TextView;

import com.jaakkomantyla.voicecode.CodeEditText;
import com.jaakkomantyla.voicecode.MainActivity;
import com.jaakkomantyla.voicecode.R;
import com.jaakkomantyla.voicecode.VoiceCodeRecognitionListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RecognizerService extends Service {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int SPEECH_REQUEST_CODE = 0;
    private SpeechRecognizer recognizer;
    private VoiceCodeRecognitionListener recognitionListener;
    public static final String JAVA_STATEMENT = "java";
    public static final String PHONE_SEARCH = "phone";
    private HashMap<String, Integer> captions;
    private String initInfo;
    public RecognizerService() {
    }


    @Override
    public void onCreate(){
        super.onCreate();

        WeakReference<RecognizerService> activityReference = new WeakReference<>(this);
        try {
            Assets assets = new Assets(activityReference.get());
            File assetDir = assets.syncAssets();
            activityReference.get().setupRecognizer(assetDir);
            initInfo = "Recognizer initialized";
        } catch (IOException e) {
            System.out.println(e);
            initInfo = "Failed to init recognizer " + e;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "java.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(recognitionListener);


        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);

        // Create grammar-based search for digit recognition
        File javaGrammar = new File(assetsDir, "java_keywords.jsgf");
        recognizer.addGrammarSearch(JAVA_STATEMENT, javaGrammar);


    }

    public void switchSearch(String searchName) {

        recognizer.stop();


        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds). ???

        recognizer.startListening(searchName);

        String caption = getResources().getString(captions.get(searchName));

        //((TextView) findViewById(R.id.info_text)).setText(caption);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }
}
