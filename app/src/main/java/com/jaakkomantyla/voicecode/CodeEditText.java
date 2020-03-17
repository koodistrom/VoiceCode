package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.util.Scanner;
import java.util.function.Consumer;



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

        public <T extends Node> void highlight(Class<T> nodeType, int color ){
            Spannable spannable = new SpannableString(this.getText());
            String str = spannable.toString();
            testParse.findAll(nodeType).stream()
                    .forEach(n->{
                        highlightNode(str,n,spannable,color);
                    });


            this.setText(spannable , TextView.BufferType.SPANNABLE);
        }



        public void highlightSyntax(){
            highlight(Modifier.class, Color.RED);
            highlight(NameExpr.class, Color.GREEN);
            highlight(AnnotationExpr.class, Color.YELLOW);

        }

        private int calculatePosInChars( String content, Position position) {
            Scanner reader = new Scanner( content);
            int distance = 0;
            for (int i = 1; i < position.line; i++){
                distance += reader.nextLine().length() + 1;
            }
            distance += position.column -1;
            return distance;
        }

        public void highlightNode(String str, Node n, Spannable spannable, int color){
            int start = calculatePosInChars(str, n.getBegin().get());
            int end = calculatePosInChars(str, n.getEnd().get())+1;
            System.out.println("position start"+n.getBegin().get()+ " end " +n.getEnd().get() );
            spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        public <N extends Node> void highlightName(String str, NodeWithSimpleName<N> n, Spannable spannable, int color){
            highlightNode(str, n.getName(), spannable, color);
        }
    }

