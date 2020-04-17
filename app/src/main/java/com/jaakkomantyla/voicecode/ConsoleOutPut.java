package com.jaakkomantyla.voicecode;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleOutPut  extends OutputStream {
        private String mCache;
        private ConsoleFragment console;
        public ConsoleOutPut(MainActivity context) {
            console = context.getConsoleFragment();
        }

        @Override
        public void write(int b) throws IOException {
            if(mCache == null) mCache = "";

            if(((char) b) == '\n'){
                console.print("System.out: " + mCache + "\n");
                mCache = "";
            }else{
                mCache += (char) b;
            }
        }
    }

