package de.unikl.bcverifier.isl.parser;

import beaver.Symbol;
import beaver.Scanner;
import de.unikl.bcverifier.isl.parser.ISLParser.Terminals;


%%

%public
%final
%class ISLScanner
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

Identifier  = [:letter:] ([:letter:] | [:digit:] | "_")*
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
 "true"      { return sym(Terminals.TRUE); }
 "false"      { return sym(Terminals.FALSE); }
 "old"      { return sym(Terminals.OLD); }
 "new"      { return sym(Terminals.NEW); }
 "if"      { return sym(Terminals.IF); }
 "then"      { return sym(Terminals.THEN); }
 "else"      { return sym(Terminals.ELSE); }
 "null"		{ return sym(Terminals.NULL); }
 "invariant" { return sym(Terminals.INVARIANT); }
}

//Separators and operators
<YYINITIAL> {
"("           { return sym(Terminals.LPAREN); }
")"           { return sym(Terminals.RPAREN); }
// "["           { return sym(Terminals.LBRACKET); }
// "]"           { return sym(Terminals.RBRACKET); }
 ","           { return sym(Terminals.COMMA); }
"==>"           { return sym(Terminals.IMPLIES); }
"&&"			{ return sym(Terminals.AND); }
"||"			{ return sym(Terminals.OR); }
"~"           { return sym(Terminals.RELATED); }
"."           { return sym(Terminals.DOT); }
"%"          { return sym(Terminals.MOD); }
"=="          { return sym(Terminals.EQUALS); }
"!="          { return sym(Terminals.UNEQUALS); }
"!"				{ return sym(Terminals.NOT); }
"::"			{ return sym(Terminals.COLONCOLON); }
";"			{ return sym(Terminals.SEMI); }
}


//Literals
<YYINITIAL> {
//    \"            { yybegin(STRING); string.setLength(0);  }
    {IntLiteral}  { return sym(Terminals.INTLITERAL); }
}


<YYINITIAL> {
    {Identifier}  { return sym(Terminals.IDENTIFIER); }
//    {TypeIdentifier}  { return sym(Terminals.TYPE_IDENTIFIER); }
	{Comment}     { /* discard token */ }
	{WhiteSpace}  { /* discard token */ }
}



/*
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
*/

.|\n          { return sym(Terminals.INVALIDTOKEN); }
<<EOF>>       { return sym(Terminals.EOF); }