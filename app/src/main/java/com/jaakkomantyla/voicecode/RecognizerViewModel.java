package com.jaakkomantyla.voicecode;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class RecognizerViewModel extends AndroidViewModel implements Runnable, RecognitionListener {

    private SpeechRecognizer recognizer;
    public static final String JAVA_STATEMENT = "java";
    public static final String PHONE_SEARCH = "phone";
    private HashMap<String, Integer> captions;

    private MutableLiveData<String> utterance;
    private MutableLiveData<String> toastText;
    private MutableLiveData<String> info;
    private MutableLiveData<Boolean> ready;

    //TODO: fix the delay in recognizing start
    public RecognizerViewModel(Application application){
        super(application);


        captions = new HashMap<>();
        captions.put(JAVA_STATEMENT, R.string.java_caption);

        utterance = new MutableLiveData<>();
        toastText = new MutableLiveData<>();
        info = new MutableLiveData<>();
        ready = new MutableLiveData<Boolean>();
        ready.setValue(false);

        Thread t1 = new Thread(this);
        t1.start();

    }

    @Override
    public void run() {

        String initInfo;
        WeakReference<Application> appReference = new WeakReference<>(this.getApplication());
        try {
            Assets assets = new Assets(appReference.get());
            File assetDir = assets.syncAssets();
            setupRecognizer(assetDir);
            initInfo = "Recognizer initialized";
        } catch (IOException e) {
            System.out.println(e);
            initInfo = "Failed to init recognizer " + e;
        }

        info.postValue(initInfo);
        ready.postValue(true);
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

        info.setValue(getApplication().getResources().getString(captions.get(searchName)));

        //((TextView) findViewById(R.id.info_text)).setText(caption);
    }

    @Override
    public void onBeginningOfSpeech() {

        toastText.setValue("started");
    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            toastText.setValue("partial result: " + hypothesis.getHypstr());

            switchSearch(JAVA_STATEMENT);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {

            utterance.setValue(hypothesis.getHypstr());
        }
    }

    @Override
    public void onError(Exception e) {
        info.setValue(e.getMessage());
    }

    @Override
    public void onTimeout() {

    }

    public MutableLiveData<String> getUtterance() {
        return utterance;
    }

    public void setUtterance(MutableLiveData<String> utterance) {
        this.utterance = utterance;
    }

    public MutableLiveData<String> getToastText() {
        return toastText;
    }

    public void setToastText(MutableLiveData<String> toastText) {
        this.toastText = toastText;
    }

    public MutableLiveData<String> getInfo() {
        return info;
    }

    public void setInfo(MutableLiveData<String> info) {
        this.info = info;
    }

    public MutableLiveData<Boolean> getReady() {
        return ready;
    }

    public void setReady(MutableLiveData<Boolean> ready) {
        this.ready = ready;
    }

    public SpeechRecognizer getRecognizer() {
        return recognizer;
    }

    public void setRecognizer(SpeechRecognizer recognizer) {
        this.recognizer = recognizer;
    }
}