BCVerifier -- A Backward Compatibility Verifier for Java Libraries
==================================================================

A library implementation is **backward compatible** with an older implementation if the new implementation of the library preserves the behavior of the old implementation in all programs in which the old implementation was used.
**BCVerifier** is a tool that automatically checks backward compatibility between two Java library implementations.

Verifying backward compatibility consists of ensuring that there is a special simulation relation between programs that use the old library implementation and programs that use the new library implementation.
The verification process relies on a **coupling invariant** that describes this relation.
The coupling invariant has to be provided by the user of the tool.
The BCVerifier tool then checks that the relation induced by the coupling invariant is a proper simulation for the provided library implementations.

Documentation
-------------

Being updated to current tool version (will be made available my Mid-December).

Limitations
-----------

- *Definition-completeness*: The tool assumes that all types referred to by the library are also defined in the library. Furthermore, the tool is unaware of the standard JDK library (except that there is a class Object which is at the root of the type hierarchy).

- *Sealed packages*: The tool assumes that clients cannot define new types in existing packages of the libraries. This allows for stronger encapsulation guarantees. In particular, non-public types are not visible to clients, which allows for more interesting changes in new library implementations.

- *Single-threaded setting*: The tool assumes that no threads are created within the library and that the library is only accessed by a single thread.

- *Field protection*: The tool assumes that code outside the library does not directly access fields that are defined in the library. This is usually considered bad practice anyhow.

- *Language subset*: Currently, the tool only supports a limited subset of Java. In particular, arrays, floats, doubles, static fields and exceptions are not supported.

Developers
----------

- Mathias Weber <m_weber@cs.uni-kl.de>
- [Yannick Welsch](https://softech.informatik.uni-kl.de/Homepage/YannickWelsch) <welsch@cs.uni-kl.de>
- Peter Zeller <p_zeller@cs.uni-kl.de>

[Software Technology Group, University of Kaiserslautern](http://softech.cs.uni-kl.de)