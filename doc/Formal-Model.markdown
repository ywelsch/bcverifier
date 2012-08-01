Formal Model
============

In this section, we explain the formal model underlying the verification approach. For more details, we refer to the papers [cite papers].

Proving backward compatibility of two library implementations is done by using special simulation relations. The library developer specifies the relation using a so-called "coupling invariant" which describes how the old library implementation is related to the new implementation. The BCVerifier tool then proves that the relation induced by the coupling invariant has the simulation property.

Program Configurations
----------------------

The idea behind our verification method is based on the states where control is outside of the library, which we call the observable states. The relation induced by the coupling invariant that relates the configurations of both libraries must hold at corresponding observable states in the execution. As libraries are comprised of multiple classes, the observable states are not statically bound to program points like start and end of methods.

In order to be able to relate the parts of the configurations which belong to the respective library implementations, we must describe what part of the configuration actually is part of the library implementation and which part is part of the program context.

A program configuration usually consists of a stack and a heap. A stack is a sequence of stack frames. Stack frames are created by method invocations. If the body of the invoked method is defined in the library, then we say that the stack frame belongs to the library; otherwise it is part of the program context. We group consecutive  stack frames that have the same origin into interaction frames. A well-formed stack then consists of an alternating sequence of interaction frames that belong to the library and interaction frames that belong to the program context. The interaction frame at the bottom of the stack belongs to the program context as execution starts in the program context (with a main method).

The simulation relation relates program configurations which have the same number of interaction frames and where the interaction frames of the program context are similar. This allows interaction frames of the library implementations to look completely different.

Separating the heap into a part that belongs to the program context and a part that belongs to the library is a bit more difficult. With inheritance, some code parts of a class/object can belong to the context and other parts to the library. We differentiate for fields whether they have been defined in classes of the program context or the library. Objects are then mappings from field names to values.
For the libraries to be indistinguishable, the state reachable from interaction frames of the program context must be similar. To better characterize the state reachable by the program context, we distinguish
- which objects have been created by code of the library or by code of the context. We use the predicate createdByCtxt(o) to denote whether the object o has been created by code of the program context; if the predicate does not hold, then the object was created by code of the library.
- which objects created by the program context have been made known to the library or vice-versa. We use the predicate exposed(o) to denote whether the object o has been exposed (by either the program context or the library).
The objects which are reachable by interaction frames of the program context are then only objects which have been created by program context or those which have been created by the library and which have been exposed.

The simulation relation relates program configurations for which there is a bijective renaming between the exposed objects of the first program configuration and the exposed objects of the second program configuration. The two library implementations might however still create "different" objects as long as these are not exposed to the program context. Exposed objects can have different dynamic types but must have the same public super types as the context can only use the public types to distinguish them.
Similarly, there must be a bijective renaming between the objects created by the program context of the first and second configuration.


Due to the non-deterministic choice of object identifiers, there is a bijective renaming between objects of the old and objects of the new configuration.