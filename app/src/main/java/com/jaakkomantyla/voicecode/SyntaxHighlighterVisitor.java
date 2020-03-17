package com.jaakkomantyla.voicecode;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.Token;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import java.util.Scanner;

public class SyntaxHighlighterVisitor extends VoidVisitorAdapter<Void> {
    CodeEditText codeEditText;
    Spannable codeSpannable;
    private int annotationColor;
    private int methodColor;
    private int keywordColor;
    private int nameColor;
    private int stringLiteralColor;
    private int numberColor;
    private int commentColor;

    public SyntaxHighlighterVisitor(CodeEditText codeEditText){
        this.codeEditText = codeEditText;
        codeSpannable = new SpannableString(codeEditText.getText());

        annotationColor = Color.YELLOW;
        nameColor = Color.rgb(140,40,140);
        keywordColor = Color.rgb(255, 165, 0);
        methodColor = Color.rgb(255, 222, 0);
        stringLiteralColor = Color.rgb(100,255,100);
        numberColor = Color.rgb(100,100,255);
        commentColor = Color.rgb(100, 100, 100);
    }

    @Override
    public void visit(CompilationUnit cu, Void arg) {
        super.visit(cu, arg);
        codeEditText.setText(codeSpannable , TextView.BufferType.SPANNABLE);
    }


    //annotations
    @Override
    public void visit(MarkerAnnotationExpr ae, Void arg){
        super.visit(ae, arg);
        highlightNode(ae,codeSpannable,annotationColor);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Void arg) {
        super.visit(n, arg);
        highlightName( n, codeSpannable, annotationColor);
    }

    @Override
    public void visit(NormalAnnotationExpr n, Void arg) {
        super.visit(n, arg);
        highlightName( n, codeSpannable, annotationColor);
    }

    @Override
    public void visit(AnnotationDeclaration n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, annotationColor);
    }


    //variableNames
    @Override
    public void visit(NameExpr ne, Void arg){
        super.visit(ne, arg);
        highlightNode(ne,codeSpannable,nameColor);
    }

    @Override
    public void visit(VariableDeclarator n, Void arg){
        super.visit(n, arg);
        n.getType().ifClassOrInterfaceType(t->{
            if(t.getName().toString().equals("String")){
                highlightNode( t, codeSpannable, keywordColor);
            }else{
                highlightSimpleName(n,codeSpannable,nameColor);
            }
        });

    }

    //method & constrictor declarations
    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        highlightSimpleName( md, codeSpannable, methodColor);
    }

    @Override
    public void visit(ConstructorDeclaration cd, Void arg) {
        super.visit(cd, arg);
        highlightSimpleName( cd, codeSpannable, methodColor);
    }

    //literals
    @Override
    public void visit(BooleanLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(StringLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, stringLiteralColor);
    }

    @Override
    public void visit(CharLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, stringLiteralColor);
    }

    @Override
    public void visit(LongLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, numberColor);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, numberColor);
    }
    @Override
    public void visit(DoubleLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, numberColor);
    }

    //Comments

    @Override
    public void visit(LineComment n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, commentColor);
    }

    @Override
    public void visit(JavadocComment n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, commentColor);
    }

    @Override
    public void visit(BlockComment n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, commentColor);
    }


    //java keywords

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);
        n.getTokenRange().get().forEach(t -> {
            if(t.getText().equals("class") ||
            t.getText().equals("extends") ||
                    t.getText().equals("implements") ||
                    t.getText().equals("interface")){
                highlightToken(t,codeSpannable,keywordColor);
            }


        });

    }

    @Override
    public void visit(ForStmt fs, Void arg) {
        super.visit(fs, arg);
        highlightNodeStart( fs,3, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ForEachStmt fs, Void arg) {
        super.visit(fs, arg);
        highlightNodeStart( fs,3, codeSpannable, keywordColor);
    }

    @Override
    public void visit(IfStmt fs, Void arg) {
        super.visit(fs, arg);
        highlightNodeStart( fs,2, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ReturnStmt rs, Void arg) {
        super.visit(rs, arg);
        highlightNodeStart( rs,6, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ThrowStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,5, codeSpannable, keywordColor);
    }

    @Override
    public void visit(SwitchStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,6, codeSpannable, keywordColor);
    }

    @Override
    public void visit(SwitchEntry n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,4, codeSpannable, keywordColor);
    }

    @Override
    public void visit(SynchronizedStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,12, codeSpannable, keywordColor);
    }

    @Override
    public void visit(PackageDeclaration n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,7, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,6, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ObjectCreationExpr n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,3, codeSpannable, keywordColor);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,5, codeSpannable, keywordColor);
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,4, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,5, codeSpannable, keywordColor);
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,2, codeSpannable, keywordColor);
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,5, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ArrayCreationExpr n, Void arg) {
        super.visit(n, arg);
        highlightNodeStart( n,3, codeSpannable, keywordColor);
    }



    @Override
    public void visit(Modifier m, Void arg) {
        super.visit(m, arg);
        highlightNode( m, codeSpannable, keywordColor);
    }

    @Override
    public void visit(PrimitiveType pt, Void arg) {
        super.visit(pt, arg);
        highlightNode( pt, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ThisExpr t, Void arg) {
        super.visit(t, arg);
        highlightNode( t, codeSpannable, keywordColor);
    }

    @Override
    public void visit(SuperExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(NullLiteralExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(InstanceOfExpr n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(VoidType n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ArrayType n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(ContinueStmt n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(BreakStmt n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }

    @Override
    public void visit(AssertStmt n, Void arg) {
        super.visit(n, arg);
        highlightNode( n, codeSpannable, keywordColor);
    }






    public void highlightNodeStart( Node n,int numberOfChars, Spannable spannable, int color){
        String str = spannable.toString();
        int start = calculatePosInChars(str, n.getBegin().get());
        int end = start+numberOfChars;
        System.out.println("position start"+n.getBegin().get()+ " end " +n.getEnd().get() );
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void highlightNode( Node n, Spannable spannable, int color){
        String str = spannable.toString();
        int start = calculatePosInChars(str, n.getBegin().get());
        int end = calculatePosInChars(str, n.getEnd().get())+1;
        System.out.println("position start"+n.getBegin().get()+ " end " +n.getEnd().get() );
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public <N extends Node> void highlightName(NodeWithName<N> n, Spannable spannable, int color){
        highlightNode( n.getName(), spannable, color);
    }

    public <N extends Node> void highlightSimpleName(NodeWithSimpleName<N> n, Spannable spannable, int color){
        highlightNode( n.getName(), spannable, color);
    }

    public void highlightRange( int start, int end, Spannable spannable, int color){

        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void highlightToken(JavaToken token, Spannable spannable, int color){
        String str = spannable.toString();
        int start = calculatePosInChars(str, token.getRange().get().begin);
        int end = calculatePosInChars(str,  token.getRange().get().end)+1;
        spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private int calculateLinesAndColsToChars( String content, int lines, int cols) {
        Scanner reader = new Scanner( content);
        int distance = 0;
        for (int i = 1; i < lines; i++){
            distance += reader.nextLine().length() + 1;
        }
        distance += cols -1;
        return distance;
    }


}