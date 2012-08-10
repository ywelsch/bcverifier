BCVerifier -- Verifying backward compatibility for Java packages
================================================================

A library implementation is **backward compatible** with an older version if the new implementation of the library preserves the behavior of the old implementation for all programs in which the old implementation was used.

**BCVerifier** is a tool that allows to automatically check backward compatibility between two Java library implementations.

Setting
-------

- *Definition-completeness*: The tool assumes that all types used by the library are defined. Furthermore, the tool is unaware of the standard JDK library (except that there is a class Object which is at the root of the type hierarchy). 

- *Sealed packages*: The tool assumes that the packages of the libraries can not be extended by clients. This allows for stronger encapsulation guarantees. In particular, non-public types are not visible to clients, which allows for more interesting changes in new library implementations.

- *Single-threaded setting*: The tool assumes that no threads are created within the library and that the library is only accessed by a single thread.

- *Field protection*: The tool assumes that program contexts do not directly access fields that are defined in the library. This is usually considered bad practice anyhow.

- *Language subset*: Currently, the tool only supports a limited subset of Java. For example, arrays are not supported.

Documentation
-------------

- [[Formal Model]] describes the verification approach
- [[Invariant Specification Language]] describes the ISL specification language which is used to specify coupling invariants
- [[Tool]] describes the architecture of the tool

Tool Developers
---------------

- Mathias Weber <m_weber@cs.uni-kl.de>
- Yannick Welsch <welsch@cs.uni-kl.de>
- Peter Zeller <p_zeller@cs.uni-kl.de>

[Software Technology Group, University of Kaiserslautern](http://softech.cs.uni-kl.de)