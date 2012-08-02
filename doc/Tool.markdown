Tool
====

Library Implementations
-----------------------

The tool BCVerifier is a standalone tool for checking backward compatibility of to Java implementations. It uses the formal model described in "the other section" [ref]. As input the tool uses two implementations made in Java. The implementations that should be checked need to be available as Java source code in two directories. The package structure must be obeyed as usual. These two folders are passed to the verifier using the command line argument `-l <old_implementation> <new_implementation>` where `<old_implementation>` is the path to the source folder of the old library implementation and `<new_implementation>` is the path to the source folder of the new library implementation.

The Java source files are checked for source compatibility first. Source compatibility is a precondition for behavioral backward compatibility. If two library implementations are source compatible then both implementations can be combined with the same program contexts yielding valid Java programs each. [conditions here or in the Formal Model chapter?] By specifying the command line argument `-c` the library files are compiled using the right compiler flags to support all the functionality of the tool.


Coupling Invariant
------------------

The coupling invariant can be specified using one of two possible syntaxes.

  - The Boogie syntax uses pure Boogie expressions. No afford is made to check that the invariant specified is valid with respect to inconsistencies with the prelude or other essential syntactic and semantic restrictions. The Boogie specifications are directly inserted as is into the generated Boogie program. *Use with care!*

  - The Invariant Specification Syntax is a high level specification language assisting to specify the coupling invariant. Files in this language are validated against the Java code and transformed into a consistent Boogie specification.

Both types of specifications are specified using the command line argument `-s <invariant_file>` where `<invariant_file>` is the path to the specification.


Layout of the Boogie Program
----------------------------

Both libraries are transformed into one Boogie procedure by analyzing the byte code of the library implementations. The prelude includes all axioms essential for the verification process. The Boogie procedure `checkLibraries` simulates all possible method calls of both library implementations. The body of this procedure is divided into basic blocks.

The basic blocks starting with `preconditions` include the preconditions of the possible interactions with the library like method calls, constructor calls and method returns into a library method. The coupling invariant is assumed in these blocks. The blocks starting with `check` include the checks that need to be performed when the control switches from the library to the context (boundary call or boundary return) or a local place is reached during simulation. Included in these blocks is the check of the coupling invariant.

The methods of the libraries are generated into blocks starting with `lib1_` or `lib2_`. The blocks starting with `lib1_` are generated from the methods of the old implementation, the other blocks are generated from the new implementation. Additionally there are two blocks, `dispatch1` and `dispatch2`. These blocks include references to the call table, return table, and local places table. The call table includes references to the starting blocks of all methods of the corresponding implementation. So the `lib1_calltable` includes references to all methods of the old implementation. The return table includes references to all program points a method called on the context could return to. These are determined by finding method calls inside the body of library methods. The local places table includes all references to local places. These are used to return to local places after checking the local invariant.

Method calls are translated into a series of basic blocks. The blocks `lib<1/2>_<method_sig>_<method_name><i>_boundary` and `lib<1/2>_<method_sig>_<method_name><i>_intern` stand for calling the method is a boundary call or calling the method is an internal call without interaction with the context. `<method_sig>` is the signature of the method the simulation is currently working on, so the caller of the method invocation. `<method_name>` is the name of the method the is invoked. The index `<i>` is a counter, so for each method invocation these blocks have an unambiguous name. The last block of a method invocation is `lib<1/2>_<method_sig>_<method_name><i>` which is added to the return table of the corresponding library and serves as the point the execution continues after returning from the invoked method.


Response from Boogie
--------------------

TODO
