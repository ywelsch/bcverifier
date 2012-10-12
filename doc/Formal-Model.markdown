Verification Approach
=====================

This section explains the verification approach and its underlying formal model. For a more technical presentation, the reader is referred to the papers ([1], [2], [3] and [4]).

[1]: http://dx.doi.org/10.1145/2318202.2318209 "Verifying Backward Compatibility of Object-Oriented Libraries Using Boogie"
[2]: https://softech.informatik.uni-kl.de/twiki/pub/Homepage/YannickWelsch/journal.pdf "A Fully Abstract Trace-based Semantics for Reasoning About Backward Compatibility of Class Libraries"
[3]: http://dx.doi.org/10.1007/978-3-642-25032-3_3 "Full Abstraction at Package Boundaries of Object-Oriented Languages"
[4]: http://dx.doi.org/10.1145/2245276.2232058 "A Type System for Checking Specialization of Packages in Object-Oriented Programming"

A prerequisite for backward compatibility is that the new library implementation provides at least the interface of the old implementation. In modern object-oriented languages such as Java, interfaces of libraries are complex due to the interplay of inheritance, subtyping, namespace mechanisms and accessibility modifiers. Depending on whether a library is distributed in source of binary form, two notions of *interface compatibility* are relevant. **Source compatibility** ensures that every program that *compiles* against the old library implementation also compiles against the new library implementation. **Binary compatibility** ensures that every program that *links* against the binary form of the old library also links against the binary form of the new implementation. Source and binary compatibility are incomparable; neither one is a superset of the other (see [5]). However, a set of (necessary and sufficient) checkable rules can be established for both forms of interface compatibility. The current theory of backward compatibility was developed in the setting where clients are recompiled with the new library implementation. In the following, only source compatible library implementations are considered. Nonetheless, large parts of the theory are directly transferable to a setting where binary compatibility is desired instead.

[5]: https://blogs.msdn.com/b/jmstall/archive/2008/03/10/binary-vs-source-compatibility.aspx?Redirected=true "Binary vs. Source compatibility"

Proving backward compatibility of two library implementations relies on a particular kind of simulation relation. The library developer specifies the relation using a so-called **coupling invariant** which describes how the old library implementation is related to the new implementation. The BCVerifier tool then proves that the relation induced by the coupling invariant has the simulation property.
The reasoning method is based on the states where control of execution is outside of the library. In a single-threaded setting, these are the states where a client can *observe* if two implementations behave differently. As libraries can call back into client code using dynamic dispatch (see [Callback example](./?example=cb)), the observable states are not statically bound to program points like start and end of library methods.
The relation only equates observable states where the behavior of the two library implementations is indistinguishable.
Backward compatibility is proven by checking that the relation induced by the coupling invariant is a proper simulation between programs with the first library implementation and programs with the second library implementation, which means that 1) the initial (observable) states are in the relation, and 2) computational steps between consecutive observable states are properly simulated. Here two cases can occur:

  - Computational steps where control of execution stays outside of the library.

  - Computational steps where control of execution goes inside the library and back out. This means that the library code gets to execute.

To define whether or not observable states of two library implementations are indistinguishable, it is necessary to know what part of the state results from code of the library implementation and which part results from the program that uses the library (also called **program context**).

Separation of state
-------------------

A program state usually consists of a stack and a heap. A *stack* is a sequence of stack frames. Stack frames are created by method invocations. If the body of the invoked method is defined in the library, then we say that the stack frame belongs to the library; otherwise it is part of the program context. We group consecutive  stack frames that have the same origin into *interaction frames*. A well-formed stack then consists of an alternating sequence of interaction frames that belong to the library and interaction frames that belong to the program context. The interaction frame at the bottom of the stack belongs to the program context as execution starts in the program context (with a main method).

Separating the *heap* into a part that belongs to the library and a part that belongs to the program context is a bit more difficult. With inheritance, some code parts of an object can belong to the context and other parts to the library. We differentiate for fields whether they have been defined in classes of the program context or the library. Objects can be considered as mappings from field names to values.
For the libraries to be indistinguishable, the heap state reachable from interaction frames of the program context must be similar. To better characterize which objects are potentially reachable by the program context, we distinguish

  - which objects have been created by code of the library or by code of the context.

  - which objects created by the program context have been made known to the library or vice-versa.

As the theory has to consider all possible program contexts, we assume that every object which has been made known to the program context at some point in time can later be used again by the program context. The objects which can appear in interaction frames of the program context are then only objects which have been created by the program context or those which have been created by the library and which have been exposed.

Indistinguishable states
------------------------

