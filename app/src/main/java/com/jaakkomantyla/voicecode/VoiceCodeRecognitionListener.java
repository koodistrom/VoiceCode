package com.jaakkomantyla.voicecode;

import android.widget.TextView;
import android.widget.Toast;

import com.jaakkomantyla.voicecode.VoiceParsingUtils.VoiceParser;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

public class VoiceCodeRecognitionListener implements RecognitionListener {

    private MainActivity context;
    private VoiceParser voiceParser;
    VoiceCodeRecognitionListener(MainActivity context){

        this.context = context;
        voiceParser = new VoiceParser(context);
    }

    @Override
    public void onBeginningOfSpeech() {
        Toast toast=Toast.makeText(context.getApplicationContext(),"started",Toast.LENGTH_SHORT);

        toast.show();
    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if (hypothesis != null) {

            Toast toast = Toast.makeText(context.getApplicationContext(), "partial result: " + hypothesis.getHypstr(), Toast.LENGTH_SHORT);
            toast.show();

            context.switchSearch(context.JAVA_STATEMENT);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            voiceParser.parseToCode(hypothesis.getHypstr());

        }
    }

    @Override
    public void onError(Exception e) {
        ((TextView) context.findViewById(R.id.info_text)).setText(e.getMessage());
    }

    @Override
    public void onTimeout() {

    }





}
