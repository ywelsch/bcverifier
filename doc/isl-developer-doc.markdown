ISL Developer Documentation
===========================

The translation is described using two functions:

- tr() translates an isl expression into a boogie expression
- wd() generates the boogie well-definedness condition for an isl expression

Translation
-----------

- Translation is implemented in de.unikl.bcverifier.isl.translation.ExprTranslation.
- Only the non-trivial translations are listed here.
- heapX is replaced with heap1 or heap2 depending on the context (the same holds for stackX, ipX, etc.)

### Correspondence relation

	tr(a ~ b) := RelNull(tr(a), tr(b), related)
	
### Forall expression
	
	tr(forall T_1 o_1, ..., T_n o_n :: expr) := 
		forall Ref o_1, ..., Ref o_n :: 
			Obj(heapX, o_1) && RefOfType(o_1, heapX, $T_1)
			&& ... 
			&& Obj(heapX, on) && RefOfType(o_n, heapX, $T_n) 
			==> tr(expr)  

### Field access

	// assuming o is of type C and has a field f of type int or Object  (or a subtype)
	tr(o.f) := heapX[tr(o), $T.f]
	
	// assuming o is of type C and has a field f of type boolean
	tr(o.f) := int2bool(heapX[tr(o), $T.f])

### Local variable access
	
	// assuming i references a local Java variable in a context with stackpointer sp
	// and i has type int or Object (or a subtype)
	tr(i) := stackX[ipX][sp][register(i)]
	
	// i has type boolean
	tr(i) := int2bool(stackX[ipX][sp][register(i)])
	
### Builtin functions

	tr(at(p, sp)) := stackX[ipX][tr(sp)][place] == p
	
	tr(at(p)) := tr(at(p, spX()))
	
	tr(sp1()) := spmap1[ip1]
	tr(sp2()) := spmap2[ip2]
	
	tr(exposed(o)) := heapX[tr(o), exposed]
	
	tr(createdByCtxt(o)) := heapX[tr(o), createdByCtxt]
	
	tr(stack(p, sp, e)) := tr(e)
	tr(stack(p, e)) := tr(e)
	
The builtin function `stack` has no direct effect on the translation, but it provides the context
for accessing the local variables in `e`.

Well-definedness
----------------

- Well-definedness of expressions is implemented in de.unikl.bcverifier.isl.translation.ExprWellDefinedness.
- Only the non-trivial translations are listed here.


	wd(if e then e1 else e2) := wd(e) && if tr(e) then wd(e1) else wd(e2)
	
	wd(o.f) := wd(o) && tr(o) != null
	
	wd(a && b) := wd(a) && (tr(a) ==> wd(b))
	
	wd(a ==> b) := wd(a && b)
	
	wd(a || b) := wd(left) && (!tr(left) ==> wd(right))
	
	wd(a / b) := wd(a) && wd(b) && tr(b) != 0
	
	wd(a % b) := wd(a / b)
	
	wd(forall x :: e) := forall x :: wd(e)
	
### Builtin functions

	wd(at(p, sp)) := wd(sp) && 0 <= tr(sp) && tr <= tr(spX())
	
	wd(stack(p, sp, e) := wd(at(p, sp)) && tr(at(p, sp)) && wd(e)
	
	wd(stack(p, e) := tr(at(p)) && wd(e)
