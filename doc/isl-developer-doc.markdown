ISL Developer Documentation
===========================

The translation is described using two functions:

- TR() translates an isl expression into a boogie expression
- WD() generates the boogie well-definedness condition for an isl expression

Translation
-----------

- Translation is implemented in de.unikl.bcverifier.isl.translation.ExprTranslation.
- Only the non-trivial translations are listed here.
- heapX is replaced with heap1 or heap2 depending on the context (the same holds for stackX, ipX, etc.)

### Correspondence relation

	TR(a ~ b) := RelNull(TR(a), TR(b), related)
	
### Forall expression
	
	TR(forall T_1 o_1, ..., T_n o_n :: expr) := 
		forall Ref o_1, ..., Ref o_n :: 
			Obj(heapX, o_1) && RefOfType(o_1, heapX, $T_1)
			&& ... 
			&& Obj(heapX, on) && RefOfType(o_n, heapX, $T_n) 
			==> TR(expr)  

### Field access

	// assuming o is of type C and has a field f of type int or Object  (or a subtype)
	TR(o.f) := heapX[TR(o), $T.f]
	
	// assuming o is of type C and has a field f of type boolean
	TR(o.f) := int2bool(heapX[TR(o), $T.f])

### Local variable access
	
	// assuming i references a local Java variable in a context with stackpointer sp
	// and i has type int or Object (or a subtype)
	TR(i) := stackX[ipX][sp][register(i)]
	
	// i has type boolean
	TR(i) := int2bool(stackX[ipX][sp][register(i)])
	
### instanceof

	TR(a instanceof C) := isInstanceOf(TR(a), heap1, C) 

isInstanceOf is defined as in Java: 
	
	isInstanceOf(o, heap, t) <==> o != null && isOfType(o, heap, t))

### Builtin functions

	TR(at(p, sp)) := stackX[ipX][TR(sp)][place] == p
	
	TR(at(p)) := TR(at(p, spX()))
	
	TR(sp1()) := spmap1[ip1]
	TR(sp2()) := spmap2[ip2]
	
	TR(exposed(o)) := heapX[TR(o), exposed]
	
	TR(createdByCtxt(o)) := heapX[TR(o), createdByCtxt]
	
	TR(stack(p, sp, e)) := TR(e)
	TR(stack(p, e)) := TR(e)
	
The builtin function `stack` has no direct effect on the translation, but it provides the context
for accessing the local variables in `e`.

Well-definedness
----------------

- Well-definedness of expressions is implemented in de.unikl.bcverifier.isl.translation.ExprWellDefinedness.
- Only the non-trivial translations are listed here.


	WD(if e then e1 else e2) := WD(e) && if TR(e) then WD(e1) else WD(e2)
	
	WD(o.f) := WD(o) && TR(o) != null
	
	WD(a && b) := WD(a) && (TR(a) ==> WD(b))
	
	WD(a ==> b) := WD(a && b)
	
	WD(a || b) := WD(left) && (!TR(left) ==> WD(right))
	
	WD(a / b) := WD(a) && WD(b) && TR(b) != 0
	
	WD(a % b) := WD(a / b)
	
	WD(forall x :: e) := forall x :: WD(e)

	WD(exists x :: e) := forall x :: WD(e)
	
### Builtin functions

	WD(at(p, sp)) := WD(sp) && 0 <= TR(sp) && tr <= TR(spX())
	
	WD(stack(p, sp, e) := WD(at(p, sp)) && TR(at(p, sp)) && WD(e)
	
	WD(stack(p, e) := TR(at(p)) && WD(e)

	WD(exposed(e)) := TR(e) != null

	WD(createdByCtxt(e)) := TR(e) != null
