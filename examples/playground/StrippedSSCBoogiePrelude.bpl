// SSCBoogiePrelude.bpl without ownership/invariants and array/structs and boxing/unboxing and String

// *********************************************
// *                                          *
// *   Boogie 2 prelude for MSIL translator   *
// *                                          *
// ********************************************


//------------ New types

type TName;
type real;
type Elements alpha;

//------------ Encode the heap

type ref;
const null: ref;

type Field alpha;

type HeapType = <beta>[ref, Field beta]beta;
var $Heap : HeapType where IsHeap($Heap);

type ActivityType;
var $ActivityIndicator : ActivityType;


// IsHeap(h) holds if h is a properly formed heap
function IsHeap(h: HeapType) returns (bool);
// IsPreHeap(h) holds if the heap is properly formed, except
// that object-invariant properties (like "o.inv ==> Inv(o)")
// may not hold.
function IsPreHeap(h: HeapType) returns (bool);
axiom (forall h: HeapType :: { IsPreHeap(h) }  IsHeap(h) ==> IsPreHeap(h));


// records whether a pointer refers to allocated data
const unique $allocated : Field bool;

// a map from class names to their representative "references" used to obtain values of static fields
function ClassRepr(class: TName) returns (ref);
// this map is injective
function ClassReprInv(ref) returns (TName);
axiom (forall c: TName :: {ClassRepr(c)} ClassReprInv(ClassRepr(c)) == c);
axiom (forall T: TName :: !($typeof(ClassRepr(T)) <: System.Object));
axiom (forall T: TName :: ClassRepr(T) != null);

// The following function lets the translation utter an expression, with the hope that it
// will cause good things to be triggered.
function $Utter(ref) returns (bool);

//------------ Fields
// fields are classified into whether or not the field is static (i.e., it is a field of a ClassRepr "ref")
// and whether or not it is directly modifiable by the user

// indicates a field is static
function IsStaticField<alpha>(f: Field alpha) returns (bool);
axiom !IsStaticField($allocated);

//------------ Encode type information

function $typeof (ref) returns (TName);

function $BaseClass(sub: TName) returns (base: TName);
axiom (forall T: TName ::  { $BaseClass(T) }  T <: $BaseClass(T) &&  (!$IsValueType(T) && T != System.Object ==> T != $BaseClass(T)));

