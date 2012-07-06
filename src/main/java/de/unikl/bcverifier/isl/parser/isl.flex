package de.unikl.bcverifier.isl.parser;

import beaver.Symbol;
import beaver.Scanner;
import de.unikl.bcverifier.isl.parser.parser.Terminals;

%%

%public
%final
%class ABSScanner
%extends Scanner
%unicode
%function nextToken
%type ISLSymbol
%yylexthrow Scanner.Exception
%line
%column
%char

%{
  StringBuffer string = new StringBuffer();

  private ISLSymbol sym(short id) {
    return new ISLSymbol(id, yyline + 1, yycolumn + 1, yylength(), yychar, yytext());
  }
  private ISLSymbol sym(short id, String text) {
    return new ISLSymbol(id, yyline + 1, yycolumn + 1, text.length(), yychar, text);
  }
  
  //private ISLSymbol symString(String text) {
  //    return new ISLSymbol(Terminals.STRINGLITERAL, yyline + 1, yycolumn + 1 - text.length(), text.length(), yychar-text.length(), text);
  //}
%}


// Helper Definitions

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]


//Comments
Comment = {TraditionalComment}	| {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "/*" "*"+ [^/*] ~"*/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?


//Identifiers defined using character classes
// BoolLiteral = [Tt]rue | [Ff]alse

Identifier  = [:lowercase:] ([:letter:] | [:digit:] | "_")*
TypeIdentifier  = [:uppercase:] ([:letter:] | [:digit:] | "_")*
IntLiteral = 0 | [1-9][0-9]*


//Alternative, explicit definition
//Alpha = [a-zA-Z]
//Identifier = {Alpha}({Alpha} | [:digit:] | "_")*
//ID       [a-z][a-z0-9]*

%state STRING



%% // Rules
//Keywords
// Important: if a new keyword is added also add it to Keywords.java
//            to get syntax highlighting in Eclipse
<YYINITIAL> {
 "forall"      { return sym(Terminals.FORALL); }
 
}
/*
//Separators
<YYINITIAL> {
 "("           { return sym(Terminals.LPAREN); }
 ")"           { return sym(Terminals.RPAREN); }
 "{"           { return sym(Terminals.LBRACE); }
 "}"           { return sym(Terminals.RBRACE); }
 "["           { return sym(Terminals.LBRACKET); }
 "]"           { return sym(Terminals.RBRACKET); }
 ","           { return sym(Terminals.COMMA); }
 ";"           { return sym(Terminals.SEMICOLON); }
 ":"           { return sym(Terminals.COLON); }
}

//Operators
<YYINITIAL> {
 "?"           { return sym(Terminals.QMARK); }
 ".."          { return sym(Terminals.UNTIL); }
 "."           { return sym(Terminals.DOT); }
 "!"           { return sym(Terminals.BANG); }
 "="           { return sym(Terminals.ASSIGN); }
 "&"           { return sym(Terminals.GUARDAND); }
 "=="          { return sym(Terminals.EQEQ); }
 "!="          { return sym(Terminals.NOTEQ); }
 "=>"          { return sym(Terminals.RARROW); }
 "->"          { return sym(Terminals.IMPLIES); }
 "<->"         { return sym(Terminals.EQUIV); }
  "+"	       { return sym(Terminals.PLUS); }
  "-"          { return sym(Terminals.MINUS); }
  "*"          { return sym(Terminals.MULT); }
  "/"          { return sym(Terminals.DIV); }
  "%"          { return sym(Terminals.MOD); }
 "&&"          { return sym(Terminals.ANDAND); }
 "||"          { return sym(Terminals.OROR); }
 "|"          { return sym(Terminals.BAR); }
 "~"          { return sym(Terminals.NEGATION); }
 "<"          { return sym(Terminals.LT); }
 ">"          { return sym(Terminals.GT); }
 "<="          { return sym(Terminals.LTEQ); }
 ">="          { return sym(Terminals.GTEQ); }
 "_"          { return sym(Terminals.USCORE); }
 "'"          { return sym(Terminals.PRIME); }
}

//Literals
<YYINITIAL> {
    \"            { yybegin(STRING); string.setLength(0);  }
    {IntLiteral}  { return sym(Terminals.INTLITERAL); }
//    {BoolLiteral} { return sym(Terminals.BOOLLITERAL); }
}

<YYINITIAL> {
    {Identifier}  { return sym(Terminals.IDENTIFIER); }
    {TypeIdentifier}  { return sym(Terminals.TYPE_IDENTIFIER); }
	{Comment}     { /* discard token */ }
	{WhiteSpace}  { /* discard token */ }
}




<STRING> {
 \"            { yybegin(YYINITIAL);
                 return symString(string.toString()); }
 [^\n\r\"\\]+  { string.append( yytext() ); }
 \\t           { string.append('\t'); }
 \\n           { string.append('\n'); }
 \\r           { string.append('\r'); }
 \\\"          { string.append('\"'); }
 \\            { string.append('\\'); }
}


.|\n          { return sym(Terminals.INVALID_CHARACTER); }
<<EOF>>       { return sym(Terminals.EOF); }
*/