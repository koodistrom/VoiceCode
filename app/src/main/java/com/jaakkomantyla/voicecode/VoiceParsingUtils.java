package com.jaakkomantyla.voicecode;

public class VoiceParsingUtils {

    public static String checkForKeyWords(String  input){

        if(input.length() > 6 && input.substring(0,6).equals("numeric")){
            return input.substring(7);
        }
        switch(input)
        {
            case "semi-colon":
                return ";\n";
            case "braces":
                return "{}";

            case "brackets":
                return "()";

            case "linechange":
                return "\n";
            case "equals":
                return "=";
            default:
                return input;
        }


    }
}
