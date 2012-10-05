Tool
====

BCVerifier is a standalone tool for checking backward compatibility of two Java library implementations. As described in the [[Formal Model]] section, the tool takes as input two implementations of a library written in the Java language and a coupling invariant. We provide a command line version and a web front-end to the tool.

<!--
Command Line Tool
-----------------

The implementations that should be checked need to be available as Java source code in two directories. The package structure must be obeyed as usual. These two folders are passed to the verifier using the command line argument `-l <old_implementation> <new_implementation>` where `<old_implementation>` is the path to the source folder of the old library implementation and `<new_implementation>` is the path to the source folder of the new library implementation.

The Java source files are checked for source compatibility first. Source compatibility is a necessary condition for (behavioral) backward compatibility. If two library implementations are source compatible then both implementations can be combined with the same program contexts yielding valid Java programs each. By specifying the command line argument `-c` the library files are compiled using the right compiler flags to support all the functionality of the tool.
-->

Coupling Invariant
------------------

The BCVerifier accepts specifications using one of two possible syntaxes (although the web frontend only supports the first syntax).

  - The [[Invariant Specification Language]] is a high level specification language assisting to specify the coupling invariant. Files in this language are validated against the Java code and transformed into a consistent Boogie specification. Specifications of this type must have the file ending `isl`.

  - The Boogie syntax uses pure Boogie expressions. No effort is made to check that the invariant specified is valid with respect to inconsistencies with the prelude or other essential syntactic and semantic restrictions. The Boogie specifications are directly inserted as is into the generated Boogie program. *Use with care!* Specifications of this type must have the file ending `bsl`.

Both types of specifications are specified using the command line argument `-s <invariant_file>` where `<invariant_file>` is the path to the specification.

<!--
Parameters Needed
-----------------

TODO
-->

Layout of the Boogie Program
----------------------------

Both libraries are transformed into one Boogie procedure by analyzing the byte code of the library implementations. The prelude includes all axioms essential for the verification process. The Boogie procedure `checkLibraries` simulates all possible method calls of both library implementations. The body of this procedure is divided into basic blocks.

###Common Blocks of Both Libraries
The basic blocks starting with `preconditions` include the preconditions of the possible interactions with the library like method calls, constructor calls and method returns into a library method. The coupling invariant is assumed in these blocks. The blocks starting with `check` include the checks that need to be performed when the control switches from the library to the context (boundary call or boundary return) or a local place is reached during simulation. Included in these blocks is the check of the coupling invariant.

###Blocks for the Methods
The methods of the libraries are generated into blocks starting with `lib1_` or `lib2_`. The blocks starting with `lib1_` are generated from the methods of the old implementation, the other blocks are generated from the new implementation. Additionally there are two blocks, `dispatch1` and `dispatch2`. These blocks include references to the call table, return table, and local places table. The call table includes references to the starting blocks of all methods of the corresponding implementation. So the `lib1_calltable` includes references to all methods of the old implementation. The return table includes references to all program points a method called on the context could return to. These are determined by finding method calls inside the body of library methods. The local places table includes all references to local places. These are used to return to local places after checking the local invariant.


###Method Calls
Method calls are translated into a series of basic blocks. The blocks `lib<1/2>_<method_sig>_<method_name><i>_boundary` and `lib<1/2>_<method_sig>_<method_name><i>_intern` stand for calling the method is a boundary call or calling the method is an internal call without interaction with the context. `<method_sig>` is the signature of the method the simulation is currently working on, so the caller of the method invocation. `<method_name>` is the name of the method the is invoked. The index `<i>` is a counter, so for each method invocation these blocks have an unambiguous name. The last block of a method invocation is `lib<1/2>_<method_sig>_<method_name><i>` which is added to the return table of the corresponding library and serves as the point the execution continues after returning from the invoked method.

###Local Places
For each program point there may be multiple local places. For each program point at which a local place is defined a block for each such local place is generated which checks the condition of the local place and goes to the `check_local` block. This block ends with `_check`. Additionally a block is generated with all conditions of all the local places defined in this program point negated, which ends in `_skip`. This path is taken if none of the conditions of the local places is meat. The block which ends with the name of the local place is the block the execution returns to after checking the local invariant. This block is also referenced in the local place table. The simulation branches to two additional blocks when returning to such a local place block. For one the execution uses the block that ends in `_cont`. This block includes the normal execution of the method after the local place. This path is taken if the execution is not stalled. The second block is the block ending with `_stall` which is taken if the execution should be stalled at this local place.


Response from Boogie
--------------------

###Success or Error
The response from the backward compatibility verifier can be of two kind. If the verification was successful the response is `Boogie program verifier finished with 1 verified, 0 errors`. This means that the tool verified that the new library implementation is backward compatible with the old library implementation using the given coupling invariant. If the verification was not successful, the tool will answer with a Boogie trace. The maybe most important lines are lines of the form `<filename>(<line>): Error BP5001: This assertion might not hold.`. The line number tells us in which line to look for the condition that could not be established.

###Execution Trace
The first thing one has to find when confronted with such an error is the execution trace that went wrong. This can be deduced from the error trace. Listed to the right are the labels the simulation passed during this erroneous execution. The list of labels starts with `preconditions` followed by a # and a number. The number is not so important and indicates the unrolled instance of the Boogie program. The label list normally ends with a checking label. Sometimes the last label is a call, return, or local table or a loop label. This indicates a loop unroll parameter that was too low for the check to succeed. Normally also included are the labels `dispatch1`, `dispatch2`, and `check`. The labels between `dispatch1` and `dispatch2` indicate the execution of the first library implementation, so the old version. The labels between `dispatch2` and `check` indicate the execution of the second library implementation, so the new version. As described earlier, the names of the labels give hints to which method is executed, which method is being called, which action should be performed, like boundary return, boundary call and so on, or at which local place the execution stopped.

Not all labels that are passed by the simulation appear in the execution trace. The labels listed are the labels where the simulation had to choose one branch out of multiple possibilities. This means that if the library consisted of only one class and the class has only one constructor, for example the default constructor, then the labels of the constructor implementation itself might not necessarily show up in the execution trace. The best thing one can do if it seems that there are labels missing that could help in tracking down the error is to follow the path from the label `dispatch1` on. Whenever the labels are missing from the execution trace, there is only one possible path to take.
