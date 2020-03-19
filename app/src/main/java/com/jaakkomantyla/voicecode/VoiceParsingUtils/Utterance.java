package com.jaakkomantyla.voicecode.VoiceParsingUtils;

import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.LinkedList;

public class Utterance {
    String text;
    LinkedList<String> words;

    public Utterance(String text){
        this.text = text;
        words = new LinkedList<>();
        extractWords(text);
    }

    private void extractWords(String text){
        String word = "";
        for(int i = 0; i<text.length(); i++){
            if (text.charAt(i)!=' '){
                word += text.charAt(i);
            }else if(word.length()!=0){
                words.add(word);
                word = "";
            }
        }
        if(word.length()!=0){
            words.add(word);
        }
    }

    public LinkedList<String> getWords() {
        return words;
    }

    public void setWords(LinkedList<String> words) {
        this.words = words;
    }
}
