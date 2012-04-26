// Spec# program verifier version 2.0.0.0, Copyright (c) 2003-2010, Microsoft.
// Command Line Options: /noVerify /print:output.bpl Bag0.dll

type TName;

type real;

type Elements _;

type struct;

const $ZeroStruct: struct;

type ref;

const null: ref;

type Field _;

type HeapType = <beta>[ref,Field beta]beta;

var $Heap: HeapType where IsHeap($Heap);

type ActivityType;

var $ActivityIndicator: ActivityType;

function IsHeap(h: HeapType) : bool;

function IsPreHeap(h: HeapType) : bool;

axiom (forall h: HeapType :: { IsPreHeap(h) } IsHeap(h) ==> IsPreHeap(h));

const unique $allocated: Field bool;

const unique $elementsBool: Field (Elements bool);

const unique $elementsInt: Field (Elements int);

const unique $elementsRef: Field (Elements ref);

const unique $elementsReal: Field (Elements real);

const unique $elementsStruct: Field (Elements struct);

axiom DeclType($elementsBool) == System.Array;

axiom DeclType($elementsInt) == System.Array;

axiom DeclType($elementsRef) == System.Array;

axiom DeclType($elementsReal) == System.Array;

axiom DeclType($elementsStruct) == System.Array;

const unique $inv: Field TName;

const unique $localinv: Field TName;

type exposeVersionType;

const unique $exposeVersion: Field exposeVersionType;

axiom DeclType($exposeVersion) == System.Object;

const unique $ownerRef: Field ref;

const unique $ownerFrame: Field TName;

const unique $PeerGroupPlaceholder: TName;

function ClassRepr(class: TName) : ref;

function ClassReprInv(ref) : TName;

axiom (forall c: TName :: { ClassRepr(c) } ClassReprInv(ClassRepr(c)) == c);

axiom (forall T: TName :: !($typeof(ClassRepr(T)) <: System.Object));

axiom (forall T: TName :: ClassRepr(T) != null);

axiom (forall T: TName, h: HeapType :: { h[ClassRepr(T), $ownerFrame] } IsHeap(h) ==> h[ClassRepr(T), $ownerFrame] == $PeerGroupPlaceholder);

function $Utter(ref) : bool;

function IncludeInMainFrameCondition<alpha>(f: Field alpha) : bool;

axiom IncludeInMainFrameCondition($allocated);

axiom IncludeInMainFrameCondition($elementsBool) && IncludeInMainFrameCondition($elementsInt) && IncludeInMainFrameCondition($elementsRef) && IncludeInMainFrameCondition($elementsReal) && IncludeInMainFrameCondition($elementsStruct);

axiom !IncludeInMainFrameCondition($inv);

axiom !IncludeInMainFrameCondition($localinv);

axiom IncludeInMainFrameCondition($ownerRef);

axiom IncludeInMainFrameCondition($ownerFrame);

axiom IncludeInMainFrameCondition($exposeVersion);

axiom !IncludeInMainFrameCondition($FirstConsistentOwner);

function IsStaticField<alpha>(f: Field alpha) : bool;

axiom !IsStaticField($allocated);

axiom !IsStaticField($elementsBool) && !IsStaticField($elementsInt) && !IsStaticField($elementsRef) && !IsStaticField($elementsReal) && !IsStaticField($elementsStruct);

axiom !IsStaticField($inv);

axiom !IsStaticField($localinv);

axiom !IsStaticField($exposeVersion);

function $IncludedInModifiesStar<alpha>(f: Field alpha) : bool;

axiom !$IncludedInModifiesStar($ownerRef);

axiom !$IncludedInModifiesStar($ownerFrame);

axiom $IncludedInModifiesStar($exposeVersion);

axiom $IncludedInModifiesStar($elementsBool) && $IncludedInModifiesStar($elementsInt) && $IncludedInModifiesStar($elementsRef) && $IncludedInModifiesStar($elementsReal) && $IncludedInModifiesStar($elementsStruct);

function ArrayGet<alpha>(Elements alpha, int) : alpha;

function ArraySet<alpha>(Elements alpha, int, alpha) : Elements alpha;

axiom (forall<alpha> A: Elements alpha, i: int, x: alpha :: ArrayGet(ArraySet(A, i, x), i) == x);

axiom (forall<alpha> A: Elements alpha, i: int, j: int, x: alpha :: i != j ==> ArrayGet(ArraySet(A, i, x), j) == ArrayGet(A, j));

function ArrayIndex(arr: ref, dim: int, indexAtDim: int, remainingIndexContribution: int) : int;

function ArrayIndexInvX(arrayIndex: int) : int;

function ArrayIndexInvY(arrayIndex: int) : int;

axiom (forall a: ref, d: int, x: int, y: int :: { ArrayIndex(a, d, x, y) } ArrayIndexInvX(ArrayIndex(a, d, x, y)) == x);

axiom (forall a: ref, d: int, x: int, y: int :: { ArrayIndex(a, d, x, y) } ArrayIndexInvY(ArrayIndex(a, d, x, y)) == y);

axiom (forall a: ref, i: int, heap: HeapType :: { ArrayGet(heap[a, $elementsInt], i) } IsHeap(heap) ==> InRange(ArrayGet(heap[a, $elementsInt], i), $ElementType($typeof(a))));

axiom (forall a: ref, i: int, heap: HeapType :: { $typeof(ArrayGet(heap[a, $elementsRef], i)) } IsHeap(heap) && ArrayGet(heap[a, $elementsRef], i) != null ==> $typeof(ArrayGet(heap[a, $elementsRef], i)) <: $ElementType($typeof(a)));

axiom (forall a: ref, T: TName, i: int, r: int, heap: HeapType :: { $typeof(a) <: NonNullRefArray(T, r), ArrayGet(heap[a, $elementsRef], i) } IsHeap(heap) && $typeof(a) <: NonNullRefArray(T, r) ==> ArrayGet(heap[a, $elementsRef], i) != null);

function $Rank(ref) : int;

axiom (forall a: ref :: 1 <= $Rank(a));

axiom (forall a: ref, T: TName, r: int :: { $typeof(a) <: RefArray(T, r) } a != null && $typeof(a) <: RefArray(T, r) ==> $Rank(a) == r);

axiom (forall a: ref, T: TName, r: int :: { $typeof(a) <: NonNullRefArray(T, r) } a != null && $typeof(a) <: NonNullRefArray(T, r) ==> $Rank(a) == r);

axiom (forall a: ref, T: TName, r: int :: { $typeof(a) <: ValueArray(T, r) } a != null && $typeof(a) <: ValueArray(T, r) ==> $Rank(a) == r);

axiom (forall a: ref, T: TName, r: int :: { $typeof(a) <: IntArray(T, r) } a != null && $typeof(a) <: IntArray(T, r) ==> $Rank(a) == r);

function $Length(ref) : int;

axiom (forall a: ref :: { $Length(a) } 0 <= $Length(a) && $Length(a) <= 2147483647);

function $DimLength(ref, int) : int;

axiom (forall a: ref, i: int :: 0 <= $DimLength(a, i));

axiom (forall a: ref :: { $DimLength(a, 0) } $Rank(a) == 1 ==> $DimLength(a, 0) == $Length(a));

function $LBound(ref, int) : int;

function $UBound(ref, int) : int;

axiom (forall a: ref, i: int :: { $LBound(a, i) } $LBound(a, i) == 0);

axiom (forall a: ref, i: int :: { $UBound(a, i) } $UBound(a, i) == $DimLength(a, i) - 1);

type ArrayCategory;

const unique $ArrayCategoryValue: ArrayCategory;

const unique $ArrayCategoryInt: ArrayCategory;

const unique $ArrayCategoryRef: ArrayCategory;

const unique $ArrayCategoryNonNullRef: ArrayCategory;

function $ArrayCategory(arrayType: TName) : ArrayCategory;

axiom (forall T: TName, ET: TName, r: int :: { T <: ValueArray(ET, r) } T <: ValueArray(ET, r) ==> $ArrayCategory(T) == $ArrayCategoryValue);

axiom (forall T: TName, ET: TName, r: int :: { T <: IntArray(ET, r) } T <: IntArray(ET, r) ==> $ArrayCategory(T) == $ArrayCategoryInt);

axiom (forall T: TName, ET: TName, r: int :: { T <: RefArray(ET, r) } T <: RefArray(ET, r) ==> $ArrayCategory(T) == $ArrayCategoryRef);

axiom (forall T: TName, ET: TName, r: int :: { T <: NonNullRefArray(ET, r) } T <: NonNullRefArray(ET, r) ==> $ArrayCategory(T) == $ArrayCategoryNonNullRef);

const unique System.Array: TName;

axiom System.Array <: System.Object;

function $ElementType(TName) : TName;

function ValueArray(elementType: TName, rank: int) : TName;

axiom (forall T: TName, r: int :: { ValueArray(T, r) } ValueArray(T, r) <: ValueArray(T, r) && ValueArray(T, r) <: System.Array);

function IntArray(elementType: TName, rank: int) : TName;

axiom (forall T: TName, r: int :: { IntArray(T, r) } IntArray(T, r) <: IntArray(T, r) && IntArray(T, r) <: System.Array);

function RefArray(elementType: TName, rank: int) : TName;

axiom (forall T: TName, r: int :: { RefArray(T, r) } RefArray(T, r) <: RefArray(T, r) && RefArray(T, r) <: System.Array);

function NonNullRefArray(elementType: TName, rank: int) : TName;

axiom (forall T: TName, r: int :: { NonNullRefArray(T, r) } NonNullRefArray(T, r) <: NonNullRefArray(T, r) && NonNullRefArray(T, r) <: System.Array);

function NonNullRefArrayRaw(array: ref, elementType: TName, rank: int) : bool;

axiom (forall array: ref, elementType: TName, rank: int :: { NonNullRefArrayRaw(array, elementType, rank) } NonNullRefArrayRaw(array, elementType, rank) ==> $typeof(array) <: System.Array && $Rank(array) == rank && elementType <: $ElementType($typeof(array)));

axiom (forall T: TName, U: TName, r: int :: U <: T ==> RefArray(U, r) <: RefArray(T, r));

axiom (forall T: TName, U: TName, r: int :: U <: T ==> NonNullRefArray(U, r) <: NonNullRefArray(T, r));

axiom (forall A: TName, r: int :: $ElementType(ValueArray(A, r)) == A);

axiom (forall A: TName, r: int :: $ElementType(IntArray(A, r)) == A);

axiom (forall A: TName, r: int :: $ElementType(RefArray(A, r)) == A);

axiom (forall A: TName, r: int :: $ElementType(NonNullRefArray(A, r)) == A);

axiom (forall A: TName, r: int, T: TName :: { T <: RefArray(A, r) } T <: RefArray(A, r) ==> T != A && T == RefArray($ElementType(T), r) && $ElementType(T) <: A);

axiom (forall A: TName, r: int, T: TName :: { T <: NonNullRefArray(A, r) } T <: NonNullRefArray(A, r) ==> T != A && T == NonNullRefArray($ElementType(T), r) && $ElementType(T) <: A);

axiom (forall A: TName, r: int, T: TName :: { T <: ValueArray(A, r) } T <: ValueArray(A, r) ==> T == ValueArray(A, r));

axiom (forall A: TName, r: int, T: TName :: { T <: IntArray(A, r) } T <: IntArray(A, r) ==> T == IntArray(A, r));

axiom (forall A: TName, r: int, T: TName :: { RefArray(A, r) <: T } RefArray(A, r) <: T ==> System.Array <: T || (T == RefArray($ElementType(T), r) && A <: $ElementType(T)));

