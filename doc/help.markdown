BCVerifier -- Verifying backward compatibility for Java packages
================================================================

A library implementation is **backward compatible** with an older version if the new implementation of the library preserves the behavior of the old implementation for all programs in which the old implementation was used.

**BCVerifier** is a tool that allows to automatically check backward compatibility between two Java library implementations.

Setting
-------

- *Source compatibility*: Currently the tool does not check whether the new library implementation is interface-compatible with the old one.

- *Definition-completeness*: The tool assumes that all types used by the library are defined. Furthermore, the tool is unaware of the standard JDK library (except that there is a class Object which is at the root of the type hierarchy). 

- *Sealed packages*: We assume that the packages of the libraries can not be extended by clients. This allows for encapsulation guarantees. In particular, non-public types are not visible to clients, which allows for more interesting changes in new library implementations.

- *Single-threaded setting*: We assume that no threads are created within the library and that the library is only accessed by a single thread.

- *Field protection*: We assume that program contexts do not directly access fields that are defined in the library. This is usually considered bad practice anyhow.

Documentation
-------------

- [[Formal Model]]
- [[Invariant Specification Language]]
- [[Tool]]

Tool Developers
---------------

- Mathias Weber <m_weber@cs.uni-kl.de>
- Yannick Welsch <welsch@cs.uni-kl.de>
- Peter Zeller <p_zeller@cs.uni-kl.de>

[Software Technology Group, University of Kaiserslautern](http://softech.cs.uni-kl.de)