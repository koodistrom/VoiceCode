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

/**
 * The Console fragment contains a text view that displays out put and errors created when user
 * compiles and runs a java file using the app.
 */
public class ConsoleFragment extends Fragment {

    private TextView textView;
    private SpannableStringBuilder printed;
    private ConsoleOutPut soutOutPut;
    private ConsoleOutPut errOutPut;
    private JavaFileHandler fileHandler;

    /**
     * Instantiates a new Console fragment.
     */
    public ConsoleFragment(){
        super();
        printed = new SpannableStringBuilder();
        soutOutPut = new ConsoleOutPut(this, "System.out: " ,Color.WHITE);
        errOutPut = new ConsoleOutPut(this, "ERROR: " ,Color.RED);
    }

    /**
     * New instance console fragment.
     *
     * @return the console fragment
     */
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
        String openedFile = ((MainActivity)getActivity()).getRunThis();
        if(openedFile != null){
            fileHandler.compileAndRun(openedFile);
            openedFile = null;
        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    /**
     * Gets text view.
     *
     * @return the text view
     */
    public TextView getTextView() {
        return textView;
    }

    /**
     * Sets text view.
     *
     * @param textView the text view
     */
    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    /**
     * Prints text to the console using Spannable. Spannable makes it possible to use different colors.
     *
     * @param str   the string to print
     * @param color the color to print with
     */
    public void print(String str, int color){
        addStringToSpann(printed, str, color);
        if(textView!=null){
            textView.setText(printed , TextView.BufferType.SPANNABLE);
        }
    }


    private SpannableStringBuilder addStringToSpann(SpannableStringBuilder spannable, String str, int color){
        spannable.append(str);
        int end = spannable.length();
        int start = end - str.length();
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Gets sout out put.
     *
     * @return the sout out put
     */
    public ConsoleOutPut getSoutOutPut() {
        return soutOutPut;
    }

    /**
     * Sets sout out put.
     *
     * @param soutOutPut the sout out put
     */
    public void setSoutOutPut(ConsoleOutPut soutOutPut) {
        this.soutOutPut = soutOutPut;
    }

    /**
     * Gets err out put.
     *
     * @return the err out put
     */
    public ConsoleOutPut getErrOutPut() {
        return errOutPut;
    }

    /**
     * Sets err out put.
     *
     * @param errOutPut the err out put
     */
    public void setErrOutPut(ConsoleOutPut errOutPut) {
        this.errOutPut = errOutPut;
    }

    /**
     * Gets file handler.
     *
     * @return the file handler
     */
    public JavaFileHandler getFileHandler() {
        return fileHandler;
    }

    /**
     * Sets file handler.
     *
     * @param fileHandler the file handler
     */
    public void setFileHandler(JavaFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
}
