package de.unikl.bcverifier.isl.parser;

import beaver.Symbol;

public class ISLSymbol extends Symbol {
    private final int abspos;
    public ISLSymbol(short id, int line, int column, int length, int abspos, Object text) {
        super(id, line, column, length, text);
        this.abspos = abspos;
    }
    
    public int getAbsolutePosition() {
        return abspos;
    }
}
