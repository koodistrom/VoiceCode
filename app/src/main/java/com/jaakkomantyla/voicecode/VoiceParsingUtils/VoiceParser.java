package com.jaakkomantyla.voicecode.VoiceParsingUtils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.jaakkomantyla.voicecode.MainActivity;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoiceParser {

    public List<String> createClass  = extractWords("create class");
    private MainActivity context;
    private int lastTokenLength;
    private  boolean lastGoInBraces;
    private Logger logger;
    private String indent = "    ";

    public VoiceParser(MainActivity context){
        logger = Logger.getLogger(VoiceParser.class.getName());
        this.context = context;
        lastTokenLength = 0;
        lastGoInBraces = false;
    }
/*TODO:  syntax highlighting on the go, proper undo/redo, space,
 */

    public  void parseToCode(String  input){
        String text = "";
        boolean goInBraces = false;
        logger.log(Level.INFO, "input: " +input);
        if(input.length() > 6 && input.substring(0,6).equals("number")){
            text = input.substring(7);
        }else if (input.equals("undo")){

            int offset = 0;
            if(lastGoInBraces){
                offset = 2;
            }
            removefromCurrentPos(lastTokenLength, offset);

        }else if (input.equals("delete")){

            removefromCurrentPos(1, 0);

        }else if (input.equals("back")){

            moveCursorRelativeToCurrentPos(-1);
        }else if (input.equals("forward")){

            moveCursorRelativeToCurrentPos(1);
        }else{

            switch (input) {
                //String
                case "string":
                    text = "String";
                    break;

                //symbols
                case "semi-colon":
                    text = ";";
                    break;
                case "braces":
                    text = "{}";
                    goInBraces = true;
                    break;
                case "brackets":
                    text = "()";
                    goInBraces = true;
                    break;

                case "corner brackets":
                    text = "[]";
                    goInBraces = true;
                    break;

                case "quotes":
                    text = "\"\"";
                    goInBraces = true;
                    break;

                case "plus":
                    text = "+";
                    break;

                case "minus":
                    text = "-";
                    break;

                case "linechange":
                    text = "\n";
                    break;

                case "colon":
                    text = ":";
                    break;

                case "dot":
                    text = ".";
                    break;

                //assingment operators
                case "equals":
                    text = "=";
                    break;

                //infixOperators
                case "is less":
                    text = "<";
                    break;
                case "is greater":
                    text = ">";
                    break;

                case "multiply":
                    text = "*";
                    break;
                case "divide":
                    text = "/";
                    break;

                case "logical and":
                    text = "&&";
                    break;

                case "logical or":
                    text = "||";
                    break;

                case "logical not":
                    text = "!";
                    break;

                case "modulus":
                    text = "%";
                    break;

                //commands
                case "indent":
                    text = indent;
                    break;

                case "space":
                    break;

                case "print":
                    text = "System.out.println()";
                    goInBraces = true;
                    break;

                //statements
                case "if statement":
                case "for statement":
                case "while statement":
                case "switch case":
                    text = input.split(" ")[0] + "()";
                    goInBraces = true;
                    break;

                case "else statement":
                case "try statement":
                    text = input.split(" ")[0] + "{}";
                    goInBraces = true;
                    break;

                case "do while":
                    text = "do{\n\n}while()";
                    goInBraces = true;
                    break;
                case "else if":
                    text = input+"()";
                    goInBraces = true;
                    break;

                //misc
                case "catch":
                    text = input+"()";
                    goInBraces = true;
                    break;

                case "default":
                    text = input+":";
                    break;

                case "finally":
                    text = input+"{}";
                    goInBraces = true;
                    break;

                case "instance of":
                    text = "instanceof";
                    break;

                default:
                    text = input;
            }
            text +=" ";
        }

        lastTokenLength = text.length();
        lastGoInBraces = goInBraces;

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
        if(currentPos+steps<context.getCodeText().length() && currentPos+steps>=0){
            context.getCodeText().setSelection(currentPos+steps);
        }
    }

    private void removefromCurrentPos(int numOfChars, int offset){
        int currentPos =context.getCodeText().getSelectionStart();
        context.getCodeText().getText().delete(currentPos-numOfChars+offset, currentPos+offset);
    }
}
