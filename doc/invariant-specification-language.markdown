Invariant Specification Language
================================

The invariant specification language is used to describe the coupling invariants
between two libraries.

Syntax
------
	
	compilation unit ::= statement* 

	statement ::= invariant expr;
	
	expr ::= 
		  forall vardefs :: expr
    	| constant  
    	| identifier
    	| identifier(parameterlist)
    	| expr.identifier
    	| expr binaryoperator expr
    	| unaryoperator expr
    	| if expr then expr else expr
    	| (expr)

	parameterlist ::= (expr (, expr)*)?
	
	vardefs ::= (vardef (, vardef)*)?
 
	vardef ::= typedef identifier

	typedef ::= (old|new) qualifiedidentifier
	
Available operators: 

- Some standard Java operators: + - * / %  == < <= > >= && ||
- Implies operator: ==>
- Relation operator: ~ 

...
	
### Example

	invariant forall old Cell o1, new Cell o2 ::
					o1 ~ o2 ==>	if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2;
					
This invariant states that for all Cell objects o1 from the old library
and all Cell objects o2 from the new library the the following holds:
If o1 and o2 are in relation then the value stored in field o1.c is in relation 
with the value o2.c1 or o2.c2 depending on the value of o2.f.