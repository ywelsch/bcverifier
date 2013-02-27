# Issues:

## 1. Static class initializers

Initial values of static variables and static initializer blocks are not supported.

## 2. Tool does not know that types created by the context have the same type

For example, the following example does not verify:

	// old and new implementation contain only this class:
	public class A {
		public int get1() {
			return get2();		
		}
		public int get2() {
			return 1;
		}
	}
	
## 3. nosplit option for local places

...

## 4. Final methods

The tool does not know that final methods cannot be overridden by the context.


# Examples:

Examples without working specification:

- reciter
- ackermann
- listiter2

