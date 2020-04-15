package com.jaakkomantyla.voicecode;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.CharArrayWriter;
import java.io.Writer;

public class ConsoleFragment extends Fragment {

    private ConsoleViewModel mViewModel;
    private TextView textView;
    private String printed;

    public ConsoleFragment(){
        super();
        printed = "";
    }

    public static ConsoleFragment newInstance() {
        return new ConsoleFragment();
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
        textView.setText(printed);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("ö activity created called");

        mViewModel = ViewModelProviders.of(this).get(ConsoleViewModel.class);
        // TODO: Use the ViewModel
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void print(String str){
        if(textView!=null){
            textView.setText(str);
        }else{
            printed = str;
        }

    }
}
