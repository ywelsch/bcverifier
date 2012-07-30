Definitions
===========

 - `stack[f]` is an interaction frame of the library <==> `f % 2 == 1`
 - `stack[f][sp]` == stack frame of a method (of the context/library)
 - sp stack pointer of the running method of interaction frame f <==> sp == spmap[f]
 - `var ip: int` (interaction frame pointer)
 
 
Changes (compared to the previous model)
========================================
 
  - `stack1[sp1][<exp>] --> stack1[ip][spmap[ip]][<exp>]`
  - internal vs boundary calls
  - check (definition of a boundary call)
  - at points we return from a method call (result is either in different stack frame or different interaction frame)
  
  
internal vs. boundary calls
===========================

now
---

  sp := sp + 1;
  stack[sp1][param<i>] := X_i(sp - 1)
  goto call_boundary, call_internal;
  

future
------

    goto call_boundary, call_internal

  call_internal:
    spmap[ip] := spmap[ip] + 1;
    stack[ip][spmap[ip]][param<i>] := X_i(ip, spmap[ip] - 1);
    ...

  call_boundary:
    ip := ip + 1;
    spmap[ip] := 0;
    stack[ip][spmap[ip]][param<i>] := X_i(ip - 1, spmap[ip - 1]);