axiom (forall A: TName, r: int, T: TName :: { NonNullRefArray(A, r) <: T } NonNullRefArray(A, r) <: T ==> System.Array <: T || (T == NonNullRefArray($ElementType(T), r) && A <: $ElementType(T)));

axiom (forall A: TName, r: int, T: TName :: { ValueArray(A, r) <: T } ValueArray(A, r) <: T ==> System.Array <: T || T == ValueArray(A, r));

axiom (forall A: TName, r: int, T: TName :: { IntArray(A, r) <: T } IntArray(A, r) <: T ==> System.Array <: T || T == IntArray(A, r));

function $ArrayPtr(elementType: TName) : TName;

function $ElementProxy(ref, int) : ref;

function $ElementProxyStruct(struct, int) : ref;

axiom (forall a: ref, i: int, heap: HeapType :: { heap[ArrayGet(heap[a, $elementsRef], i), $ownerRef] } { heap[ArrayGet(heap[a, $elementsRef], i), $ownerFrame] } IsHeap(heap) && $typeof(a) <: System.Array ==> ArrayGet(heap[a, $elementsRef], i) == null || $IsImmutable($typeof(ArrayGet(heap[a, $elementsRef], i))) || (heap[ArrayGet(heap[a, $elementsRef], i), $ownerRef] == heap[$ElementProxy(a, 0 - 1), $ownerRef] && heap[ArrayGet(heap[a, $elementsRef], i), $ownerFrame] == heap[$ElementProxy(a, 0 - 1), $ownerFrame]));

axiom (forall a: ref, heap: HeapType :: { IsAllocated(heap, a) } IsHeap(heap) && IsAllocated(heap, a) && $typeof(a) <: System.Array ==> IsAllocated(heap, $ElementProxy(a, 0 - 1)));

axiom (forall o: ref, pos: int :: { $typeof($ElementProxy(o, pos)) } $typeof($ElementProxy(o, pos)) == System.Object);

axiom (forall o: struct, pos: int :: { $typeof($ElementProxyStruct(o, pos)) } $typeof($ElementProxyStruct(o, pos)) == System.Object);

function $StructGet<alpha>(struct, Field alpha) : alpha;

function $StructSet<alpha>(struct, Field alpha, alpha) : struct;

axiom (forall<alpha> s: struct, f: Field alpha, x: alpha :: $StructGet($StructSet(s, f, x), f) == x);

axiom (forall<alpha,beta> s: struct, f: Field alpha, f': Field beta, x: alpha :: f != f' ==> $StructGet($StructSet(s, f, x), f') == $StructGet(s, f'));

function ZeroInit(s: struct, typ: TName) : bool;

function ZeroInitStruct(TName) : struct;

axiom (forall t: TName :: { ZeroInitStruct(t) } ZeroInit(ZeroInitStruct(t), t));

function $typeof(ref) : TName;

function $BaseClass(sub: TName) : TName;

axiom (forall T: TName :: { $BaseClass(T) } T <: $BaseClass(T) && (!$IsValueType(T) && T != System.Object ==> T != $BaseClass(T)));

function AsDirectSubClass(sub: TName, base: TName) : TName;

function OneClassDown(sub: TName, base: TName) : TName;

axiom (forall A: TName, B: TName, C: TName :: { C <: AsDirectSubClass(B, A) } C <: AsDirectSubClass(B, A) ==> OneClassDown(C, A) == B);

function $IsValueType(TName) : bool;

axiom (forall T: TName :: $IsValueType(T) ==> (forall U: TName :: T <: U ==> T == U) && (forall U: TName :: U <: T ==> T == U));

const unique System.Boolean: TName;

axiom $IsValueType(System.Boolean);

const unique System.Object: TName;

axiom (forall T: TName :: T <: System.Object ==> !$IsValueType(T));

function $IsTokenForType(struct, TName) : bool;

function TypeObject(TName) : ref;

const unique System.Type: TName;

axiom System.Type <: System.Object;

axiom (forall T: TName :: { TypeObject(T) } $IsNotNull(TypeObject(T), System.Type));

function TypeName(ref) : TName;

axiom (forall T: TName :: { TypeObject(T) } TypeName(TypeObject(T)) == T);

function $Is(ref, TName) : bool;

axiom (forall o: ref, T: TName :: { $Is(o, T) } $Is(o, T) <==> o == null || $typeof(o) <: T);

function $IsNotNull(ref, TName) : bool;

axiom (forall o: ref, T: TName :: { $IsNotNull(o, T) } $IsNotNull(o, T) <==> o != null && $Is(o, T));

function $As(ref, TName) : ref;

axiom (forall o: ref, T: TName :: $Is(o, T) ==> $As(o, T) == o);

axiom (forall o: ref, T: TName :: !$Is(o, T) ==> $As(o, T) == null);

axiom (forall h: HeapType, o: ref :: { $typeof(o) <: System.Array, h[o, $inv] } IsHeap(h) && o != null && $typeof(o) <: System.Array ==> h[o, $inv] == $typeof(o) && h[o, $localinv] == $typeof(o));

function IsAllocated<alpha>(h: HeapType, o: alpha) : bool;

axiom (forall<alpha> h: HeapType, o: ref, f: Field alpha :: { IsAllocated(h, h[o, f]) } IsHeap(h) && h[o, $allocated] ==> IsAllocated(h, h[o, f]));

axiom (forall h: HeapType, o: ref, f: Field ref :: { h[h[o, f], $allocated] } IsHeap(h) && h[o, $allocated] ==> h[h[o, f], $allocated]);

axiom (forall<alpha> h: HeapType, s: struct, f: Field alpha :: { IsAllocated(h, $StructGet(s, f)) } IsAllocated(h, s) ==> IsAllocated(h, $StructGet(s, f)));

axiom (forall<alpha> h: HeapType, e: Elements alpha, i: int :: { IsAllocated(h, ArrayGet(e, i)) } IsAllocated(h, e) ==> IsAllocated(h, ArrayGet(e, i)));

axiom (forall h: HeapType, o: ref :: { h[o, $allocated] } IsAllocated(h, o) ==> h[o, $allocated]);

axiom (forall h: HeapType, c: TName :: { h[ClassRepr(c), $allocated] } IsHeap(h) ==> h[ClassRepr(c), $allocated]);

const $BeingConstructed: ref;

const unique $NonNullFieldsAreInitialized: Field bool;

const $PurityAxiomsCanBeAssumed: bool;

axiom DeclType($NonNullFieldsAreInitialized) == System.Object;

function DeclType<alpha>(field: Field alpha) : TName;

function AsNonNullRefField(field: Field ref, T: TName) : Field ref;

function AsRefField(field: Field ref, T: TName) : Field ref;

function AsRangeField(field: Field int, T: TName) : Field int;

axiom (forall f: Field ref, T: TName :: { AsNonNullRefField(f, T) } AsNonNullRefField(f, T) == f ==> AsRefField(f, T) == f);

axiom (forall h: HeapType, o: ref, f: Field ref, T: TName :: { h[o, AsRefField(f, T)] } IsHeap(h) ==> $Is(h[o, AsRefField(f, T)], T));

axiom (forall h: HeapType, o: ref, f: Field ref, T: TName :: { h[o, AsNonNullRefField(f, T)] } IsHeap(h) && o != null && (o != $BeingConstructed || h[$BeingConstructed, $NonNullFieldsAreInitialized] == true) ==> h[o, AsNonNullRefField(f, T)] != null);

axiom (forall h: HeapType, o: ref, f: Field int, T: TName :: { h[o, AsRangeField(f, T)] } IsHeap(h) ==> InRange(h[o, AsRangeField(f, T)], T));

function $IsMemberlessType(TName) : bool;

axiom (forall o: ref :: { $IsMemberlessType($typeof(o)) } !$IsMemberlessType($typeof(o)));

function $AsInterface(TName) : TName;

axiom (forall J: TName :: { System.Object <: $AsInterface(J) } $AsInterface(J) == J ==> !(System.Object <: J));

axiom (forall<T> $J: TName, s: T, b: ref :: { UnboxedType(Box(s, b)) <: $AsInterface($J) } $AsInterface($J) == $J && Box(s, b) == b && UnboxedType(Box(s, b)) <: $AsInterface($J) ==> $typeof(b) <: $J);

function $HeapSucc(oldHeap: HeapType, newHeap: HeapType) : bool;

function $IsImmutable(T: TName) : bool;

axiom !$IsImmutable(System.Object);

function $AsImmutable(T: TName) : TName;

function $AsMutable(T: TName) : TName;

axiom (forall T: TName, U: TName :: { U <: $AsImmutable(T) } U <: $AsImmutable(T) ==> $IsImmutable(U) && $AsImmutable(U) == U);

axiom (forall T: TName, U: TName :: { U <: $AsMutable(T) } U <: $AsMutable(T) ==> !$IsImmutable(U) && $AsMutable(U) == U);

function AsOwner(string: ref, owner: ref) : ref;

axiom (forall o: ref, T: TName :: { $typeof(o) <: $AsImmutable(T) } o != null && o != $BeingConstructed && $typeof(o) <: $AsImmutable(T) ==> (forall h: HeapType :: { IsHeap(h) } IsHeap(h) ==> h[o, $inv] == $typeof(o) && h[o, $localinv] == $typeof(o) && h[o, $ownerFrame] == $PeerGroupPlaceholder && AsOwner(o, h[o, $ownerRef]) == o && (forall t: ref :: { AsOwner(o, h[t, $ownerRef]) } AsOwner(o, h[t, $ownerRef]) == o ==> t == o || h[t, $ownerFrame] != $PeerGroupPlaceholder)));

const unique System.String: TName;

function $StringLength(ref) : int;

axiom (forall s: ref :: { $StringLength(s) } 0 <= $StringLength(s));

function AsRepField(f: Field ref, declaringType: TName) : Field ref;

axiom (forall h: HeapType, o: ref, f: Field ref, T: TName :: { h[o, AsRepField(f, T)] } IsHeap(h) && h[o, AsRepField(f, T)] != null ==> h[h[o, AsRepField(f, T)], $ownerRef] == o && h[h[o, AsRepField(f, T)], $ownerFrame] == T);

function AsPeerField(f: Field ref) : Field ref;

axiom (forall h: HeapType, o: ref, f: Field ref :: { h[o, AsPeerField(f)] } IsHeap(h) && h[o, AsPeerField(f)] != null ==> h[h[o, AsPeerField(f)], $ownerRef] == h[o, $ownerRef] && h[h[o, AsPeerField(f)], $ownerFrame] == h[o, $ownerFrame]);

function AsElementsRepField(f: Field ref, declaringType: TName, position: int) : Field ref;

axiom (forall h: HeapType, o: ref, f: Field ref, T: TName, i: int :: { h[o, AsElementsRepField(f, T, i)] } IsHeap(h) && h[o, AsElementsRepField(f, T, i)] != null ==> h[$ElementProxy(h[o, AsElementsRepField(f, T, i)], i), $ownerRef] == o && h[$ElementProxy(h[o, AsElementsRepField(f, T, i)], i), $ownerFrame] == T);

function AsElementsPeerField(f: Field ref, position: int) : Field ref;

axiom (forall h: HeapType, o: ref, f: Field ref, i: int :: { h[o, AsElementsPeerField(f, i)] } IsHeap(h) && h[o, AsElementsPeerField(f, i)] != null ==> h[$ElementProxy(h[o, AsElementsPeerField(f, i)], i), $ownerRef] == h[o, $ownerRef] && h[$ElementProxy(h[o, AsElementsPeerField(f, i)], i), $ownerFrame] == h[o, $ownerFrame]);

