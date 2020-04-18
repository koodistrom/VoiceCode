package com.jaakkomantyla.voicecode;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import dalvik.system.DexClassLoader;

public class JavaFileHandler {

    ConsoleFragment consoleFragment;
    Writer writer;
    ConsoleOutPut soutOutPut;
    ConsoleOutPut errOutPut;
    Context context;
    org.eclipse.jdt.internal.compiler.batch.Main eclipseCompiler;
    File storage;

    public JavaFileHandler(ConsoleFragment consoleFragment){
        this.consoleFragment = consoleFragment;
        this.soutOutPut = consoleFragment.getSoutOutPut();
        errOutPut = consoleFragment.getErrOutPut();
        context = consoleFragment.getContext();
        storage = context.getDir("all41", Context.MODE_PRIVATE);
        reDirectOutputs();
        prepareCompiler();

    }

    public void compileAndRun(String path){
        compile(path);
        File javaFile = new File(path);
        String className = javaFile.getName().split("\\.java")[0];
        String classPath = javaFile.getParent()+ "/./"+className+".class";
        dexify(classPath);
        run(className);

    }

    private void reDirectOutputs(){
        try{
            Field outField = System.class.getDeclaredField("out");
            Field modifiersField = Field.class.getDeclaredField("accessFlags");
            modifiersField.setAccessible(true);
            modifiersField.set(outField, outField.getModifiers() & ~Modifier.FINAL);
            outField.setAccessible(true);

            outField.set(null, new PrintStream(soutOutPut));

            Field errField = System.class.getDeclaredField("err");
            modifiersField.set(errField, errField.getModifiers() & ~Modifier.FINAL);
            errField.setAccessible(true);

            errField.set(null, new PrintStream(errOutPut));

        }catch(NoSuchFieldException e){
            e.printStackTrace();
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }


    }

    private void prepareCompiler(){

        System.out.println("copying the android.jar from asssets to the internal storage to make it available to the compiler");
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


        } catch (Exception e) {
            System.out.println("Error while copying from assets: " + e.getMessage());
            e.printStackTrace();
        }


        System.out.println("instantiating the compiler and compiling the java file");
        eclipseCompiler =
            new org.eclipse.jdt.internal.compiler.batch.Main(new PrintWriter(System.out), new PrintWriter(System.err),
            false/*noSystemExit*/, null);

    }

    public void compile(String path){
        eclipseCompiler.compile(new String[] {"-classpath", storage.getAbsolutePath()+"/android.jar",
                path});
    }

    private void dexify( String classPath){
        System.out.println("calling DEX and dexifying class: " + classPath);

        com.android.dx.command.Main.main(new String[] {"--dex", "--output=" + storage.getAbsolutePath() + "/Dexified.zip", classPath});
    }

    public void run(String className){

        System.out.println("instantiating DexClassLoader, loading class and invoking main method");
        DexClassLoader cl = new DexClassLoader(storage.getAbsolutePath() + "/Dexified.zip", storage.getAbsolutePath(), null, context.getClassLoader());
        try {
            Class libProviderClazz = cl.loadClass(className );
            Object instance = libProviderClazz.newInstance();

            Class[] cArg = new Class[1];
            cArg[0] = String[].class;

            Method main = null;
            try {
                main = libProviderClazz.getDeclaredMethod("main", cArg);
            } catch (NoSuchMethodException | SecurityException e) {

                System.out.println("No main method");
            }
            if(main != null){

                Object[] args = new Object[1];
                args[0] = new String[]{};
                main.invoke(instance, args);

            }

            //System.err.println(instance.toString());
        } catch (Exception e) {
            System.err.println("Error while instanciating object: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