The simulation relation equates program states which have the same number of interaction frames and where the interaction frames of the program context are similar. This allows interaction frames of the library implementations to look completely different. Due to the non-deterministic choice of object identifiers, the interaction frames of the program context can only be identical modulo a renaming between objects of the old and objects of the new state. The renaming tracks which new objects take the place of the old objects and must be a bijective function in order to guarantee indistinguishability (otherwise an identity check "==" from the program context would yield true for one library implementation and false for the other).
The simulation relation thus equates program states for which there is a  renaming between the exposed objects of the first program state and the exposed objects of the second program state (we call this the **correspondence relation**). The two library implementations might however still create *different* objects as long as these are not exposed to the program context. Exposed objects can have different dynamic library types but must have the same public super types as the context can only use the public types to distinguish them. As we assume that code outside the library does not directly access fields that are defined in the library, we can abstract from the fields of classes that are defined in the library.
Similarly, there must be a renaming between the objects created by the program context of the first and second state. Here the runtime type of the objects must be exactly the same.

Proof obligations
-----------------

As most important step to prove backward compatibility, we need to show that computational steps between observable states are properly simulated. If control of execution goes inside the library and back out, this can only be due to a method call, method return or a constructor call or return. We thus have to consider calls of all available (public or protected) methods and constructors. Similarly, we have to consider all possible return points in code of the library where a method was called that could potentially lead to code of the program context to be executed.
We assume for the pre-states that they were related, which means that they are indistinguishable and satisfy the coupling invariant.
As the observable pre-states were indistinguishable and we had a method call, this means that the receiver/parameters of the call were indistinguishable and a method with same name was called. Similarly, if we had a method return, then the return values were indistinguishable. For the post-states (if they exist), we must prove that they are related again. This means that we need to prove that the coupling invariant still holds for the post-states. In order to satisfy indistinguishability, we need to check again in case of a method call whether the same method name was called and the receiver/parameters are indistinguishable, or for a method return whether the return values are indistinguishable.

Example
-------

Let us consider a very simple [Cell](./?example=cell) library which provides a `Cell` class to store and retrieve references to objects. In a more refined version of the `Cell`, a library developer might now want the possibility to not only retrieve the last value that was stored, but also the previous value. In the new implementation of the class, he therefore introduces two fields to store values and a boolean flag to determine which of the two fields stores the last value that was set.

    public class Cell<T> { // old library implementation
      private T c;
      public void set(T o) {c = o;}
      public T get() {return c;}
    }

    public class Cell<T> { // new library implementation
      private T c1, c2;
      private boolean f;
      public void set(T o) {
        f = !f;
        if(f) c1 = o; else c2 = o;
      }
      public T get(){
        return f ? c1 : c2;
      }
    }

This second representation allows to add a method to retrieve the previous value, e.g., `public Object previous() { return f ? c2 : c1; }`.

The developer might now wonder whether the old version of the library can be safely replaced with the new version, i.e., whether the new version of the `Cell` library still retains the behavior of the old version when used in program contexts of the old version. Intuitively, the developer might argue in the following way why he believes that both libraries are equivalent: If the boolean flag in the second library version is true, then the value that is stored in the field `c1` corresponds to the value that is stored in the field `c` in the first library version. Similarly, if the boolean flag is false, then the value that is stored in `c2` corresponds to the value that is stored in `c`.
Using the [[Invariant Specification Language]], this property can formally be defined as a coupling invariant:

    invariant forall old Cell o1, new Cell o2 ::
              o1 ~ o2 ==>  if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2;

The invariant specifies that if we have simulating observable program states, then for all `Cell` objects `o1` of the old library implementations and `Cell` objects `o2` of the new library implementation which are in correspondence (denoted `o1 ~ o2`), the aforementioned property holds.

Advanced verification features
------------------------------

Automatic verification in the setting of general recursion and loops is difficult.
Our solution is to provide various means to split the verification task into simpler subtasks.

### Local simulations

To prove steps that encompass methods with complex loops or recursion, we use **local simulation relations**. These auxiliary relations are used to prove properties that hold between certain non-observable states. The library developer specifies the program states for the old and new library implementation where the verification task is split at. These states are defined similarly to *conditional breakpoints* (debugging terminology) and called **local places**. Using the local places, the library developer can then establish local simulation relations that are specified using a **local invariant**. Local invariants are defined in a similar way as coupling invariants.

As an example for local places and local invariants, consider the [OneOffLoop example](./?example=oneOffLoop). The old implementation executes the loop once more than the new implementation. The local invariant establishes the relation between the second iteration of the loop in the old implementation and the first iteration of the loop in the new implementation. It is then proven to hold for each iteration and finally the local invariant in the last iteration guarantees that the values returned by both methods are the same.

More complex relations can be specified that allow to relate one state in one implementation to many states in the other implementation. We say that one of the implementations is **stalled** while the other executes. Only one implementation can be stalled at a time. If the first implementation is stalled, then we additionally need to prove that the second implementation is not diverging. This is done by specifying a **termination measure**. The application of stalling places and termination measure can be found in the [Termination example](./?example=measure).