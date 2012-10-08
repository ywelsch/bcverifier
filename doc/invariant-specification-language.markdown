Invariant Specification Language
================================

The invariant specification language (ISL) is used to describe the coupling invariants
between two libraries.

Syntax
------

Non-terminals are represented in ALL CAPS. We use the meta-symbol `|` to denote alternatives and the brackets `[...]` to group elements. The meta-symbol`[X]?` is used to denote an optional item `X` and `[X]*` to denote an arbitrary sequence of elements `X`.
	
	COMPILATIONUNIT ::= [DECLARATION]* 

	DECLARATION ::= 
		  [local]? invariant EXPRESSION ;
		| [local | predefined] place IDENTIFIER = PLACEPOSITION ;
	
	PLACEPOSITION ::=
	      line INT of TYPEDEF [when EXPRESSION]?
	    | call IDENTIFIER in line INT of TYPEDEF
	
	EXPRESSION ::= 
		  [forall | exists] VARDEF [, VARDEF]* :: EXPRESSION
    	| CONSTANT // true, false, null, and integer constants
    	| IDENTIFIER
    	| IDENTIFIER ( [EXPRESSION [, EXPRESSION]*]? ) // function call
    	| EXPRESSION . IDENTIFIER // field access
    	| EXPRESSION BINARYOPERATOR EXPRESSION
    	| EXPRESSION instanceof TYPEDEF // Java instanceof operator
    	| UNARYOPERATOR EXPRESSION
    	| if EXPRESSION then EXPRESSION else EXPRESSION
    	| ( EXPRESSION ) // parenthesized expression
 
	VARDEF ::= TYPEDEF IDENTIFIER

	TYPEDEF ::= 
	      [old | new] IDENTIFIER
	    | Bijection
	      
	BINARYOPERATOR ::=
	      + | - | * | / |  % | == | != | < | <= | > | >= | && | || // Java operators
	    | ==> | <==> // other logical operators
	    | ~ // correspondence relation
	
	UNARYOPERATOR ::= 
	    ! | - // Java operators

### Types

Currently the following types are supported:

- Java primitive types `boolean` and `int`. Currently these types can not be declared.
- Java class types with library version. The library version is either `old` or `new`.
	The Java type can be referenced by the fully qualified name or just by the name of the class if it is unambiguous.
- Places (defined by place definitions).
- The special `Bijection` type defining bijective relations on reference values (i.e., object identifiers or `null`).

Semantics
---------

The global invariants have to hold at every observable point. Local invariants must hold at internal user-defined points and are used to prove global invariants.

### Correspondence Relation Operator

The correspondence operator `~` expects two reference values: a value from the 
old library on the left hand side and a value from the new library on the right hand side.

An expression `o1 ~ o2` is true if and only if `o1` and `o2` are two objects in correspondence 
(see [[Formal Model]]) or `o1` and `o2` are both null.

### Built-in Functions

ISL currently only supports built-in functions, presented in the following.

	boolean exposed(Object o)
	
Returns true when the object `o` is exposed to the context 
and false when it is internal to the library.

	boolean createdByCtxt(Object o)

Returns true when the object `o` is created by the context and false
when it was created by the library.

	boolean at(Place p, int sp)
	
Returns whether the stack frame with number `sp` is currently at place `p`. 

	boolean at(Place p)
	
Returns whether the program is currently at place `p`. 
This function is equivalent to calling `at(p, sp1()) or at(p, sp2())`.

	T stack(Place p, int sp, EXPRESSION<T> e)
	
This function evaluates the expression in the context of the place `p`. 
This means that local Java variables, that are visible at the given place, can be used.
The values of the variables will be taken from the stack frame given by `sp`.

	T stack(Place p, Expression<T> e)
	
Short form for `stack(p, sp1(), e)` or `stack(p, sp2(), e)`

	int sp1()
	int sp2()
	
Returns the number for the top-most stack frame for the old (`sp1`) and the new (`sp2`) library implementation.

	boolean related(Bijection b, Object o1, Object o2)
	
Returns whether the pair `o1`, `o2` is in the relation `b`.

### Well formed Invariants

Invariants have to be well formed:

- There must not be any division by zero
- `null` must not be dereferenced

To check whether an invariant is well formed, a separate proof obligation is generated.

All boolean operators are short-circuit operators and evaluated from left to right. 
The right expression is only evaluated if the value of the left expression does not already
fix the value of the overall expression. Therefore expression a) is well formed and
expression b) is not well formed in the following example.

	a) x > 0    && y/x == 2
	b) y/x == 2 && x > 0
 
The order in which invariants are defined is important. Invariants defined at the top
can be used to show that following invariants are well formed. In the following example
the first invariant states that c.x is never zero and thus the second invariant is
well formed. If the invariants were defined in reverse order, the well formedness of
the division could not be shown.
	
	invariant forall C c :: c.x != 0
	invariant forall C c :: 10 / c.x > 3 
