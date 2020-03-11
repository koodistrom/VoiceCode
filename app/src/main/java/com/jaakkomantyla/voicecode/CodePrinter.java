package com.jaakkomantyla.voicecode;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

public class CodePrinter {


    private final PrettyPrinterConfiguration configuration;

    public CodePrinter() {
        this(new PrettyPrinterConfiguration());
    }

    public CodePrinter(PrettyPrinterConfiguration configuration) {
        this.configuration = configuration;
    }

    public String print(Node node) {
        final VoidVisitor<Void> visitor = configuration.getVisitorFactory().apply(configuration);
        node.accept(visitor, null);
        return visitor.toString();
    }
}