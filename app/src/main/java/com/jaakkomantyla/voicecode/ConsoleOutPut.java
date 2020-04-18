package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.graphics.Color;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream for redirecting System.out and compiler outputs to app's consoleFragment.
 */
public class ConsoleOutPut  extends OutputStream {
        private String mCache;
        private ConsoleFragment console;
        private String source;
        private int textColor;

    /**
     * Instantiates a new Console out put.
     *
     * @param console   the consoleFragment to display output in
     * @param source    the source prefix to be printed in front of output e.g. "System.out"
     * @param textColor the text color
     */
    public ConsoleOutPut(ConsoleFragment console, String source, int textColor) {
            this.console = console;
            this.source = source;
            this.textColor = textColor;
        }

        @Override
        public void write(int b) throws IOException {
            if(mCache == null) mCache = "";

            if(((char) b) == '\n'){
                console.print(source + mCache + "\n", textColor);
                mCache = "";
            }else{
                mCache += (char) b;
            }
        }
    }

