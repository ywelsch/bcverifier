Invariant Specification Language
================================

The invariant specification language is used to describe the coupling invariants
between two libraries.

Syntax
------
	
	compilationunit ::= statement* 

	statement ::= 
		  invariant expr;
		| local invariant expr;
		| place identifier = expr where expr;	
		| programpoint identifier = programpointExpr;
	
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
    	| programpointExpr

	programpointExpr ::= line int in typedef

	parameterlist ::= (expr (, expr)*)?
	
	vardefs ::= vardef (, vardef)*
 
	vardef ::= typedef identifier

	typedef ::= (old|new) qualifiedidentifier
	
Available operators: 

- Some standard Java operators: + - * / %  == < <= > >= && ||
- Implication operator: ==>
- Correspondence relation operator: ~ 


Semantics
---------

The defined invariants have to hold at every observable point.
Invariants are assumed to hold at the beginning of each method call and
after returning from a method call. The invariant has to be proven at the end of each method
and before calling a method.

### Types

Currently the following types are supported:

- Java primitives: 
	- boolean
	- int
- Java class types with library version. The library version is either "old" or "new".
	The Java type can be referenced by the fully qualified name or just by the name of the class.
- Program points (e.g. line 10 in old Cell)
- Places (defined by place definitions)	

### Correspondence Relation Operator

The Correspondence Relation operator (~) expects two reference values: a value from the 
old library on the left hand side and a value from the new library on the right hand side.

An expression `o1 ~ o2` is true if and only if o1 and o2 are two objects in correspondence 
(see [Correspondence Relation](blablub.html#bum)) or o1 and o2 are both null.

### Built-in Functions

	boolean exposed(Object o)
	
Returns true when the object o is exposed to the context 
and false when it is internal to the library.

	boolean createdByCtxt(Object o)

Returns true when the object o is created by the context and false
when it was created by the library.

	boolean at(Place p, int sp)
	
Returns true if the stackframe with number sp is currently at place p. 

	boolean at(Place p)
	
Returns true if the program is currently at place p. 
This function is equivalent to calling at(p, sp1()) or at(p, sp2()).

	T stack(Place p, int sp, Expression<T> e)
	
This function evaluates the expression in the context of the place p. 
This means that local Java variables, that are visible at the given place, can be used.
The values of the variables will be taken from the stackframe given by sp.

	T stack(Place p, Expression<T> e)
	
Short form for `stack(p, sp1(), e)` or `stack(p, sp2(), e)`

	int sp1()
	int sp2()
	
Returns the current value of the stackpointer for the old (sp1) and the new (sp2) library.

### Well formed invariants

Invariants have to be well formed:

- There must not be any division by zero
- Null must not be dereferenced

To check whether an invariant is well formed, a separate proof obligation is generated
and it has to be shown before the invariant is assumed or has to be proven.

All boolean operators are short-circuit operators and evaluated from left to right. 
The right expression is only evaluated if the value of the left expression does not already
fix the value of the overall expression. Therefore expression a) is well formed and
expression b) is not well formed in the following example.

	a) x > 0    && y/x == 2
	b) y/x == 2 && x > 0
 
The order in which invariants are defined is important. Invariants defined at the top
can be used to show that following invariants are well formed. In the following example
the first invariant states that c.x is never zero and thus the second  invariant is
well formed. If the invariants were defined in reverse error, the well formedness of
the division could not be shown.
	
	invariant forall C c :: c.x != 0
	invariant forall C c :: 10 / c.x > 3 


