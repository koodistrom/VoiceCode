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

//TODO: fix undo/redo stacks emptying on rotate
public class CodeEditText extends androidx.appcompat.widget.AppCompatEditText {

        private Rect rect;
        private Paint paint;
        private CompilationUnit testParse;
        private SyntaxHighlighterVisitor highlighterVisitor;
    private Stack<TextInput> undoStack;
    private Stack<TextInput> redoStack;




        public CodeEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            rect = new Rect();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.LTGRAY);
            paint.setTextSize(15);

            redoStack = new Stack<>();
            undoStack = new Stack<>();


            String testCode = context.getString(R.string.test_code);
            testParse = StaticJavaParser.parse(testCode);
            this.setText(testCode);

            highlighterVisitor = new SyntaxHighlighterVisitor(this);
            highlighterVisitor.visit(testParse, null);

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

        public void inputText (String text){
            addToCursorLocation(text);
            undoStack.push(new TextInput(text, getSelectionStart()-text.length()));
            redoStack.empty();
            if(undoStack.size()>10){
                undoStack.remove(9);
            }
        }

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

    class TextInput{
        int location;
        String text;
        int length;
        boolean isDeleted;
        public TextInput(String text, int location){
            this.text = text;
            this.location = location;
            this.length = text.length();
            this.isDeleted = false;
        }

        public TextInput(String text, int location, boolean isDeleted){
            this.text = text;
            this.location = location;
            this.length = text.length();
            this.isDeleted = true;
        }

        public int getLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(boolean deleted) {
            isDeleted = deleted;
        }
    }


    }

