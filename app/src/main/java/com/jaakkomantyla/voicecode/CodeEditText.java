package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;




public class CodeEditText extends androidx.appcompat.widget.AppCompatEditText {

        private Rect rect;
        private Paint paint;
        private CompilationUnit testParse;
        private SyntaxHighlighterVisitor highlighterVisitor;


        public CodeEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            rect = new Rect();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.LTGRAY);
            paint.setTextSize(15);



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


    }

