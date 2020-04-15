package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

import dalvik.system.DexClassLoader;

public class JavaCompilingUtils {

    public static void compile(Context context, String path, Writer writer){
        PrintWriter pw = new PrintWriter(writer, true);
        pw.println();
        File storage = context.getDir("all41", Context.MODE_PRIVATE);


        System.err.println("copying the android.jar from asssets to the internal storage to make it available to the compiler");
        pw.println("copying the android.jar from asssets to the internal storage to make it available to the compiler");
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        int BUF_SIZE = 8 * 1024;
        try {

            bis = new BufferedInputStream(context.getAssets().open("android.jar"));
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(storage.getAbsolutePath() + "/android.jar"));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();

            /*
            bis = new BufferedInputStream(context.getAssets().open("Test.java"));
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(storage.getAbsolutePath() + "/Test.java"));
            byte[] bufa = new byte[BUF_SIZE];
            int lena;
            while((lena = bis.read(bufa, 0, BUF_SIZE)) > 0) {
                dexWriter.write(bufa, 0, lena);
            }
            dexWriter.close();
            bis.close();*/
        } catch (Exception e) {
            pw.println("Error while copying from assets: " + e.getMessage());
            System.err.println("Error while copying from assets: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("path:" +
                context.getExternalFilesDir(null).getAbsolutePath()  );
        System.err.println("instantiating the compiler and compiling the java file");
        pw.println("instantiating the compiler and compiling the java file");
        org.eclipse.jdt.internal.compiler.batch.Main ecjMain =
        new org.eclipse.jdt.internal.compiler.batch.Main(new PrintWriter(writer, true),
        new PrintWriter(writer, true), false/*noSystemExit*/, null);
        ecjMain.compile(new String[] {"-classpath", storage.getAbsolutePath()+"/android.jar",
                path});

        File javaFile = new File(path);
        String className = javaFile.getName().split("\\.java")[0];
        String classPath = javaFile.getParent()+ "/./"+className+".class";
        System.err.println("calling DEX and dexifying the test class");

        System.out.println(classPath);
        com.android.dx.command.Main.main(new String[] {"--dex", "--output=" + storage.getAbsolutePath() + "/Test.zip", classPath});


        System.err.println("instantiating DexClassLoader, loading class and invoking toString()");
        DexClassLoader cl = new DexClassLoader(storage.getAbsolutePath() + "/Test.zip", storage.getAbsolutePath(), null, context.getClassLoader());
        try {
            Class libProviderClazz = cl.loadClass(className );
            Object instance = libProviderClazz.newInstance();
            System.err.println(instance.toString());
        } catch (Exception e) {
            System.err.println("Error while instanciating object: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