axiom (forall h: HeapType, o: ref :: { h[h[o, $ownerRef], $inv] <: h[o, $ownerFrame] } IsHeap(h) && h[o, $ownerFrame] != $PeerGroupPlaceholder && h[h[o, $ownerRef], $inv] <: h[o, $ownerFrame] && h[h[o, $ownerRef], $localinv] != $BaseClass(h[o, $ownerFrame]) ==> h[o, $inv] == $typeof(o) && h[o, $localinv] == $typeof(o));

procedure $SetOwner(o: ref, ow: ref, fr: TName);
  modifies $Heap;
  ensures (forall<alpha> p: ref, F: Field alpha :: { $Heap[p, F] } (F != $ownerRef && F != $ownerFrame) || old($Heap[p, $ownerRef] != $Heap[o, $ownerRef]) || old($Heap[p, $ownerFrame] != $Heap[o, $ownerFrame]) ==> old($Heap[p, F]) == $Heap[p, F]);
  ensures (forall p: ref :: { $Heap[p, $ownerRef] } { $Heap[p, $ownerFrame] } old($Heap[p, $ownerRef] == $Heap[o, $ownerRef]) && old($Heap[p, $ownerFrame] == $Heap[o, $ownerFrame]) ==> $Heap[p, $ownerRef] == ow && $Heap[p, $ownerFrame] == fr);
  free ensures $HeapSucc(old($Heap), $Heap);



procedure $UpdateOwnersForRep(o: ref, T: TName, e: ref);
  modifies $Heap;
  ensures (forall<alpha> p: ref, F: Field alpha :: { $Heap[p, F] } (F != $ownerRef && F != $ownerFrame) || old($Heap[p, $ownerRef] != $Heap[e, $ownerRef]) || old($Heap[p, $ownerFrame] != $Heap[e, $ownerFrame]) ==> old($Heap[p, F]) == $Heap[p, F]);
  ensures e == null ==> $Heap == old($Heap);
  ensures e != null ==> (forall p: ref :: { $Heap[p, $ownerRef] } { $Heap[p, $ownerFrame] } old($Heap[p, $ownerRef] == $Heap[e, $ownerRef]) && old($Heap[p, $ownerFrame] == $Heap[e, $ownerFrame]) ==> $Heap[p, $ownerRef] == o && $Heap[p, $ownerFrame] == T);
  free ensures $HeapSucc(old($Heap), $Heap);



procedure $UpdateOwnersForPeer(c: ref, d: ref);
  modifies $Heap;
  ensures (forall<alpha> p: ref, F: Field alpha :: { $Heap[p, F] } (F != $ownerRef && F != $ownerFrame) || old($Heap[p, $ownerRef] != $Heap[d, $ownerRef] || $Heap[p, $ownerFrame] != $Heap[d, $ownerFrame]) ==> old($Heap[p, F]) == $Heap[p, F]);
  ensures d == null ==> $Heap == old($Heap);
  ensures d != null ==> (forall p: ref :: { $Heap[p, $ownerRef] } { $Heap[p, $ownerFrame] } old($Heap[p, $ownerRef] == $Heap[d, $ownerRef] && $Heap[p, $ownerFrame] == $Heap[d, $ownerFrame]) ==> $Heap[p, $ownerRef] == old($Heap)[c, $ownerRef] && $Heap[p, $ownerFrame] == old($Heap)[c, $ownerFrame]);
  free ensures $HeapSucc(old($Heap), $Heap);



const unique $FirstConsistentOwner: Field ref;

function $AsPureObject(ref) : ref;

function ##FieldDependsOnFCO<alpha>(o: ref, f: Field alpha, ev: exposeVersionType) : alpha;

