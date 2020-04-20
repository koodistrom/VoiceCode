package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Stack;

import androidx.annotation.NonNull;

/**
 * Class extending Android EditText holding special methods and fields for displaying java source code.
 */

public class CodeEditText extends androidx.appcompat.widget.AppCompatEditText {

    private Rect rect;
    private Paint paint;
    private CompilationUnit parsed;
    private SyntaxHighlighterVisitor highlighterVisitor;
    private Stack<TextInput> undoStack;
    private Stack<TextInput> redoStack;

    /**
     * Instantiates a new Code edit text.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CodeEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            rect = new Rect();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.LTGRAY);
            paint.setTextSize(15);

            redoStack = new Stack<>();
            undoStack = new Stack<>();


            openNew(context.getString (R. string.test_code));


        }

        @Override
        protected void onDraw(Canvas canvas) {
            int baseline = getBaseline();
            for (int i = 0; i < getLineCount(); i++) {
                canvas.drawText("" + (i+1), rect.left, baseline, paint);
                baseline += getLineHeight();
            }
            super.onDraw(canvas);
        }

    /**
     * Open new is called when displaying source code from new java file. Method highlights the
     * syntax and displays the code on view.
     *
     * @param textFromFile the text from file
     */
    public void openNew(String textFromFile){
            setText(textFromFile);
            try {
                parsed = StaticJavaParser.parse(textFromFile);
                highlighterVisitor = new SyntaxHighlighterVisitor(this);
                highlighterVisitor.visit(parsed, null);
            }catch (Exception e){
                System.err.println(e);
            }
        }

    /**
     * Input text. Is called when user inputs new text to the view. Method adds new text and adds
     * it to undo stack so it can be undone later if needed.
     *
     * @param text the text
     */
    public void inputText (String text){
            addToCursorLocation(text);
            undoStack.push(new TextInput(text, getSelectionStart()-text.length()));
            redoStack.empty();
            if(undoStack.size()>10){
                undoStack.remove(0);
            }
        }

    /**
     * Undo.
     */
    public void undo(){
            if(!undoStack.isEmpty()) {
                TextInput undone = undoStack.pop();
                if (!undone.isDeleted) {
                    getText().delete(undone.getLocation(), undone.getLocation() + undone.getLength());
                } else {
                    getText().insert(undone.getLocation(), undone.getText());
                }

                undone.setDeleted(!undone.isDeleted());
                redoStack.push(undone);
            }
        }

    /**
     * Redo.
     */
    public void redo(){

            if(!redoStack.isEmpty()) {
                TextInput redone = redoStack.pop();
                if (!redone.isDeleted) {
                    getText().delete(redone.getLocation(), redone.getLocation() + redone.getLength());
                } else {
                    getText().insert(redone.getLocation(), redone.getText());
                }
                redone.setDeleted(!redone.isDeleted());
                undoStack.push(redone);
            }
        }

    /**
     * Delete.
     */
    public void delete(){
            String deleted = String.valueOf(getText().charAt(getSelectionStart()-1));
            undoStack.push(new TextInput(deleted, getSelectionStart()-1, true));
            getText().delete(getSelectionStart()-1, getSelectionStart());

        }

    private void addToCursorLocation(String text){

        int start = Math.max(getSelectionStart(), 0);
        int end = Math.max(getSelectionEnd(), 0);
        getText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());
    }

    /**
     * Moves cursor relative to current position. Used for example when adding brackets to
     * move the cursor afterwards inside them automatically.
     *
     * @param steps how much to move the cursor
     */
    public void moveCursorRelativeToCurrentPos(int steps){
        int currentPos = getSelectionStart();
        if(currentPos+steps<length() && currentPos+steps>=0){
            setSelection(currentPos+steps);
        }
    }

    private void removefromCurrentPos(int numOfChars, int offset){
        int currentPos = getSelectionStart();
        getText().delete(currentPos-numOfChars+offset, currentPos+offset);
    }

    /**
     * Zeroes the view by setting text and undo and redo stacks empty.
     */
    public void zero(){
        setText("");
        redoStack = new Stack<>();
        undoStack = new Stack<>();

    }

    /**
     * Text input is A class that undo and redo stacks use. It holds information of the content
     * and location of each input.
     */
    class TextInput{
        /**
         * The Location from the start of the text as char index.
         */
        int location;
        /**
         * The Text inputted.
         */
        String text;
        /**
         * The Length of the text.
         */
        int length;
        /**
         * The Is deleted boolean is true if user deleted text instead of adding.
         */
        boolean isDeleted;

        /**
         * Instantiates a new Text input.
         *
         * @param text     the text
         * @param location the location
         */
        public TextInput(String text, int location){
            this.text = text;
            this.location = location;
            this.length = text.length();
            this.isDeleted = false;
        }

        /**
         * Instantiates a new Text input.
         *
         * @param text      the text
         * @param location  the location
         * @param isDeleted the is deleted
         */
        public TextInput(String text, int location, boolean isDeleted){
            this.text = text;
            this.location = location;
            this.length = text.length();
            this.isDeleted = true;
        }


        /**
         * Gets location.
         *
         * @return the location
         */
        public int getLocation() {
            return location;
        }

        /**
         * Sets location.
         *
         * @param location the location
         */
        public void setLocation(int location) {
            this.location = location;
        }

        /**
         * Gets text.
         *
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * Sets text.
         *
         * @param text the text
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * Gets length.
         *
         * @return the length
         */
        public int getLength() {
            return length;
        }

        /**
         * Sets length.
         *
         * @param length the length
         */
        public void setLength(int length) {
            this.length = length;
        }

        /**
         * Is deleted boolean.
         *
         * @return the boolean
         */
        public boolean isDeleted() {
            return isDeleted;
        }

        /**
         * Sets deleted.
         *
         * @param deleted the deleted
         */
        public void setDeleted(boolean deleted) {
            isDeleted = deleted;
        }

        @NonNull
        @Override
        public String toString() {
            return text;
        }
    }


    }

