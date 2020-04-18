package com.jaakkomantyla.voicecode;

import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConsoleFragment extends Fragment {

    private ConsoleViewModel mViewModel;
    private TextView textView;
    private SpannableStringBuilder printed;
    private ConsoleOutPut soutOutPut;
    private ConsoleOutPut errOutPut;
    private JavaFileHandler fileHandler;

    public ConsoleFragment(){
        super();
        printed = new SpannableStringBuilder();
        soutOutPut = new ConsoleOutPut(this, "System.out: " ,Color.WHITE);
        errOutPut = new ConsoleOutPut(this, "ERROR: " ,Color.RED);



    }

    public static ConsoleFragment newInstance() {
        return new ConsoleFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fileHandler = new JavaFileHandler(this);

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.console_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textView = getView().findViewById(R.id.console_text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(printed);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mViewModel = ViewModelProviders.of(this).get(ConsoleViewModel.class);
        // TODO: Use the ViewModel
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void print(String str, int color){
        addStringToSpann(printed, str, color);
        if(textView!=null){
            textView.setText(printed , TextView.BufferType.SPANNABLE);
        }
    }

    public SpannableStringBuilder addStringToSpann(SpannableStringBuilder spannable, String str, int color){
        spannable.append(str);
        int end = spannable.length();
        int start = end - str.length();
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public ConsoleOutPut getSoutOutPut() {
        return soutOutPut;
    }

    public void setSoutOutPut(ConsoleOutPut soutOutPut) {
        this.soutOutPut = soutOutPut;
    }

    public ConsoleOutPut getErrOutPut() {
        return errOutPut;
    }

    public void setErrOutPut(ConsoleOutPut errOutPut) {
        this.errOutPut = errOutPut;
    }

    public JavaFileHandler getFileHandler() {
        return fileHandler;
    }

    public void setFileHandler(JavaFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
}
