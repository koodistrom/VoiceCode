package com.jaakkomantyla.voicecode.VoiceParsingUtils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.jaakkomantyla.voicecode.MainActivity;

import java.util.LinkedList;
import java.util.List;

public class VoiceParser {

    public List<String> createClass  = extractWords("create class");
    private MainActivity context;

    public VoiceParser(MainActivity context){
        this.context = context;
    }


    public  void parseToCode(String  input){

        /*??
        if(input.length() > 6 && input.substring(0,6).equals("numeric")){
            return input.substring(7);
        }
        */
        String text;
        boolean goInBraces = false;
        switch(input)
        {
            case "semi-colon":
                text =  ";\n";
                break;
            case "braces":
                text =  "{}";
                goInBraces = true;
                break;
            case "brackets":
                text =  "()";
                goInBraces = true;
                break;

            case "linechange":
                text =  "\n";
                break;
            case "equals":
                text =  "=";
                break;
            default:
                text =  input;
        }

        text +=" ";

        addToCursorLocation(text);
        if(goInBraces){
            moveCursorRelativeToCurrentPos(-2);
        }
    }

    public int getNumber(String input) {
        int accumulator = 0;
        int total = 0;
        String[] words = input.trim()
                .split("\\s+");


        for (String word : words) {
            switch (word) {
                case "one":
                    accumulator += 1;
                    break;
                case "two":
                    accumulator += 2;
                    break;
                case "three":
                    accumulator += 3;
                    break;
                case "four":
                    accumulator += 4;
                    break;
                case "five":
                    accumulator += 5;
                    break;
                case "six":
                    accumulator += 6;
                    break;
                case "seven":
                    accumulator += 7;
                    break;
                case "eight":
                    accumulator += 8;
                    break;
                case "nine":
                    accumulator += 9;
                    break;
                case "ten":
                    accumulator += 10;
                    break;
                case "eleven":
                    accumulator += 11;
                    break;
                case "twelve":
                    accumulator += 12;
                    break;
                case "thirteen":
                    accumulator += 13;
                    break;
                case "fourteen":
                    accumulator += 14;
                    break;
                case "fiveteen":
                    accumulator += 15;
                    break;
                case "sixteen":
                    accumulator += 16;
                    break;
                case "seventeen":
                    accumulator += 17;
                    break;
                case "eighteen":
                    accumulator += 18;
                    break;
                case "nineteen":
                    accumulator += 19;
                    break;
                case "twenty":
                    accumulator += 20;
                    break;
                case "thirty":
                    accumulator += 30;
                    break;
                case "forty":
                    accumulator += 40;
                    break;
                case "fifty":
                    accumulator += 50;
                    break;
                case "sixty":
                    accumulator += 60;
                    break;
                case "seventy":
                    accumulator += 70;
                    break;
                case "eighty":
                    accumulator += 80;
                    break;
                case "ninety":
                    accumulator += 90;
                    break;
            }

            switch (word) {
                case "hundred":
                    accumulator *= 100;
                    break;
                case "thousand":
                    accumulator *= 1000;
                    total += accumulator;
                    accumulator = 0;
                    break;
            }
        }
            return total + accumulator;

    }



    public  Node parseCommand(String input) {
        List<String> inputWords = extractWords(input);
        if(inputWords.subList(0, 1).equals(createClass)){
            ClassOrInterfaceDeclaration n = new ClassOrInterfaceDeclaration();
            if(inputWords.contains("public")){
                n.setPublic(true);
            }
            if(inputWords.contains("static")){
                n.setStatic(true);
            }
            if(inputWords.contains("abstract")){
                n.setAbstract(true);
            }
            return n;
        }
        return null;
    }

    private static List<String> extractWords(String text){
        List<String> words  = new LinkedList<>();
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
        return words;
    }

    private void addToCursorLocation(String text){

        int start = Math.max(context.getCodeText().getSelectionStart(), 0);
        int end = Math.max(context.getCodeText().getSelectionEnd(), 0);
        context.getCodeText().getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());
    }

    private void moveCursorRelativeToCurrentPos(int steps){
        int currentPos =context.getCodeText().getSelectionStart();
        context.getCodeText().setSelection(currentPos+steps);
    }
}