// Incomparable subtype axiom:
function AsDirectSubClass(sub: TName, base: TName) returns (sub': TName);
function OneClassDown(sub: TName, base: TName) returns (directSub: TName);
axiom (forall A: TName, B: TName, C: TName :: { C <: AsDirectSubClass(B,A) }  C <: AsDirectSubClass(B,A)  ==>  OneClassDown(C,A) == B);

// primitive types are unordered in the type ordering
function $IsValueType(TName) returns (bool);
axiom (forall T: TName :: $IsValueType(T)  ==>  (forall U: TName ::  T <: U  ==>  T == U) && (forall U: TName ::  U <: T  ==>  T == U));

const unique System.Boolean: TName;  // bool
axiom $IsValueType(System.Boolean);

// type constructor T[]
//
const unique System.Object : TName;
axiom (forall T: TName ::  T <: System.Object ==> !$IsValueType(T));

// reflection
//
function TypeObject (TName) returns (ref); // Corresponds with C# typeof(T)
const unique System.Type : TName;
axiom System.Type <: System.Object;
axiom (forall T:TName :: {TypeObject(T)} $IsNotNull(TypeObject(T), System.Type));
function TypeName(ref) returns (TName);  // the inverse of TypeObject, which is injective
axiom (forall T:TName :: {TypeObject(T)}  TypeName(TypeObject(T)) == T);

function $Is (ref, TName) returns (bool);
axiom (forall o:ref, T:TName :: {$Is(o, T)} $Is(o, T)  <==>  o == null || $typeof(o) <: T);

function $IsNotNull(ref, TName) returns (bool);
axiom (forall o:ref, T:TName :: {$IsNotNull(o, T)} $IsNotNull(o, T)  <==>  o != null && $Is(o,T));

// $As(o,T) is to be used only when T denotes a reference type (see also BoxTester).  It returns either o or null.
function $As (ref, TName) returns (ref);
axiom (forall o:ref, T:TName :: $Is(o, T)  ==>  $As(o, T) == o);
axiom (forall o:ref, T:TName :: ! $Is(o, T)  ==>  $As(o, T) == null);

// Arrays are always valid (but may be committed)

//---------- Types and allocation of reachable things

function IsAllocated<alpha>(h: HeapType, o: alpha) returns (bool);

// everything in the range of a proper heap is allocated whenever the domain is
axiom (forall<alpha> h: HeapType, o: ref, f: Field alpha :: {IsAllocated(h, h[o,f])} IsHeap(h) && h[o, $allocated]  ==>  IsAllocated(h, h[o,f]));
axiom (forall h: HeapType, o: ref, f: Field ref :: {h[h[o,f], $allocated]} IsHeap(h) && h[o, $allocated]  ==>  h[h[o,f], $allocated]);

axiom (forall h: HeapType, o: ref :: {h[o, $allocated]}  IsAllocated(h,o)  ==>  h[o, $allocated]);

axiom (forall h: HeapType, c:TName :: {h[ClassRepr(c), $allocated]} IsHeap(h)  ==>  h[ClassRepr(c), $allocated]);

const $BeingConstructed: ref;
const unique $NonNullFieldsAreInitialized: Field bool;
const $PurityAxiomsCanBeAssumed: bool;
axiom DeclType($NonNullFieldsAreInitialized) == System.Object;

// types of fields
function DeclType<alpha>(field: Field alpha) returns (class: TName);  // for "class C { T f; ...", DeclType(f) == C
function AsNonNullRefField(field: Field ref, T: TName) returns (f: Field ref);  // for "class C { T! f; ...", AsNonNullRefField(f,T) == f
function AsRefField(field: Field ref, T: TName) returns (f: Field ref);  // for "class C { T f; ...", AsRefField(f,T) == f
// for integral types T
function AsRangeField(field: Field int, T: TName) returns (f: Field int);  // for "class C { T f; ...", AsRangeField(f,T) == f

axiom (forall f: Field ref, T: TName :: {AsNonNullRefField(f,T)}  AsNonNullRefField(f,T)==f  ==>  AsRefField(f,T)==f);

// fields in the heap are well typed
axiom (forall h: HeapType, o: ref, f: Field ref, T: TName :: {h[o,AsRefField(f,T)]}  IsHeap(h)  ==>  $Is(h[o,AsRefField(f,T)], T));
axiom (forall h: HeapType, o: ref, f: Field ref, T: TName :: {h[o,AsNonNullRefField(f,T)]}  IsHeap(h) && o != null && (o != $BeingConstructed || h[$BeingConstructed, $NonNullFieldsAreInitialized] == true) ==>  h[o,AsNonNullRefField(f,T)] != null);
axiom (forall h: HeapType, o: ref, f: Field int, T: TName :: {h[o,AsRangeField(f,T)]}  IsHeap(h)  ==>  InRange(h[o,AsRangeField(f,T)], T));

// abstract classes, interfaces, ...
function $IsMemberlessType(TName) returns (bool);
axiom (forall o: ref :: { $IsMemberlessType($typeof(o)) }  !$IsMemberlessType($typeof(o)));
function $AsInterface(TName) returns (TName);

axiom (forall J: TName :: { System.Object <: $AsInterface(J) }  $AsInterface(J) == J  ==>  !(System.Object <: J));

function $HeapSucc(oldHeap: HeapType, newHeap: HeapType) returns (bool);

//------------ Immutable types

function $IsImmutable(T:TName) returns (bool);

// We say here that System.Object is mutable, but only using the $IsImmutable predicate.  The functions
// $AsImmutable and $AsMutable below are used to say that all subtypes below fixpoints of these functions
// are also fixpoints.
axiom !$IsImmutable(System.Object);

function $AsImmutable(T:TName) returns (theType: TName);
function $AsMutable(T:TName) returns (theType: TName);

axiom (forall T: TName, U:TName :: {U <: $AsImmutable(T)} U <: $AsImmutable(T) ==>  $IsImmutable(U) && $AsImmutable(U) == U);
axiom (forall T: TName, U:TName :: {U <: $AsMutable(T)} U <: $AsMutable(T) ==>  !$IsImmutable(U) && $AsMutable(U) == U);


//---------- Various sized integers

const unique System.SByte : TName;  // sbyte
axiom $IsValueType(System.SByte);
const unique System.Byte : TName;  // byte
axiom $IsValueType(System.Byte);
const unique System.Int16 : TName;  //short
axiom $IsValueType(System.Int16);
const unique System.UInt16 : TName;  // ushort
axiom $IsValueType(System.UInt16);
const unique System.Int32 : TName;  // int
axiom $IsValueType(System.Int32);
const unique System.UInt32 : TName;  // uint
axiom $IsValueType(System.UInt32);
const unique System.Int64 : TName;  // long
axiom $IsValueType(System.Int64);
const unique System.UInt64 : TName;  // ulong
axiom $IsValueType(System.UInt64);
const unique System.Char : TName;  // char
axiom $IsValueType(System.Char);
const unique System.UIntPtr : TName;
axiom $IsValueType(System.UIntPtr);
const unique System.IntPtr : TName;
axiom $IsValueType(System.IntPtr);

function InRange(i: int, T: TName) returns (bool);
axiom (forall i:int :: InRange(i, System.SByte)  <==>  -128 <= i && i < 128);
axiom (forall i:int :: InRange(i, System.Byte)  <==>  0 <= i && i < 256);
axiom (forall i:int :: InRange(i, System.Int16)  <==>  -32768 <= i && i < 32768);
axiom (forall i:int :: InRange(i, System.UInt16)  <==>  0 <= i && i < 65536);
axiom (forall i:int :: InRange(i, System.Int32)  <==>  -2147483648 <= i && i <= 2147483647);
axiom (forall i:int :: InRange(i, System.UInt32)  <==>  0 <= i && i <= 4294967295);
axiom (forall i:int :: InRange(i, System.Int64)  <==>  -9223372036854775808 <= i && i <= 9223372036854775807);
axiom (forall i:int :: InRange(i, System.UInt64)  <==>  0 <= i && i <= 18446744073709551615);
axiom (forall i:int :: InRange(i, System.Char)  <==>  0 <= i && i < 65536);


//---------- Type conversions and sizes


function $IntToInt(val: int, fromType: TName, toType: TName) returns (int);
function $IntToReal(val: int, fromType: TName, toType: TName) returns (real);
function $RealToInt(val: real, fromType: TName, toType: TName) returns (int);
function $RealToReal(val: real, fromType: TName, toType: TName) returns (real);

axiom (forall z: int, B: TName, C: TName :: InRange(z, C) ==> $IntToInt(z, B, C) == z);

function $SizeIs (TName, int) returns (bool); // SizeIs(T,n) means that n = sizeof(T)



//------------ Formula/term operators

function $IfThenElse<a>(bool, a, a) returns (a);

axiom (forall<a> b:bool, x:a, y:a :: {$IfThenElse(b,x,y)} b ==>  $IfThenElse(b,x,y) == x);
axiom (forall<a> b:bool, x:a, y:a :: {$IfThenElse(b,x,y)} !b ==>  $IfThenElse(b,x,y) == y);

//------------ Bit-level operators

function #neg (int) returns (int);
function #and (int, int) returns (int);
function #or (int, int) returns (int);
function #xor (int, int) returns (int);
function #shl (int, int) returns (int);
function #shr (int, int) returns (int);

function #rneg(real) returns (real);
function #radd(real, real) returns (real);
function #rsub(real, real) returns (real);
function #rmul(real, real) returns (real);
function #rdiv(real, real) returns (real);
function #rmod(real, real) returns (real);
function #rLess(real, real) returns (bool);
function #rAtmost(real, real) returns (bool);
function #rEq(real, real) returns (bool);
function #rNeq(real, real) returns (bool);
function #rAtleast(real, real) returns (bool);
function #rGreater(real, real) returns (bool);


//----------- Properties of operators

// the connection between % and /
axiom (forall x:int, y:int :: {x % y} {x / y}  x % y == x - x / y * y);

// remainder is C# is complicated, because division rounds toward 0
axiom (forall x:int, y:int :: {x % y}  0 <= x && 0 < y  ==>  0 <= x % y  &&  x % y < y);
axiom (forall x:int, y:int :: {x % y}  0 <= x && y < 0  ==>  0 <= x % y  &&  x % y < -y);
axiom (forall x:int, y:int :: {x % y}  x <= 0 && 0 < y  ==>  -y < x % y  &&  x % y <= 0);
axiom (forall x:int, y:int :: {x % y}  x <= 0 && y < 0  ==>  y < x % y  &&  x % y <= 0);

axiom (forall x:int, y:int :: {(x + y) % y}  0 <= x && 0 <= y  ==>  (x + y) % y == x % y);
// do we need this symmetric one, too?
axiom (forall x:int, y:int :: {(y + x) % y}  0 <= x && 0 <= y  ==>  (y + x) % y == x % y);
axiom (forall x:int, y:int :: {(x - y) % y}  0 <= x-y && 0 <= y  ==>  (x - y) % y == x % y);

// the following axiom prevents a matching loop in Simplify
// axiom (forall x:int, y:int :: {x * y / y * y}  x * y / y * y == x * y);

// the following axiom has some unfortunate matching, but it does state a property about % that
// is sometime useful
axiom (forall a: int, b: int, d: int :: { a % d, b % d } 2 <= d && a % d == b % d && a < b  ==>  a + d <= b);

//  These axioms provide good functionality, but in some cases they can be very expensive
// distributions of * and +/-
axiom (forall x: int, y: int, z: int ::  { (x+y)*z }  (x+y)*z == x*z + y*z);
axiom (forall x: int, y: int, z: int ::  { (x-y)*z }  (x-y)*z == x*z - y*z);
axiom (forall x: int, y: int, z: int ::  { z*(x+y) }  z*(x+y) == z*x + z*y);
axiom (forall x: int, y: int, z: int ::  { z*(x-y) }  z*(x-y) == z*x - z*y);

axiom (forall x: int, y: int :: { #and(x,y) }  #and(x,y) == #and(y,x));
axiom (forall x: int, y: int :: { #or(x,y) }  #or(x,y) == #or(y,x));

axiom (forall x: int, y: int :: { #and(x,y) }  0 <= x || 0 <= y  ==>  0 <= #and(x,y));
axiom (forall x: int, y: int :: { #or(x,y) }  0 <= x && 0 <= y  ==>  0 <= #or(x,y) && #or(x,y) <= x + y);

axiom (forall x: int :: { #and(x,-1) }  #and(x,-1) == x);
axiom (forall x: int :: { #and(x,0) }  #and(x,0) == 0);
axiom (forall x: int :: { #or(x,-1) }  #or(x,-1) == -1);
axiom (forall x: int :: { #or(x,0) }  #or(x,0) == x);

axiom (forall i:int :: {#shl(i,0)} #shl(i,0) == i);
axiom (forall i:int, j:int :: {#shl(i,j)}  1 <= j ==> #shl(i,j) == #shl(i,j-1) * 2);
axiom (forall i:int, j:int :: {#shl(i,j)} 0 <= i && i < 32768 && 0 <= j && j <= 16  ==>  0 <= #shl(i, j) && #shl(i, j) <= 2147483647);

axiom (forall i:int :: {#shr(i,0)} #shr(i,0) == i);
axiom (forall i:int, j:int :: {#shr(i,j)} 1 <= j ==> #shr(i,j) == #shr(i,j-1) / 2);


function #min(int, int) returns (int);
function #max(int, int) returns (int);
axiom (forall x: int, y: int :: { #min(x,y) } (#min(x,y) == x || #min(x,y) == y) && #min(x,y) <= x && #min(x,y) <= y);
axiom (forall x: int, y: int :: { #max(x,y) } (#max(x,y) == x || #max(x,y) == y) && x <= #max(x,y) && y <= #max(x,y));


// ************** END PRELUDE **************