axiom (forall<alpha> o: ref, f: Field alpha, h: HeapType :: { h[$AsPureObject(o), f] } IsHeap(h) && o != null && h[o, $allocated] == true && $AsPureObject(o) == o && h[o, $ownerFrame] != $PeerGroupPlaceholder && h[h[o, $ownerRef], $inv] <: h[o, $ownerFrame] && h[h[o, $ownerRef], $localinv] != $BaseClass(h[o, $ownerFrame]) ==> h[o, f] == ##FieldDependsOnFCO(o, f, h[h[o, $FirstConsistentOwner], $exposeVersion]));

axiom (forall o: ref, h: HeapType :: { h[o, $FirstConsistentOwner] } IsHeap(h) && o != null && h[o, $allocated] == true && h[o, $ownerFrame] != $PeerGroupPlaceholder && h[h[o, $ownerRef], $inv] <: h[o, $ownerFrame] && h[h[o, $ownerRef], $localinv] != $BaseClass(h[o, $ownerFrame]) ==> h[o, $FirstConsistentOwner] != null && h[h[o, $FirstConsistentOwner], $allocated] == true && (h[h[o, $FirstConsistentOwner], $ownerFrame] == $PeerGroupPlaceholder || !(h[h[h[o, $FirstConsistentOwner], $ownerRef], $inv] <: h[h[o, $FirstConsistentOwner], $ownerFrame]) || h[h[h[o, $FirstConsistentOwner], $ownerRef], $localinv] == $BaseClass(h[h[o, $FirstConsistentOwner], $ownerFrame])));

function Box<T>(T, ref) : ref;

function Unbox<T>(ref) : T;

type NondetType;

function MeldNondets<a>(NondetType, a) : NondetType;

function BoxFunc<T>(value: T, typ: TName) : ref;

function AllocFunc(typ: TName) : ref;

function NewInstance(object: ref, occurrence: NondetType, activity: ActivityType) : ref;

axiom (forall<T> value: T, typ: TName, occurrence: NondetType, activity: ActivityType :: { NewInstance(BoxFunc(value, typ), occurrence, activity) } Box(value, NewInstance(BoxFunc(value, typ), occurrence, activity)) == NewInstance(BoxFunc(value, typ), occurrence, activity) && UnboxedType(NewInstance(BoxFunc(value, typ), occurrence, activity)) == typ);

axiom (forall x: ref, typ: TName, occurrence: NondetType, activity: ActivityType :: !$IsValueType(UnboxedType(x)) ==> NewInstance(BoxFunc(x, typ), occurrence, activity) == x);

axiom (forall<T> x: T, p: ref :: { Unbox(Box(x, p)): T } Unbox(Box(x, p)) == x);

function UnboxedType(ref) : TName;

axiom (forall p: ref :: { $IsValueType(UnboxedType(p)) } $IsValueType(UnboxedType(p)) ==> (forall<T> heap: HeapType, x: T :: { heap[Box(x, p), $inv] } IsHeap(heap) ==> heap[Box(x, p), $inv] == $typeof(Box(x, p)) && heap[Box(x, p), $localinv] == $typeof(Box(x, p))));

axiom (forall<T> x: T, p: ref :: { UnboxedType(Box(x, p)) <: System.Object } UnboxedType(Box(x, p)) <: System.Object && Box(x, p) == p ==> x == p);

function BoxTester(p: ref, typ: TName) : ref;

axiom (forall p: ref, typ: TName :: { BoxTester(p, typ) } UnboxedType(p) == typ <==> BoxTester(p, typ) != null);

axiom (forall p: ref, typ: TName :: { BoxTester(p, typ) } BoxTester(p, typ) != null ==> (forall<T>  :: Box(Unbox(p): T, p) == p));

function BoxDisguise<U>(U) : ref;

function UnBoxDisguise<U>(ref) : U;

axiom (forall<U> x: ref, p: ref :: { Unbox(Box(x, p)): U } Box(x, p) == p ==> Unbox(Box(x, p)): U == UnBoxDisguise(x) && BoxDisguise(Unbox(Box(x, p)): U) == x);

axiom (forall typ: TName, occurrence: NondetType, activity: ActivityType :: { NewInstance(AllocFunc(typ), occurrence, activity) } $typeof(NewInstance(AllocFunc(typ), occurrence, activity)) == typ && NewInstance(AllocFunc(typ), occurrence, activity) != null);

axiom (forall typ: TName, occurrence: NondetType, activity: ActivityType, heap: HeapType :: { heap[NewInstance(AllocFunc(typ), occurrence, activity), $allocated] } IsHeap(heap) ==> heap[NewInstance(AllocFunc(typ), occurrence, activity), $allocated]);

const unique System.SByte: TName;

axiom $IsValueType(System.SByte);

const unique System.Byte: TName;

axiom $IsValueType(System.Byte);

const unique System.Int16: TName;

axiom $IsValueType(System.Int16);

const unique System.UInt16: TName;

axiom $IsValueType(System.UInt16);

const unique System.Int32: TName;

axiom $IsValueType(System.Int32);

const unique System.UInt32: TName;

axiom $IsValueType(System.UInt32);

const unique System.Int64: TName;

axiom $IsValueType(System.Int64);

const unique System.UInt64: TName;

axiom $IsValueType(System.UInt64);

const unique System.Char: TName;

axiom $IsValueType(System.Char);

const unique System.UIntPtr: TName;

axiom $IsValueType(System.UIntPtr);

const unique System.IntPtr: TName;

axiom $IsValueType(System.IntPtr);

function InRange(i: int, T: TName) : bool;

axiom (forall i: int :: InRange(i, System.SByte) <==> 0 - 128 <= i && i < 128);

axiom (forall i: int :: InRange(i, System.Byte) <==> 0 <= i && i < 256);

axiom (forall i: int :: InRange(i, System.Int16) <==> 0 - 32768 <= i && i < 32768);

axiom (forall i: int :: InRange(i, System.UInt16) <==> 0 <= i && i < 65536);

axiom (forall i: int :: InRange(i, System.Int32) <==> 0 - 2147483648 <= i && i <= 2147483647);

axiom (forall i: int :: InRange(i, System.UInt32) <==> 0 <= i && i <= 4294967295);

axiom (forall i: int :: InRange(i, System.Int64) <==> 0 - 9223372036854775808 <= i && i <= 9223372036854775807);

axiom (forall i: int :: InRange(i, System.UInt64) <==> 0 <= i && i <= 18446744073709551615);

axiom (forall i: int :: InRange(i, System.Char) <==> 0 <= i && i < 65536);

function $IntToInt(val: int, fromType: TName, toType: TName) : int;

function $IntToReal(val: int, fromType: TName, toType: TName) : real;

function $RealToInt(val: real, fromType: TName, toType: TName) : int;

function $RealToReal(val: real, fromType: TName, toType: TName) : real;

axiom (forall z: int, B: TName, C: TName :: InRange(z, C) ==> $IntToInt(z, B, C) == z);

function $SizeIs(TName, int) : bool;

function $IfThenElse<a>(bool, a, a) : a;

axiom (forall<a> b: bool, x: a, y: a :: { $IfThenElse(b, x, y) } b ==> $IfThenElse(b, x, y) == x);

axiom (forall<a> b: bool, x: a, y: a :: { $IfThenElse(b, x, y) } !b ==> $IfThenElse(b, x, y) == y);

function #neg(int) : int;

function #and(int, int) : int;

function #or(int, int) : int;

function #xor(int, int) : int;

function #shl(int, int) : int;

function #shr(int, int) : int;

function #rneg(real) : real;

function #radd(real, real) : real;

function #rsub(real, real) : real;

function #rmul(real, real) : real;

function #rdiv(real, real) : real;

function #rmod(real, real) : real;

function #rLess(real, real) : bool;

function #rAtmost(real, real) : bool;

function #rEq(real, real) : bool;

function #rNeq(real, real) : bool;

function #rAtleast(real, real) : bool;

function #rGreater(real, real) : bool;

axiom (forall x: int, y: int :: { x % y } { x / y } x % y == x - x / y * y);

axiom (forall x: int, y: int :: { x % y } 0 <= x && 0 < y ==> 0 <= x % y && x % y < y);

axiom (forall x: int, y: int :: { x % y } 0 <= x && y < 0 ==> 0 <= x % y && x % y < 0 - y);

axiom (forall x: int, y: int :: { x % y } x <= 0 && 0 < y ==> 0 - y < x % y && x % y <= 0);

axiom (forall x: int, y: int :: { x % y } x <= 0 && y < 0 ==> y < x % y && x % y <= 0);

axiom (forall x: int, y: int :: { (x + y) % y } 0 <= x && 0 <= y ==> (x + y) % y == x % y);

axiom (forall x: int, y: int :: { (y + x) % y } 0 <= x && 0 <= y ==> (y + x) % y == x % y);

axiom (forall x: int, y: int :: { (x - y) % y } 0 <= x - y && 0 <= y ==> (x - y) % y == x % y);

axiom (forall a: int, b: int, d: int :: { a % d, b % d } 2 <= d && a % d == b % d && a < b ==> a + d <= b);

axiom (forall x: int, y: int :: { #and(x, y) } #and(x, y) == #and(y, x));

axiom (forall x: int, y: int :: { #or(x, y) } #or(x, y) == #or(y, x));

axiom (forall x: int, y: int :: { #and(x, y) } 0 <= x || 0 <= y ==> 0 <= #and(x, y));

axiom (forall x: int, y: int :: { #or(x, y) } 0 <= x && 0 <= y ==> 0 <= #or(x, y) && #or(x, y) <= x + y);

axiom (forall x: int :: { #and(x, 0 - 1) } #and(x, 0 - 1) == x);

axiom (forall x: int :: { #and(x, 0) } #and(x, 0) == 0);

axiom (forall x: int :: { #or(x, 0 - 1) } #or(x, 0 - 1) == 0 - 1);

axiom (forall x: int :: { #or(x, 0) } #or(x, 0) == x);

axiom (forall i: int :: { #shl(i, 0) } #shl(i, 0) == i);

axiom (forall i: int, j: int :: { #shl(i, j) } 1 <= j ==> #shl(i, j) == #shl(i, j - 1) * 2);

axiom (forall i: int, j: int :: { #shl(i, j) } 0 <= i && i < 32768 && 0 <= j && j <= 16 ==> 0 <= #shl(i, j) && #shl(i, j) <= 2147483647);

axiom (forall i: int :: { #shr(i, 0) } #shr(i, 0) == i);

axiom (forall i: int, j: int :: { #shr(i, j) } 1 <= j ==> #shr(i, j) == #shr(i, j - 1) / 2);

function #min(int, int) : int;

function #max(int, int) : int;

axiom (forall x: int, y: int :: { #min(x, y) } (#min(x, y) == x || #min(x, y) == y) && #min(x, y) <= x && #min(x, y) <= y);

axiom (forall x: int, y: int :: { #max(x, y) } (#max(x, y) == x || #max(x, y) == y) && x <= #max(x, y) && y <= #max(x, y));

function #System.String.IsInterned$System.String$notnull(HeapType, ref) : ref;

function #System.String.Equals$System.String(HeapType, ref, ref) : bool;

function #System.String.Equals$System.String$System.String(HeapType, ref, ref) : bool;

function ##StringEquals(ref, ref) : bool;

axiom (forall h: HeapType, a: ref, b: ref :: { #System.String.Equals$System.String(h, a, b) } #System.String.Equals$System.String(h, a, b) == #System.String.Equals$System.String$System.String(h, a, b));

axiom (forall h: HeapType, a: ref, b: ref :: { #System.String.Equals$System.String$System.String(h, a, b) } #System.String.Equals$System.String$System.String(h, a, b) == ##StringEquals(a, b) && #System.String.Equals$System.String$System.String(h, a, b) == ##StringEquals(b, a) && (a == b ==> ##StringEquals(a, b)));

axiom (forall a: ref, b: ref, c: ref :: ##StringEquals(a, b) && ##StringEquals(b, c) ==> ##StringEquals(a, c));

axiom (forall h: HeapType, a: ref, b: ref :: { #System.String.Equals$System.String$System.String(h, a, b) } a != null && b != null && #System.String.Equals$System.String$System.String(h, a, b) ==> #System.String.IsInterned$System.String$notnull(h, a) == #System.String.IsInterned$System.String$notnull(h, b));

const unique Bag.elems: Field ref;

const unique Bag.count: Field int;

const unique Microsoft.Contracts.ObjectInvariantException: TName;

const unique System.Runtime.InteropServices._Exception: TName;

const unique System.Exception: TName;

const unique System.IComparable: TName;

const unique System.IComparable`1...System.String: TName;

const unique System.Collections.Generic.IEnumerable`1...System.Char: TName;

const unique System.Collections.ICollection: TName;

const unique System.Runtime.Serialization.ISerializable: TName;

const unique System.IConvertible: TName;

const unique Bag: TName;

const unique System.IEquatable`1...System.String: TName;

const unique Microsoft.Contracts.GuardException: TName;

const unique System.Collections.IList: TName;

const unique System.ICloneable: TName;

const unique System.Collections.IEnumerable: TName;

axiom !IsStaticField(Bag.count);

axiom IncludeInMainFrameCondition(Bag.count);

axiom $IncludedInModifiesStar(Bag.count);

axiom DeclType(Bag.count) == Bag;

axiom AsRangeField(Bag.count, System.Int32) == Bag.count;

axiom !IsStaticField(Bag.elems);

axiom IncludeInMainFrameCondition(Bag.elems);

axiom $IncludedInModifiesStar(Bag.elems);

axiom DeclType(Bag.elems) == Bag;

axiom AsNonNullRefField(Bag.elems, IntArray(System.Int32, 1)) == Bag.elems;

axiom Bag <: Bag;

axiom $BaseClass(Bag) == System.Object && AsDirectSubClass(Bag, $BaseClass(Bag)) == Bag;

axiom !$IsImmutable(Bag) && $AsMutable(Bag) == Bag;

axiom (forall $U: TName :: { $U <: Bag } $U <: Bag ==> $U == Bag);

axiom System.Array <: System.Array;

axiom $BaseClass(System.Array) == System.Object && AsDirectSubClass(System.Array, $BaseClass(System.Array)) == System.Array;

axiom !$IsImmutable(System.Array) && $AsMutable(System.Array) == System.Array;

axiom System.ICloneable <: System.ICloneable;

axiom $IsMemberlessType(System.ICloneable);

axiom $AsInterface(System.ICloneable) == System.ICloneable;

axiom System.Array <: System.ICloneable;

axiom System.Collections.IList <: System.Collections.IList;

axiom System.Collections.ICollection <: System.Collections.ICollection;

axiom System.Collections.IEnumerable <: System.Collections.IEnumerable;

axiom $IsMemberlessType(System.Collections.IEnumerable);

axiom $AsInterface(System.Collections.IEnumerable) == System.Collections.IEnumerable;

axiom System.Collections.ICollection <: System.Collections.IEnumerable;

axiom $IsMemberlessType(System.Collections.ICollection);

axiom $AsInterface(System.Collections.ICollection) == System.Collections.ICollection;

axiom System.Collections.IList <: System.Collections.ICollection;

axiom System.Collections.IList <: System.Collections.IEnumerable;

axiom $IsMemberlessType(System.Collections.IList);

axiom $AsInterface(System.Collections.IList) == System.Collections.IList;

axiom System.Array <: System.Collections.IList;

axiom System.Array <: System.Collections.ICollection;

axiom System.Array <: System.Collections.IEnumerable;

axiom $IsMemberlessType(System.Array);

// System.Array object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: System.Array } IsHeap($h) && $h[$oi, $inv] <: System.Array && $h[$oi, $localinv] != $BaseClass(System.Array) ==> true);

// Bag object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: Bag } IsHeap($h) && $h[$oi, $inv] <: Bag && $h[$oi, $localinv] != $BaseClass(Bag) ==> 0 <= $h[$oi, Bag.count] && $h[$oi, Bag.count] <= $Length($h[$oi, Bag.elems]));

procedure Bag.SpecSharp.CheckInvariant$System.Boolean(this: ref where $IsNotNull(this, Bag) && $Heap[this, $allocated], throwException$in: bool where true) returns ($result: bool where true);
  // user-declared preconditions
  requires ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this) && (forall $p: ref :: $p != null && $Heap[$p, $allocated] && $Heap[$p, $ownerRef] == this && $Heap[$p, $ownerFrame] == Bag ==> $Heap[$p, $inv] == $typeof($p) && $Heap[$p, $localinv] == $typeof($p));
  free requires $BeingConstructed == null;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag.SpecSharp.CheckInvariant$System.Boolean(this: ref, throwException$in: bool) returns ($result: bool)
{
  var throwException: bool where true;
  var stack0i: int;
  var stack1i: int;
  var stack0b: bool;
  var stack1o: ref;
  var stack50000o: ref;
  var stack0o: ref;
  var return.value: bool where true;
  var SS$Display.Return.Local: bool where true;

  entry:
    throwException := throwException$in;
    goto block2397;

  block2397:
    goto block2584;

  block2584:
    // ----- nop
    // ----- load constant 0
    stack0i := 0;
    // ----- load field
    assert this != null;
    stack1i := $Heap[this, Bag.count];
    // ----- binary operator
    // ----- branch
    goto true2584to2431, false2584to2448;

  true2584to2431:
    assume stack0i > stack1i;
    goto block2431;

  false2584to2448:
    assume stack0i <= stack1i;
    goto block2448;

  block2431:
    // ----- copy
    stack0b := throwException;
    // ----- unary operator
    // ----- branch
    goto true2431to2618, false2431to2465;

  block2448:
    // ----- load field
    assert this != null;
    stack0i := $Heap[this, Bag.count];
    // ----- load field
    assert this != null;
    stack1o := $Heap[this, Bag.elems];
    // ----- unary operator
    assert stack1o != null;
    stack1i := $Length(stack1o);
    // ----- unary operator
    stack1i := $IntToInt(stack1i, System.UIntPtr, System.Int32);
    // ----- binary operator
    // ----- branch
    goto true2448to2431, false2448to2533;

  true2431to2618:
    assume !stack0b;
    goto block2618;

  false2431to2465:
    assume stack0b;
    goto block2465;

  block2618:
    // ----- load constant 0
    return.value := false;
    // ----- branch
    goto block2550;

  block2465:
    assume false;
    // ----- new object
    havoc stack50000o;
    assume $Heap[stack50000o, $allocated] == false && stack50000o != null && $typeof(stack50000o) == Microsoft.Contracts.ObjectInvariantException;
    assume $Heap[stack50000o, $ownerRef] == stack50000o && $Heap[stack50000o, $ownerFrame] == $PeerGroupPlaceholder;
    // ----- call
    assert stack50000o != null;
    assume $Utter($Heap[stack50000o, $ownerRef]);
    assert (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> IncludeInMainFrameCondition($f) && $Heap[$o, $allocated] && ($Heap[$o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[$o, $ownerRef], $inv] <: $Heap[$o, $ownerFrame]) || $Heap[$Heap[$o, $ownerRef], $localinv] == $BaseClass($Heap[$o, $ownerFrame])) && ($o != stack50000o || !(Microsoft.Contracts.ObjectInvariantException <: DeclType($f))) && true && true);
    call Microsoft.Contracts.ObjectInvariantException..ctor(stack50000o);
    // ----- copy
    stack0o := stack50000o;
    // ----- throw
    assert stack0o != null;
    assume false;
    return;

  true2448to2431:
    assume stack0i > stack1i;
    goto block2431;

  false2448to2533:
    assume stack0i <= stack1i;
    goto block2533;

  block2533:
    // ----- branch
    goto block2499;

  block2550:
    // ----- copy
    SS$Display.Return.Local := return.value;
    // ----- copy
    stack0b := return.value;
    // ----- return
    $result := stack0b;
    return;

  block2499:
    // ----- load constant 1
    return.value := true;
    // ----- branch
    goto block2550;
}



axiom Microsoft.Contracts.ObjectInvariantException <: Microsoft.Contracts.ObjectInvariantException;

axiom Microsoft.Contracts.GuardException <: Microsoft.Contracts.GuardException;

axiom System.Exception <: System.Exception;

axiom $BaseClass(System.Exception) == System.Object && AsDirectSubClass(System.Exception, $BaseClass(System.Exception)) == System.Exception;

axiom !$IsImmutable(System.Exception) && $AsMutable(System.Exception) == System.Exception;

axiom System.Runtime.Serialization.ISerializable <: System.Runtime.Serialization.ISerializable;

axiom $IsMemberlessType(System.Runtime.Serialization.ISerializable);

axiom $AsInterface(System.Runtime.Serialization.ISerializable) == System.Runtime.Serialization.ISerializable;

axiom System.Exception <: System.Runtime.Serialization.ISerializable;

axiom System.Runtime.InteropServices._Exception <: System.Runtime.InteropServices._Exception;

axiom $IsMemberlessType(System.Runtime.InteropServices._Exception);

axiom $AsInterface(System.Runtime.InteropServices._Exception) == System.Runtime.InteropServices._Exception;

axiom System.Exception <: System.Runtime.InteropServices._Exception;

// System.Exception object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: System.Exception } IsHeap($h) && $h[$oi, $inv] <: System.Exception && $h[$oi, $localinv] != $BaseClass(System.Exception) ==> true);

axiom $BaseClass(Microsoft.Contracts.GuardException) == System.Exception && AsDirectSubClass(Microsoft.Contracts.GuardException, $BaseClass(Microsoft.Contracts.GuardException)) == Microsoft.Contracts.GuardException;

axiom !$IsImmutable(Microsoft.Contracts.GuardException) && $AsMutable(Microsoft.Contracts.GuardException) == Microsoft.Contracts.GuardException;

// Microsoft.Contracts.GuardException object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: Microsoft.Contracts.GuardException } IsHeap($h) && $h[$oi, $inv] <: Microsoft.Contracts.GuardException && $h[$oi, $localinv] != $BaseClass(Microsoft.Contracts.GuardException) ==> true);

axiom $BaseClass(Microsoft.Contracts.ObjectInvariantException) == Microsoft.Contracts.GuardException && AsDirectSubClass(Microsoft.Contracts.ObjectInvariantException, $BaseClass(Microsoft.Contracts.ObjectInvariantException)) == Microsoft.Contracts.ObjectInvariantException;

axiom !$IsImmutable(Microsoft.Contracts.ObjectInvariantException) && $AsMutable(Microsoft.Contracts.ObjectInvariantException) == Microsoft.Contracts.ObjectInvariantException;

// Microsoft.Contracts.ObjectInvariantException object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: Microsoft.Contracts.ObjectInvariantException } IsHeap($h) && $h[$oi, $inv] <: Microsoft.Contracts.ObjectInvariantException && $h[$oi, $localinv] != $BaseClass(Microsoft.Contracts.ObjectInvariantException) ==> true);

procedure Microsoft.Contracts.ObjectInvariantException..ctor(this: ref where $IsNotNull(this, Microsoft.Contracts.ObjectInvariantException) && $Heap[this, $allocated]);
  // object is fully unpacked:  this.inv == Object
  free requires ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this);
  // nothing is owned by [this,*] and 'this' is alone in its own peer group
  free requires (forall $o: ref :: $o != this ==> $Heap[$o, $ownerRef] != this) && $Heap[this, $ownerRef] == this && $Heap[this, $ownerFrame] == $PeerGroupPlaceholder;
  free requires $BeingConstructed == this;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // target object is allocated upon return
  free ensures $Heap[this, $allocated];
  // target object is additively exposable for Microsoft.Contracts.ObjectInvariantException
  ensures ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == Microsoft.Contracts.ObjectInvariantException && $Heap[this, $localinv] == $typeof(this);
  ensures $Heap[this, $ownerRef] == old($Heap)[this, $ownerRef] && $Heap[this, $ownerFrame] == old($Heap)[this, $ownerFrame];
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(Microsoft.Contracts.ObjectInvariantException <: DeclType($f))) && true && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] && $o != this ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



procedure Bag..ctor$System.Int32.array(this: ref where $IsNotNull(this, Bag) && $Heap[this, $allocated], initialElements$in: ref where $Is(initialElements$in, IntArray(System.Int32, 1)) && $Heap[initialElements$in, $allocated]);
  // object is fully unpacked:  this.inv == Object
  free requires ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this);
  // initialElements is peer consistent
  requires initialElements$in == null || (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[initialElements$in, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[initialElements$in, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // initialElements is peer consistent (owner must not be valid)
  requires initialElements$in == null || $Heap[initialElements$in, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[initialElements$in, $ownerRef], $inv] <: $Heap[initialElements$in, $ownerFrame]) || $Heap[$Heap[initialElements$in, $ownerRef], $localinv] == $BaseClass($Heap[initialElements$in, $ownerFrame]);
  // nothing is owned by [this,*] and 'this' is alone in its own peer group
  free requires (forall $o: ref :: $o != this ==> $Heap[$o, $ownerRef] != this) && $Heap[this, $ownerRef] == this && $Heap[this, $ownerFrame] == $PeerGroupPlaceholder;
  free requires $BeingConstructed == this;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // target object is allocated upon return
  free ensures $Heap[this, $allocated];
  // target object is additively exposable for Bag
  ensures ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == Bag && $Heap[this, $localinv] == $typeof(this);
  ensures $Heap[this, $ownerRef] == old($Heap)[this, $ownerRef] && $Heap[this, $ownerFrame] == old($Heap)[this, $ownerFrame];
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(Bag <: DeclType($f))) && true && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] && $o != this ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag..ctor$System.Int32.array(this: ref, initialElements$in: ref)
{
  var initialElements: ref where $Is(initialElements, IntArray(System.Int32, 1)) && $Heap[initialElements, $allocated];
  var stack0o: ref;
  var stack0i: int;
  var temp0: exposeVersionType;
  var e: ref where $Is(e, IntArray(System.Int32, 1)) && $Heap[e, $allocated];
  var temp1: ref;

  entry:
    initialElements := initialElements$in;
    assume $Heap[this, Bag.count] == 0;
    goto block3366;

  block3366:
    goto block3434;

  block3434:
    // ----- copy  ----- Bag0.ssc(11,5)
    stack0o := initialElements;
    // ----- unary operator  ----- Bag0.ssc(11,5)
    assert stack0o != null;
    stack0i := $Length(stack0o);
    // ----- unary operator  ----- Bag0.ssc(11,5)
    stack0i := $IntToInt(stack0i, System.UIntPtr, System.Int32);
    // ----- store field  ----- Bag0.ssc(11,5)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp0;
    $Heap[this, $exposeVersion] := temp0;
    // ----- frame condition  ----- Bag0.ssc(11,5)
    assert !(IncludeInMainFrameCondition(Bag.count) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != this || !(Bag <: DeclType(Bag.count))) && true && true);
    $Heap[this, Bag.count] := stack0i;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || 0 <= $Heap[this, Bag.count];
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(12,11)
    stack0o := initialElements;
    // ----- unary operator  ----- Bag0.ssc(12,11)
    assert stack0o != null;
    stack0i := $Length(stack0o);
    // ----- unary operator  ----- Bag0.ssc(12,11)
    stack0i := $IntToInt(stack0i, System.UIntPtr, System.Int32);
    // ----- new array  ----- Bag0.ssc(12,11)
    assert 0 <= stack0i;
    havoc temp1;
    assume $Heap[temp1, $allocated] == false && $Length(temp1) == stack0i;
    assume $Heap[$ElementProxy(temp1, -1), $allocated] == false && $ElementProxy(temp1, -1) != temp1 && $ElementProxy(temp1, -1) != null;
    assume temp1 != null;
    assume $typeof(temp1) == IntArray(System.Int32, 1);
    assume $Heap[temp1, $ownerRef] == temp1 && $Heap[temp1, $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[$ElementProxy(temp1, -1), $ownerRef] == $ElementProxy(temp1, -1) && $Heap[$ElementProxy(temp1, -1), $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[temp1, $inv] == $typeof(temp1) && $Heap[temp1, $localinv] == $typeof(temp1);
    assume (forall $i: int :: ArrayGet($Heap[temp1, $elementsInt], $i) == 0);
    $Heap[temp1, $allocated] := true;
    call System.Object..ctor($ElementProxy(temp1, -1));
    e := temp1;
    assume IsHeap($Heap);
    // ----- serialized AssumeStatement  ----- Bag0.ssc(12,51)
    assume false;
    return;
}



procedure System.Object..ctor(this: ref where $IsNotNull(this, System.Object) && $Heap[this, $allocated]);
  // object is fully unpacked:  this.inv == Object
  free requires ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this);
  // nothing is owned by [this,*] and 'this' is alone in its own peer group
  free requires (forall $o: ref :: $o != this ==> $Heap[$o, $ownerRef] != this) && $Heap[this, $ownerRef] == this && $Heap[this, $ownerFrame] == $PeerGroupPlaceholder;
  free requires $BeingConstructed == this;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // target object is allocated upon return
  free ensures $Heap[this, $allocated];
  // target object is additively exposable for System.Object
  ensures ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this);
  ensures $Heap[this, $ownerRef] == old($Heap)[this, $ownerRef] && $Heap[this, $ownerFrame] == old($Heap)[this, $ownerFrame];
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(System.Object <: DeclType($f))) && true && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] && $o != this ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



axiom System.String <: System.String;

axiom $BaseClass(System.String) == System.Object && AsDirectSubClass(System.String, $BaseClass(System.String)) == System.String;

axiom $IsImmutable(System.String) && $AsImmutable(System.String) == System.String;

axiom System.IComparable <: System.IComparable;

axiom $IsMemberlessType(System.IComparable);

axiom $AsInterface(System.IComparable) == System.IComparable;

axiom System.String <: System.IComparable;

axiom System.String <: System.ICloneable;

axiom System.IConvertible <: System.IConvertible;

axiom $IsMemberlessType(System.IConvertible);

axiom $AsInterface(System.IConvertible) == System.IConvertible;

axiom System.String <: System.IConvertible;

axiom System.IComparable`1...System.String <: System.IComparable`1...System.String;

axiom $IsMemberlessType(System.IComparable`1...System.String);

axiom $AsInterface(System.IComparable`1...System.String) == System.IComparable`1...System.String;

axiom System.String <: System.IComparable`1...System.String;

axiom System.Collections.Generic.IEnumerable`1...System.Char <: System.Collections.Generic.IEnumerable`1...System.Char;

axiom System.Collections.Generic.IEnumerable`1...System.Char <: System.Collections.IEnumerable;

axiom $IsMemberlessType(System.Collections.Generic.IEnumerable`1...System.Char);

axiom $AsInterface(System.Collections.Generic.IEnumerable`1...System.Char) == System.Collections.Generic.IEnumerable`1...System.Char;

axiom System.String <: System.Collections.Generic.IEnumerable`1...System.Char;

axiom System.String <: System.Collections.IEnumerable;

axiom System.IEquatable`1...System.String <: System.IEquatable`1...System.String;

axiom $IsMemberlessType(System.IEquatable`1...System.String);

axiom $AsInterface(System.IEquatable`1...System.String) == System.IEquatable`1...System.String;

axiom System.String <: System.IEquatable`1...System.String;

axiom (forall $U: TName :: { $U <: System.String } $U <: System.String ==> $U == System.String);

// System.String object invariant
axiom (forall $oi: ref, $h: HeapType :: { $h[$oi, $inv] <: System.String } IsHeap($h) && $h[$oi, $inv] <: System.String && $h[$oi, $localinv] != $BaseClass(System.String) ==> true);

procedure Bag..ctor$System.Int32.array$notnull$System.Int32$System.Int32(this: ref where $IsNotNull(this, Bag) && $Heap[this, $allocated], initialElements$in: ref where $IsNotNull(initialElements$in, IntArray(System.Int32, 1)) && $Heap[initialElements$in, $allocated], start$in: int where InRange(start$in, System.Int32), howMany$in: int where InRange(howMany$in, System.Int32));
  // object is fully unpacked:  this.inv == Object
  free requires ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == System.Object && $Heap[this, $localinv] == $typeof(this);
  // user-declared preconditions
  requires 0 <= start$in;
  requires 0 <= howMany$in;
  requires start$in + howMany$in <= $Length(initialElements$in);
  // initialElements is peer consistent
  requires (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[initialElements$in, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[initialElements$in, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // initialElements is peer consistent (owner must not be valid)
  requires $Heap[initialElements$in, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[initialElements$in, $ownerRef], $inv] <: $Heap[initialElements$in, $ownerFrame]) || $Heap[$Heap[initialElements$in, $ownerRef], $localinv] == $BaseClass($Heap[initialElements$in, $ownerFrame]);
  // nothing is owned by [this,*] and 'this' is alone in its own peer group
  free requires (forall $o: ref :: $o != this ==> $Heap[$o, $ownerRef] != this) && $Heap[this, $ownerRef] == this && $Heap[this, $ownerFrame] == $PeerGroupPlaceholder;
  free requires $BeingConstructed == this;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // target object is allocated upon return
  free ensures $Heap[this, $allocated];
  // target object is additively exposable for Bag
  ensures ($Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame])) && $Heap[this, $inv] == Bag && $Heap[this, $localinv] == $typeof(this);
  ensures $Heap[this, $ownerRef] == old($Heap)[this, $ownerRef] && $Heap[this, $ownerFrame] == old($Heap)[this, $ownerFrame];
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(Bag <: DeclType($f))) && true && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] && $o != this ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag..ctor$System.Int32.array$notnull$System.Int32$System.Int32(this: ref, initialElements$in: ref, start$in: int, howMany$in: int)
{
  var initialElements: ref where $IsNotNull(initialElements, IntArray(System.Int32, 1)) && $Heap[initialElements, $allocated];
  var start: int where InRange(start, System.Int32);
  var howMany: int where InRange(howMany, System.Int32);
  var temp0: exposeVersionType;
  var stack0i: int;
  var e: ref where $Is(e, IntArray(System.Int32, 1)) && $Heap[e, $allocated];
  var temp1: ref;
  var stack0o: ref;
  var stack1i: int;
  var stack2o: ref;
  var stack3i: int;
  var stack4i: int;
  var temp2: exposeVersionType;
  var temp3: ref;

  entry:
    initialElements := initialElements$in;
    start := start$in;
    howMany := howMany$in;
    assume $Heap[this, Bag.count] == 0;
    goto block4981;

  block4981:
    goto block5066;

  block5066:
    // ----- nop
    // ----- store field  ----- Bag0.ssc(22,5)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp0;
    $Heap[this, $exposeVersion] := temp0;
    // ----- frame condition  ----- Bag0.ssc(22,5)
    assert !(IncludeInMainFrameCondition(Bag.count) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != this || !(Bag <: DeclType(Bag.count))) && true && true);
    $Heap[this, Bag.count] := howMany;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || 0 <= $Heap[this, Bag.count];
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(23,11)
    stack0i := howMany;
    // ----- new array  ----- Bag0.ssc(23,11)
    assert 0 <= stack0i;
    havoc temp1;
    assume $Heap[temp1, $allocated] == false && $Length(temp1) == stack0i;
    assume $Heap[$ElementProxy(temp1, -1), $allocated] == false && $ElementProxy(temp1, -1) != temp1 && $ElementProxy(temp1, -1) != null;
    assume temp1 != null;
    assume $typeof(temp1) == IntArray(System.Int32, 1);
    assume $Heap[temp1, $ownerRef] == temp1 && $Heap[temp1, $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[$ElementProxy(temp1, -1), $ownerRef] == $ElementProxy(temp1, -1) && $Heap[$ElementProxy(temp1, -1), $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[temp1, $inv] == $typeof(temp1) && $Heap[temp1, $localinv] == $typeof(temp1);
    assume (forall $i: int :: ArrayGet($Heap[temp1, $elementsInt], $i) == 0);
    $Heap[temp1, $allocated] := true;
    call System.Object..ctor($ElementProxy(temp1, -1));
    e := temp1;
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(24,5)
    stack0o := initialElements;
    // ----- copy  ----- Bag0.ssc(24,5)
    stack1i := start;
    // ----- copy  ----- Bag0.ssc(24,5)
    stack2o := e;
    // ----- load constant 0  ----- Bag0.ssc(24,5)
    stack3i := 0;
    // ----- binary operator  ----- Bag0.ssc(24,5)
    stack4i := start + howMany;
    // ----- call  ----- Bag0.ssc(24,5)
    assume $Utter($Heap[stack0o, $ownerRef]);
    assume $Utter($Heap[stack2o, $ownerRef]);
    assert (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(Bag <: DeclType($f))) && true && true ==> IncludeInMainFrameCondition($f) && $Heap[$o, $allocated] && ($Heap[$o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[$o, $ownerRef], $inv] <: $Heap[$o, $ownerFrame]) || $Heap[$Heap[$o, $ownerRef], $localinv] == $BaseClass($Heap[$o, $ownerFrame])) && ($o != stack2o || !($typeof(stack2o) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && ($o != stack2o || $f != $exposeVersion));
    call System.Array.Copy$System.Array$notnull$System.Int32$System.Array$notnull$System.Int32$System.Int32(stack0o, stack1i, stack2o, stack3i, stack4i);
    // ----- store field  ----- Bag0.ssc(25,5)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp2;
    $Heap[this, $exposeVersion] := temp2;
    // ----- frame condition  ----- Bag0.ssc(25,5)
    assert !(IncludeInMainFrameCondition(Bag.elems) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != this || !(Bag <: DeclType(Bag.elems))) && true && true);
    $Heap[this, Bag.elems] := e;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    // ----- call  ----- Bag0.ssc(26,5)
    assert this != null;
    assume $Utter($Heap[this, $ownerRef]);
    assert (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != this || !(Bag <: DeclType($f))) && true && true ==> IncludeInMainFrameCondition($f) && $Heap[$o, $allocated] && ($Heap[$o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[$o, $ownerRef], $inv] <: $Heap[$o, $ownerFrame]) || $Heap[$Heap[$o, $ownerRef], $localinv] == $BaseClass($Heap[$o, $ownerFrame])) && ($o != this || !(System.Object <: DeclType($f))) && true && true);
    call System.Object..ctor(this);
    goto block5219;

  block5219:
    // ----- FrameGuard processing  ----- Bag0.ssc(27,3)
    temp3 := this;
    // ----- classic pack  ----- Bag0.ssc(27,3)
    assert temp3 != null;
    assert $Heap[temp3, $inv] == System.Object && $Heap[temp3, $localinv] == $typeof(temp3);
    assert 0 <= $Heap[temp3, Bag.count];
    assert $Heap[temp3, Bag.count] <= $Length($Heap[temp3, Bag.elems]);
    assert (forall $p: ref :: $p != null && $Heap[$p, $allocated] && $Heap[$p, $ownerRef] == temp3 && $Heap[$p, $ownerFrame] == Bag ==> $Heap[$p, $inv] == $typeof($p) && $Heap[$p, $localinv] == $typeof($p));
    $Heap[temp3, $inv] := Bag;
    assume IsHeap($Heap);
    // ----- return  ----- Bag0.ssc(27,3)
    return;
}



procedure System.Array.Copy$System.Array$notnull$System.Int32$System.Array$notnull$System.Int32$System.Int32(sourceArray$in: ref where $IsNotNull(sourceArray$in, System.Array) && $Heap[sourceArray$in, $allocated], sourceIndex$in: int where InRange(sourceIndex$in, System.Int32), destinationArray$in: ref where $IsNotNull(destinationArray$in, System.Array) && $Heap[destinationArray$in, $allocated], destinationIndex$in: int where InRange(destinationIndex$in, System.Int32), length$in: int where InRange(length$in, System.Int32));
  // user-declared preconditions
  requires sourceArray$in != null;
  requires destinationArray$in != null;
  requires $Rank(sourceArray$in) == $Rank(destinationArray$in);
  requires sourceIndex$in >= $LBound(sourceArray$in, 0);
  requires destinationIndex$in >= $LBound(destinationArray$in, 0);
  requires length$in >= 0;
  requires sourceIndex$in + length$in <= $LBound(sourceArray$in, 0) + $Length(sourceArray$in);
  requires destinationIndex$in + length$in <= $LBound(destinationArray$in, 0) + $Length(destinationArray$in);
  // sourceArray is peer consistent
  requires (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[sourceArray$in, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[sourceArray$in, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // sourceArray is peer consistent (owner must not be valid)
  requires $Heap[sourceArray$in, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[sourceArray$in, $ownerRef], $inv] <: $Heap[sourceArray$in, $ownerFrame]) || $Heap[$Heap[sourceArray$in, $ownerRef], $localinv] == $BaseClass($Heap[sourceArray$in, $ownerFrame]);
  // destinationArray is peer consistent
  requires (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[destinationArray$in, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[destinationArray$in, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // destinationArray is peer consistent (owner must not be valid)
  requires $Heap[destinationArray$in, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[destinationArray$in, $ownerRef], $inv] <: $Heap[destinationArray$in, $ownerFrame]) || $Heap[$Heap[destinationArray$in, $ownerRef], $localinv] == $BaseClass($Heap[destinationArray$in, $ownerFrame]);
  free requires $BeingConstructed == null;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // hard-coded postcondition
  ensures (forall $k: int :: { ArrayGet($Heap[destinationArray$in, $elementsBool], $k) } (destinationIndex$in <= $k && $k < destinationIndex$in + length$in ==> old(ArrayGet($Heap[sourceArray$in, $elementsBool], $k + sourceIndex$in - destinationIndex$in)) == ArrayGet($Heap[destinationArray$in, $elementsBool], $k)) && (!(destinationIndex$in <= $k && $k < destinationIndex$in + length$in) ==> old(ArrayGet($Heap[destinationArray$in, $elementsBool], $k)) == ArrayGet($Heap[destinationArray$in, $elementsBool], $k)));
  ensures (forall $k: int :: { ArrayGet($Heap[destinationArray$in, $elementsInt], $k) } (destinationIndex$in <= $k && $k < destinationIndex$in + length$in ==> old(ArrayGet($Heap[sourceArray$in, $elementsInt], $k + sourceIndex$in - destinationIndex$in)) == ArrayGet($Heap[destinationArray$in, $elementsInt], $k)) && (!(destinationIndex$in <= $k && $k < destinationIndex$in + length$in) ==> old(ArrayGet($Heap[destinationArray$in, $elementsInt], $k)) == ArrayGet($Heap[destinationArray$in, $elementsInt], $k)));
  ensures (forall $k: int :: { ArrayGet($Heap[destinationArray$in, $elementsRef], $k) } (destinationIndex$in <= $k && $k < destinationIndex$in + length$in ==> old(ArrayGet($Heap[sourceArray$in, $elementsRef], $k + sourceIndex$in - destinationIndex$in)) == ArrayGet($Heap[destinationArray$in, $elementsRef], $k)) && (!(destinationIndex$in <= $k && $k < destinationIndex$in + length$in) ==> old(ArrayGet($Heap[destinationArray$in, $elementsRef], $k)) == ArrayGet($Heap[destinationArray$in, $elementsRef], $k)));
  ensures (forall $k: int :: { ArrayGet($Heap[destinationArray$in, $elementsReal], $k) } (destinationIndex$in <= $k && $k < destinationIndex$in + length$in ==> old(ArrayGet($Heap[sourceArray$in, $elementsReal], $k + sourceIndex$in - destinationIndex$in)) == ArrayGet($Heap[destinationArray$in, $elementsReal], $k)) && (!(destinationIndex$in <= $k && $k < destinationIndex$in + length$in) ==> old(ArrayGet($Heap[destinationArray$in, $elementsReal], $k)) == ArrayGet($Heap[destinationArray$in, $elementsReal], $k)));
  ensures (forall $k: int :: { ArrayGet($Heap[destinationArray$in, $elementsStruct], $k) } (destinationIndex$in <= $k && $k < destinationIndex$in + length$in ==> old(ArrayGet($Heap[sourceArray$in, $elementsStruct], $k + sourceIndex$in - destinationIndex$in)) == ArrayGet($Heap[destinationArray$in, $elementsStruct], $k)) && (!(destinationIndex$in <= $k && $k < destinationIndex$in + length$in) ==> old(ArrayGet($Heap[destinationArray$in, $elementsStruct], $k)) == ArrayGet($Heap[destinationArray$in, $elementsStruct], $k)));
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(destinationArray$in) || !($typeof(old(destinationArray$in)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && ($o != old(destinationArray$in) || $f != old($exposeVersion)) ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



procedure Bag.RemoveMin(this: ref where $IsNotNull(this, Bag) && $Heap[this, $allocated]) returns ($result: int where InRange($result, System.Int32));
  // target object is peer consistent
  requires (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[this, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[this, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // target object is peer consistent (owner must not be valid)
  requires $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
  free requires $BeingConstructed == null;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag.RemoveMin(this: ref) returns ($result: int)
{
  var m: int where InRange(m, System.Int32);
  var mindex: int where InRange(mindex, System.Int32);
  var i: int where InRange(i, System.Int32);
  var stack0i: int;
  var stack0b: bool;
  var stack0o: ref;
  var stack1i: int;
  var local5: int where InRange(local5, System.Int32);
  var temp0: exposeVersionType;
  var stack2o: ref;
  var stack3i: int;
  var stack2i: int;
  var return.value: int where InRange(return.value, System.Int32);
  var local4: int where InRange(local4, System.Int32);
  var SS$Display.Return.Local: int where InRange(SS$Display.Return.Local, System.Int32);
  var $Heap$block5916$LoopPreheader: HeapType;

  entry:
    goto block5899;

  block5899:
    goto block6120;

  block6120:
    // ----- nop
    // ----- load constant 2147483647  ----- Bag0.ssc(31,9)
    m := 2147483647;
    // ----- load constant 0  ----- Bag0.ssc(32,9)
    mindex := 0;
    // ----- load constant 0  ----- Bag0.ssc(33,14)
    i := 0;
    goto block5916$LoopPreheader;

  block5916:
    // ----- default loop invariant: allocation and ownership are stable  ----- Bag0.ssc(33,21)
    assume (forall $o: ref :: { $Heap[$o, $allocated] } $Heap$block5916$LoopPreheader[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } $Heap$block5916$LoopPreheader[$ot, $allocated] && $Heap$block5916$LoopPreheader[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == $Heap$block5916$LoopPreheader[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == $Heap$block5916$LoopPreheader[$ot, $ownerFrame]) && $Heap$block5916$LoopPreheader[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];
    // ----- default loop invariant: exposure  ----- Bag0.ssc(33,21)
    assume (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $Heap$block5916$LoopPreheader[$o, $allocated] ==> $Heap$block5916$LoopPreheader[$o, $inv] == $Heap[$o, $inv] && $Heap$block5916$LoopPreheader[$o, $localinv] == $Heap[$o, $localinv]);
    assume (forall $o: ref :: !$Heap$block5916$LoopPreheader[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
    assume $Heap[this, $inv] == $Heap$block5916$LoopPreheader[this, $inv];
    // ----- default loop invariant: modifies  ----- Bag0.ssc(33,21)
    assume (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> $Heap$block5916$LoopPreheader[$o, $f] == $Heap[$o, $f]);
    assume $HeapSucc($Heap$block5916$LoopPreheader, $Heap);
    // ----- default loop invariant: owner fields  ----- Bag0.ssc(33,21)
    assert (forall $o: ref :: { $Heap[$o, $ownerFrame] } { $Heap[$o, $ownerRef] } $o != null && $Heap$block5916$LoopPreheader[$o, $allocated] ==> $Heap[$o, $ownerRef] == $Heap$block5916$LoopPreheader[$o, $ownerRef] && $Heap[$o, $ownerFrame] == $Heap$block5916$LoopPreheader[$o, $ownerFrame]);
    // ----- advance activity
    havoc $ActivityIndicator;
    // ----- load field  ----- Bag0.ssc(33,21)
    assert this != null;
    stack0i := $Heap[this, Bag.count];
    // ----- binary operator  ----- Bag0.ssc(33,21)
    // ----- branch  ----- Bag0.ssc(33,21)
    goto true5916to6069, false5916to6103;

  true5916to6069:
    assume i >= stack0i;
    goto block6069;

  false5916to6103:
    assume i < stack0i;
    goto block6103;

  block6069:
    // ----- load field  ----- Bag0.ssc(39,5)
    assert this != null;
    local5 := $Heap[this, Bag.count];
    // ----- load constant 1  ----- Bag0.ssc(39,5)
    stack0i := 1;
    // ----- binary operator  ----- Bag0.ssc(39,5)
    stack0i := local5 - stack0i;
    // ----- store field  ----- Bag0.ssc(39,5)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp0;
    $Heap[this, $exposeVersion] := temp0;
    // ----- frame condition  ----- Bag0.ssc(39,5)
    assert !(IncludeInMainFrameCondition(Bag.count) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != old(this) || !($typeof(old(this)) <: DeclType(Bag.count)) || !$IncludedInModifiesStar(Bag.count)) && true);
    $Heap[this, Bag.count] := stack0i;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || 0 <= $Heap[this, Bag.count];
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(39,5)
    stack0i := local5;
    // ----- load field  ----- Bag0.ssc(40,5)
    assert this != null;
    stack0o := $Heap[this, Bag.elems];
    // ----- copy  ----- Bag0.ssc(40,5)
    stack1i := mindex;
    // ----- load field  ----- Bag0.ssc(40,5)
    assert this != null;
    stack2o := $Heap[this, Bag.elems];
    // ----- load field  ----- Bag0.ssc(40,5)
    assert this != null;
    stack3i := $Heap[this, Bag.count];
    // ----- load element  ----- Bag0.ssc(40,5)
    assert stack2o != null;
    assert 0 <= stack3i;
    assert stack3i < $Length(stack2o);
    stack2i := ArrayGet($Heap[stack2o, $elementsInt], stack3i);
    // ----- store element  ----- Bag0.ssc(40,5)
    assert stack0o != null;
    assert 0 <= stack1i;
    assert stack1i < $Length(stack0o);
    assert $Heap[stack0o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[stack0o, $ownerRef], $inv] <: $Heap[stack0o, $ownerFrame]) || $Heap[$Heap[stack0o, $ownerRef], $localinv] == $BaseClass($Heap[stack0o, $ownerFrame]);
    // ----- frame condition  ----- Bag0.ssc(40,5)
    assert !(old($Heap)[stack0o, $allocated] && (old($Heap)[stack0o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[stack0o, $ownerRef], $inv] <: old($Heap)[stack0o, $ownerFrame]) || old($Heap)[old($Heap)[stack0o, $ownerRef], $localinv] == $BaseClass(old($Heap)[stack0o, $ownerFrame])) && true);
    $Heap[stack0o, $elementsInt] := ArraySet($Heap[stack0o, $elementsInt], stack1i, stack2i);
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(41,5)
    return.value := m;
    // ----- branch  ----- Bag0.ssc(41,5)
    goto block5967;

  block6103:
    // ----- load field  ----- Bag0.ssc(34,7)
    assert this != null;
    stack0o := $Heap[this, Bag.elems];
    // ----- copy  ----- Bag0.ssc(34,7)
    stack1i := i;
    // ----- load element  ----- Bag0.ssc(34,7)
    assert stack0o != null;
    assert 0 <= stack1i;
    assert stack1i < $Length(stack0o);
    stack0i := ArrayGet($Heap[stack0o, $elementsInt], stack1i);
    // ----- binary operator  ----- Bag0.ssc(34,7)
    // ----- branch  ----- Bag0.ssc(34,7)
    goto true6103to5984, false6103to6018;

  true6103to5984:
    assume stack0i >= m;
    goto block5984;

  false6103to6018:
    assume stack0i < m;
    goto block6018;

  block5984:
    // ----- copy  ----- Bag0.ssc(33,32)
    local4 := i;
    // ----- load constant 1  ----- Bag0.ssc(33,32)
    stack0i := 1;
    // ----- binary operator  ----- Bag0.ssc(33,32)
    stack0i := local4 + stack0i;
    // ----- copy  ----- Bag0.ssc(33,32)
    i := stack0i;
    // ----- copy  ----- Bag0.ssc(33,32)
    stack0i := local4;
    // ----- branch  ----- Bag0.ssc(33,32)
    goto block5916;

  block6018:
    // ----- copy  ----- Bag0.ssc(35,9)
    mindex := i;
    // ----- load field  ----- Bag0.ssc(36,9)
    assert this != null;
    stack0o := $Heap[this, Bag.elems];
    // ----- copy  ----- Bag0.ssc(36,9)
    stack1i := i;
    // ----- load element  ----- Bag0.ssc(36,9)
    assert stack0o != null;
    assert 0 <= stack1i;
    assert stack1i < $Length(stack0o);
    m := ArrayGet($Heap[stack0o, $elementsInt], stack1i);
    goto block5984;

  block5967:
    // ----- copy
    SS$Display.Return.Local := return.value;
    // ----- copy
    stack0i := return.value;
    // ----- return
    $result := stack0i;
    return;

  block5916$LoopPreheader:
    $Heap$block5916$LoopPreheader := $Heap;
    goto block5916;
}



procedure Bag.Add$System.Int32(this: ref where $IsNotNull(this, Bag) && $Heap[this, $allocated], x$in: int where InRange(x$in, System.Int32));
  // target object is peer consistent
  requires (forall $pc: ref :: { $typeof($pc) } { $Heap[$pc, $localinv] } { $Heap[$pc, $inv] } { $Heap[$pc, $ownerFrame] } { $Heap[$pc, $ownerRef] } $pc != null && $Heap[$pc, $allocated] && $Heap[$pc, $ownerRef] == $Heap[this, $ownerRef] && $Heap[$pc, $ownerFrame] == $Heap[this, $ownerFrame] ==> $Heap[$pc, $inv] == $typeof($pc) && $Heap[$pc, $localinv] == $typeof($pc));
  // target object is peer consistent (owner must not be valid)
  requires $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
  free requires $BeingConstructed == null;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag.Add$System.Int32(this: ref, x$in: int)
{
  var x: int where InRange(x, System.Int32);
  var stack0i: int;
  var stack1o: ref;
  var stack1i: int;
  var stack0b: bool;
  var b: ref where $Is(b, IntArray(System.Int32, 1)) && $Heap[b, $allocated];
  var temp0: ref;
  var stack0o: ref;
  var stack2o: ref;
  var stack3i: int;
  var stack4o: ref;
  var stack4i: int;
  var temp1: exposeVersionType;
  var local2: int where InRange(local2, System.Int32);
  var temp2: exposeVersionType;

  entry:
    x := x$in;
    goto block7004;

  block7004:
    goto block7106;

  block7106:
    // ----- nop
    // ----- load field  ----- Bag0.ssc(46,5)
    assert this != null;
    stack0i := $Heap[this, Bag.count];
    // ----- load field  ----- Bag0.ssc(46,5)
    assert this != null;
    stack1o := $Heap[this, Bag.elems];
    // ----- unary operator  ----- Bag0.ssc(46,5)
    assert stack1o != null;
    stack1i := $Length(stack1o);
    // ----- unary operator  ----- Bag0.ssc(46,5)
    stack1i := $IntToInt(stack1i, System.UIntPtr, System.Int32);
    // ----- binary operator  ----- Bag0.ssc(46,5)
    // ----- branch  ----- Bag0.ssc(46,5)
    goto true7106to7123, false7106to7072;

  true7106to7123:
    assume stack0i != stack1i;
    goto block7123;

  false7106to7072:
    assume stack0i == stack1i;
    goto block7072;

  block7123:
    // ----- load field  ----- Bag0.ssc(52,5)
    assert this != null;
    stack0o := $Heap[this, Bag.elems];
    // ----- load field  ----- Bag0.ssc(52,5)
    assert this != null;
    stack1i := $Heap[this, Bag.count];
    // ----- store element  ----- Bag0.ssc(52,5)
    assert stack0o != null;
    assert 0 <= stack1i;
    assert stack1i < $Length(stack0o);
    assert $Heap[stack0o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[stack0o, $ownerRef], $inv] <: $Heap[stack0o, $ownerFrame]) || $Heap[$Heap[stack0o, $ownerRef], $localinv] == $BaseClass($Heap[stack0o, $ownerFrame]);
    // ----- frame condition  ----- Bag0.ssc(52,5)
    assert !(old($Heap)[stack0o, $allocated] && (old($Heap)[stack0o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[stack0o, $ownerRef], $inv] <: old($Heap)[stack0o, $ownerFrame]) || old($Heap)[old($Heap)[stack0o, $ownerRef], $localinv] == $BaseClass(old($Heap)[stack0o, $ownerFrame])) && true);
    $Heap[stack0o, $elementsInt] := ArraySet($Heap[stack0o, $elementsInt], stack1i, x);
    assume IsHeap($Heap);
    // ----- load field  ----- Bag0.ssc(53,5)
    assert this != null;
    local2 := $Heap[this, Bag.count];
    // ----- load constant 1  ----- Bag0.ssc(53,5)
    stack0i := 1;
    // ----- binary operator  ----- Bag0.ssc(53,5)
    stack0i := local2 + stack0i;
    // ----- store field  ----- Bag0.ssc(53,5)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp2;
    $Heap[this, $exposeVersion] := temp2;
    // ----- frame condition  ----- Bag0.ssc(53,5)
    assert !(IncludeInMainFrameCondition(Bag.count) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != old(this) || !($typeof(old(this)) <: DeclType(Bag.count)) || !$IncludedInModifiesStar(Bag.count)) && true);
    $Heap[this, Bag.count] := stack0i;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || 0 <= $Heap[this, Bag.count];
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    // ----- copy  ----- Bag0.ssc(53,5)
    stack0i := local2;
    // ----- return  ----- Bag0.ssc(53,5)
    return;

  block7072:
    // ----- load constant 2  ----- Bag0.ssc(48,14)
    stack0i := 2;
    // ----- load field  ----- Bag0.ssc(48,14)
    assert this != null;
    stack1o := $Heap[this, Bag.elems];
    // ----- unary operator  ----- Bag0.ssc(48,14)
    assert stack1o != null;
    stack1i := $Length(stack1o);
    // ----- unary operator  ----- Bag0.ssc(48,14)
    stack1i := $IntToInt(stack1i, System.UIntPtr, System.Int32);
    // ----- binary operator  ----- Bag0.ssc(48,14)
    stack0i := stack0i * stack1i;
    // ----- new array  ----- Bag0.ssc(48,14)
    assert 0 <= stack0i;
    havoc temp0;
    assume $Heap[temp0, $allocated] == false && $Length(temp0) == stack0i;
    assume $Heap[$ElementProxy(temp0, -1), $allocated] == false && $ElementProxy(temp0, -1) != temp0 && $ElementProxy(temp0, -1) != null;
    assume temp0 != null;
    assume $typeof(temp0) == IntArray(System.Int32, 1);
    assume $Heap[temp0, $ownerRef] == temp0 && $Heap[temp0, $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[$ElementProxy(temp0, -1), $ownerRef] == $ElementProxy(temp0, -1) && $Heap[$ElementProxy(temp0, -1), $ownerFrame] == $PeerGroupPlaceholder;
    assume $Heap[temp0, $inv] == $typeof(temp0) && $Heap[temp0, $localinv] == $typeof(temp0);
    assume (forall $i: int :: ArrayGet($Heap[temp0, $elementsInt], $i) == 0);
    $Heap[temp0, $allocated] := true;
    call System.Object..ctor($ElementProxy(temp0, -1));
    b := temp0;
    assume IsHeap($Heap);
    // ----- load field  ----- Bag0.ssc(49,7)
    assert this != null;
    stack0o := $Heap[this, Bag.elems];
    // ----- load constant 0  ----- Bag0.ssc(49,7)
    stack1i := 0;
    // ----- copy  ----- Bag0.ssc(49,7)
    stack2o := b;
    // ----- load constant 0  ----- Bag0.ssc(49,7)
    stack3i := 0;
    // ----- load field  ----- Bag0.ssc(49,7)
    assert this != null;
    stack4o := $Heap[this, Bag.elems];
    // ----- unary operator  ----- Bag0.ssc(49,7)
    assert stack4o != null;
    stack4i := $Length(stack4o);
    // ----- unary operator  ----- Bag0.ssc(49,7)
    stack4i := $IntToInt(stack4i, System.UIntPtr, System.Int32);
    // ----- call  ----- Bag0.ssc(49,7)
    assume $Utter($Heap[stack0o, $ownerRef]);
    assume $Utter($Heap[stack2o, $ownerRef]);
    assert (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && ($o != old(this) || !($typeof(old(this)) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && true ==> IncludeInMainFrameCondition($f) && $Heap[$o, $allocated] && ($Heap[$o, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[$o, $ownerRef], $inv] <: $Heap[$o, $ownerFrame]) || $Heap[$Heap[$o, $ownerRef], $localinv] == $BaseClass($Heap[$o, $ownerFrame])) && ($o != stack2o || !($typeof(stack2o) <: DeclType($f)) || !$IncludedInModifiesStar($f)) && ($o != stack2o || $f != $exposeVersion));
    call System.Array.Copy$System.Array$notnull$System.Int32$System.Array$notnull$System.Int32$System.Int32(stack0o, stack1i, stack2o, stack3i, stack4i);
    // ----- store field  ----- Bag0.ssc(50,7)
    assert this != null;
    assert $Heap[this, $ownerFrame] == $PeerGroupPlaceholder || !($Heap[$Heap[this, $ownerRef], $inv] <: $Heap[this, $ownerFrame]) || $Heap[$Heap[this, $ownerRef], $localinv] == $BaseClass($Heap[this, $ownerFrame]);
    havoc temp1;
    $Heap[this, $exposeVersion] := temp1;
    // ----- frame condition  ----- Bag0.ssc(50,7)
    assert !(IncludeInMainFrameCondition(Bag.elems) && old($Heap)[this, $allocated] && (old($Heap)[this, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[this, $ownerRef], $inv] <: old($Heap)[this, $ownerFrame]) || old($Heap)[old($Heap)[this, $ownerRef], $localinv] == $BaseClass(old($Heap)[this, $ownerFrame])) && (this != old(this) || !($typeof(old(this)) <: DeclType(Bag.elems)) || !$IncludedInModifiesStar(Bag.elems)) && true);
    $Heap[this, Bag.elems] := b;
    assume IsPreHeap($Heap);
    assert !($Heap[this, $inv] <: Bag && $Heap[this, $localinv] != $BaseClass(Bag)) || $Heap[this, Bag.count] <= $Length($Heap[this, Bag.elems]);
    assume IsHeap($Heap);
    goto block7123;
}



procedure Bag..cctor();
  free requires $BeingConstructed == null;
  free requires $PurityAxiomsCanBeAssumed;
  modifies $Heap, $ActivityIndicator;
  // newly allocated objects are fully valid
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } $o != null && !old($Heap)[$o, $allocated] && $Heap[$o, $allocated] ==> $Heap[$o, $inv] == $typeof($o) && $Heap[$o, $localinv] == $typeof($o));
  // first consistent owner unchanged if its exposeVersion is
  free ensures (forall $o: ref :: { $Heap[$o, $FirstConsistentOwner] } old($Heap)[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] == $Heap[old($Heap)[$o, $FirstConsistentOwner], $exposeVersion] ==> old($Heap)[$o, $FirstConsistentOwner] == $Heap[$o, $FirstConsistentOwner]);
  // frame condition
  free ensures (forall<alpha> $o: ref, $f: Field alpha :: { $Heap[$o, $f] } $o != null && IncludeInMainFrameCondition($f) && old($Heap)[$o, $allocated] && (old($Heap)[$o, $ownerFrame] == $PeerGroupPlaceholder || !(old($Heap)[old($Heap)[$o, $ownerRef], $inv] <: old($Heap)[$o, $ownerFrame]) || old($Heap)[old($Heap)[$o, $ownerRef], $localinv] == $BaseClass(old($Heap)[$o, $ownerFrame])) && true && true ==> old($Heap)[$o, $f] == $Heap[$o, $f]);
  free ensures $HeapSucc(old($Heap), $Heap);
  // inv/localinv change only in blocks
  free ensures (forall $o: ref :: { $Heap[$o, $localinv] } { $Heap[$o, $inv] } old($Heap)[$o, $allocated] ==> old($Heap)[$o, $inv] == $Heap[$o, $inv] && old($Heap)[$o, $localinv] == $Heap[$o, $localinv]);
  free ensures (forall $o: ref :: { $Heap[$o, $allocated] } old($Heap)[$o, $allocated] ==> $Heap[$o, $allocated]) && (forall $ot: ref :: { $Heap[$ot, $ownerFrame] } { $Heap[$ot, $ownerRef] } old($Heap)[$ot, $allocated] && old($Heap)[$ot, $ownerFrame] != $PeerGroupPlaceholder ==> $Heap[$ot, $ownerRef] == old($Heap)[$ot, $ownerRef] && $Heap[$ot, $ownerFrame] == old($Heap)[$ot, $ownerFrame]) && old($Heap)[$BeingConstructed, $NonNullFieldsAreInitialized] == $Heap[$BeingConstructed, $NonNullFieldsAreInitialized];



implementation Bag..cctor()
{

  entry:
    goto block7854;

  block7854:
    goto block7922;

  block7922:
    // ----- nop
    // ----- return
    return;
}


