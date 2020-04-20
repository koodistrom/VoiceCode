package com.jaakkomantyla.voicecode.VoiceParsingUtils;

import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Class for single voice input: utterance can be one word or multible. Aren't used in the current
 * implementation.
 */
public class Utterance {

    private String text;

    private LinkedList<String> words;

    /**
     * Instantiates a new Utterance.
     *
     * @param text the voice input as text.
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utterance utterance = (Utterance) o;
        return getWords().equals(utterance.getWords());
    }

    /**
     * Contains boolean.
     *
     * @param word the word
     * @return the boolean
     */
    public boolean contains(String word){
        return getWords().contains(word);
    }

    /**
     * returns part of utterance as list.
     *
     * @param start           the index of first word
     * @param endNotInclusive the index of last word not inclusive
     * @return the list
     */
    public List<String> part(int start, int endNotInclusive){
        return words.subList(start,endNotInclusive);
    }

    /**
     * Gets words of the utterance as list.
     *
     * @return the words
     */
    public LinkedList<String> getWords() {
        return words;
    }

    /**
     * Sets words from list to utterance.
     *
     * @param words the words
     */
    public void setWords(LinkedList<String> words) {
        this.words = words;
    }
}
