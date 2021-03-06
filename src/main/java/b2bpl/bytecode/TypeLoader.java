package b2bpl.bytecode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import b2bpl.Project;
import b2bpl.bytecode.analysis.SemanticAnalyzer;
import b2bpl.bytecode.attributes.AssertAttribute;
import b2bpl.bytecode.attributes.AssumeAttribute;
import b2bpl.bytecode.attributes.ClassInvariantAttribute;
import b2bpl.bytecode.attributes.ConstraintAttribute;
import b2bpl.bytecode.attributes.LoopSpecificationAttribute;
import b2bpl.bytecode.attributes.MethodSpecificationAttribute;
import b2bpl.bytecode.attributes.ModelFieldAttribute;
import b2bpl.bytecode.bml.ast.BMLAssertStatement;
import b2bpl.bytecode.bml.ast.BMLAssumeStatement;
import b2bpl.bytecode.bml.ast.BMLConstraint;
import b2bpl.bytecode.bml.ast.BMLInvariant;
import b2bpl.bytecode.bml.ast.BMLLoopSpecification;
import b2bpl.bytecode.instructions.Instruction;
import b2bpl.bytecode.instructions.LocalVariableInstruction;


public class TypeLoader {

  private final HashMap<String, JClassType> classTypes = new HashMap<String, JClassType>();

  private final HashSet<JClassType> loadedClassTypes = new HashSet<JClassType>();

  private Project project = null;
  
  private HashSet<String> projectTypes = new HashSet<String>();

  private ISpecificationProvider specProvider = new EmptySpecificationProvider();

  private SemanticAnalyzer semanticAnalyzer;

  private ITroubleReporter troubleReporter;

  public InstructionFactory instructionFactory = new InstructionFactory(this);

  public TypeLoader() {
  }
  
  public void setProject(Project p) {
    project = p;
  }

  public  void setProjectTypes(String... types) {
    loadedClassTypes.clear();
    projectTypes = new HashSet<String>();
    for (String type : types) {
      projectTypes.add(type.replace('/', '.'));
    }
  }

  public  void setSpecificationProvider(ISpecificationProvider provider) {
    assert provider != null;
    specProvider = provider;
  }

  public  void setSemanticAnalyzer(SemanticAnalyzer analyzer) {
    semanticAnalyzer = analyzer;
  }

  public  void setTroubleReporter(ITroubleReporter reporter) {
    troubleReporter = reporter;
  }

  public  JClassType getClassType(String name) {
    name = name.replace('.', '/');
    JClassType type = classTypes.get(name);
    
    if (type == null) {
      type = new JClassType(this, name);
      classTypes.put(name, type);
      
      
    }
    return type;
  }
  
  private  ClassReader getClassReader(String typeName) {
    try {
//      // Create class reader from type name
////System.out.println("Reading class from name:\t" + typeName);
      return new ClassReader(typeName);
    } catch (IOException ioex) {
      // Create class reader from file stream
//System.out.println("Reading class from stream:\t" + typeName);
         
      InputStream is = null;
      
      String localPath = project.getBaseDirectory(); // typeName.substring(0, typeName.lastIndexOf("\\"));
      File file = new File(localPath);
      
      try {
        // Convert File to a URL
        URL url = file.toURI().toURL();         // file:/c:/myclasses/
        URL[] urls = new URL[] { url };
        
        // Create a new class loader with the directory
        ClassLoader cl = new URLClassLoader(urls);
        // Class cls = cl.loadClass(typeName);
        
        // Create input stream containing the contents of the given class      
        String className = typeName.replace('.', '/') + ".class";
        is = cl.getResourceAsStream(className);
      } catch (MalformedURLException murlex) {
        System.err.println(murlex.toString());
      //} catch (ClassNotFoundException cnfex) {
      //  System.err.println(cnfex.toString());
      }
  
      try {
        return new ClassReader(is);
      } catch (IOException ioe) {
        System.err.println("Cannot create class reader from " + typeName);
      }
      return null;
    }
  }

  public  void loadType(String name) {
    
    JClassType type = getClassType(name);
    
    if (loadedClassTypes.add(type)) {
      try {
//        int flags = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
        int flags = ClassReader.SKIP_FRAMES;
        Attribute[] attributes;
        if (projectTypes.contains(type.getName())) {
          attributes = new Attribute[] {
              new ClassInvariantAttribute(this, type),
              new ConstraintAttribute(this, type),
              new ModelFieldAttribute(this, type),
              new MethodSpecificationAttribute(this),
              new AssertAttribute(this),
              new AssumeAttribute(this),
              new LoopSpecificationAttribute(this)
          };
        } else {
          attributes = new Attribute[] {
              new ClassInvariantAttribute(this, type),
              new ConstraintAttribute(this, type),
              new ModelFieldAttribute(this, type),
              new MethodSpecificationAttribute(this)
          };
          flags |= ClassReader.SKIP_CODE;
        }
       
//System.out.println("L 167: " + type.getName());
        ClassReader reader = getClassReader(type.getName());

        JClassTypeBuilder builder = new JClassTypeBuilder(type);
        
        reader.accept(specProvider.forClass(type, builder), attributes, flags);
        semanticAnalyzer.analyze(type);
        
      //} catch (IOException ioe) {
      //  troubleReporter.reportTrouble(
      //      new TroubleMessage(B2BPLMessages.CLASS_NOT_FOUND, name));
      } catch (TroubleException te) {
        if (te.getTroubleMessage().getPosition() == null) {
          te.getTroubleMessage().setPosition(
              new TroublePosition(type, null, null));
        }
        throw te;
      }
    }
  }

  public  JClassType[] getClassTypes() {
    return classTypes.values().toArray(new JClassType[classTypes.size()]);
  }

  public  ClassNode getASMClassTypeNode(JClassType type) {
    try {
      ClassNode cn = new ClassNode();
//System.out.println("L 194: " + type.getName());
      ClassReader cr = getClassReader(type.getName()); // new ClassReader(type.getName());
      cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      return cn;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private  final class JClassTypeBuilder implements ClassVisitor {

    private final JClassType type;

    private final List<BCField> fields = new ArrayList<BCField>();

    private final List<BCMethod> methods = new ArrayList<BCMethod>();

    private final List<BMLInvariant> invariants = new ArrayList<BMLInvariant>();

    private final List<BMLConstraint> constraints =
      new ArrayList<BMLConstraint>();

    public JClassTypeBuilder(JClassType type) {
      this.type = type;
    }

    public void visit(
        int version,
        int access,
        String name,
        String signature,
        String superName,
        String[] interfaceNames) {
      JClassType supertype = null;
      if (superName != null) {
        supertype = getClassType(superName);
      }

      JClassType[] interfaces = JClassType.EMPTY_ARRAY;
      if (interfaceNames != null) {
        interfaces = new JClassType[interfaceNames.length];
        for (int i = 0; i < interfaces.length; i++) {
          interfaces[i] = getClassType(interfaceNames[i]);
        }
      }

      type.setTypeInfo(access, supertype, interfaces);
    }

    public void visitSource(String source, String debug) {
      // do nothing
    }

    public void visitOuterClass(String owner, String name, String descriptor) {
      // do nothing
    }

    public AnnotationVisitor visitAnnotation(
        String descriptor,
        boolean visible) {
      // do nothing
      return null;
    }

    public void visitAttribute(Attribute attribute) {
      if (attribute instanceof ClassInvariantAttribute) {
        ClassInvariantAttribute invAttr = (ClassInvariantAttribute) attribute;
        invariants.addAll(Arrays.asList(invAttr.getInvariants()));
      } else if (attribute instanceof ConstraintAttribute) {
        ConstraintAttribute conAttr = (ConstraintAttribute) attribute;
        constraints.addAll(Arrays.asList(conAttr.getConstraints()));
      } else if (attribute instanceof ModelFieldAttribute) {
        ModelFieldAttribute modAttr = (ModelFieldAttribute) attribute;
        for (BCField field : modAttr.getFields()) {
          fields.add(field);
        }
      }
    }

    public void visitInnerClass(
        String name,
        String outerName,
        String innerName,
        int access) {
      // do nothing
    }

    public FieldVisitor visitField(
        int access,
        String name,
        String descriptor,
        String signature,
        Object value) {
      JType fieldType = JType.fromDescriptor(TypeLoader.this, descriptor);

      BCField field = new BCField(access, type, name, fieldType);
      fields.add(field);

      return specProvider.forField(field, null);
    }

    public MethodVisitor visitMethod(
        int access,
        String name,
        String descriptor,
        String signature,
        String[] exceptionNames) {
      JType returnType = JType.returnType(TypeLoader.this, descriptor);
      JType[] argumentTypes = JType.argumentTypes(TypeLoader.this, descriptor);
      JClassType[] exceptions = JClassType.EMPTY_ARRAY;
      if (exceptionNames != null) {
        exceptions = new JClassType[exceptionNames.length];
        for (int i = 0; i < exceptions.length; i++) {
          exceptions[i] = getClassType(exceptionNames[i]);
        }
      }

      final BCMethod method = new BCMethod(
          access,
          type,
          name,
          returnType,
          argumentTypes,
          exceptions);
      methods.add(method);

      try {
        return specProvider.forMethod(method, new BCMethodBuilder(method));
      } catch (TroubleException te) {
        if (te.getTroubleMessage().getPosition() == null) {
          te.getTroubleMessage().setPosition(new TroublePosition(method, null));
        }
        throw te;
      }
    }

    public void visitEnd() {
      type.setDeclarations(
          fields.toArray(new BCField[fields.size()]),
          methods.toArray(new BCMethod[methods.size()]),
          invariants.toArray(new BMLInvariant[invariants.size()]),
          constraints.toArray(new BMLConstraint[constraints.size()]));
    }
  }

  private  final class BCMethodBuilder implements MethodVisitor {

    private final BCMethod method;

    private AssertAttribute assertAttr;

    private AssumeAttribute assumeAttr;

    private LoopSpecificationAttribute loopSpecAttr;

    private InstructionHandle currentLabelHandle;

    private Instructions instructions;

    private int maxStack;

    private int maxLocals;

    private List<ExceptionHandler> exceptionHandlers;

    private int currentSourceLine = -1;

    public BCMethodBuilder(BCMethod method) {
      this.method = method;
    }

    private void reportTrouble(
        TroubleDescription description,
        Object... parameters) {
      troubleReporter.reportTrouble(new TroubleMessage(
          new TroublePosition(method, null), description, parameters));
    }

    public AnnotationVisitor visitAnnotationDefault() {
      // do nothing
      return null;
    }

    public AnnotationVisitor visitAnnotation(
        String descriptor,
        boolean visible) {
      // do nothing
      return null;
    }

    public AnnotationVisitor visitParameterAnnotation(
        int parameter,
        String descriptor,
        boolean visible) {
      // do nothing
      return null;
    }

    public void visitAttribute(Attribute attribute) {
      if (attribute instanceof MethodSpecificationAttribute) {
        MethodSpecificationAttribute specAttr = (MethodSpecificationAttribute) attribute;
        method.setSpecification(specAttr.getSpecification());
      } else if (attribute instanceof LoopSpecificationAttribute) {
        loopSpecAttr = (LoopSpecificationAttribute) attribute;
      } else if (attribute instanceof AssertAttribute) {
        assertAttr = (AssertAttribute) attribute;
      } else if (attribute instanceof AssumeAttribute) {
        assumeAttr = (AssumeAttribute) attribute;
      }
    }

    public void visitCode() {
      currentLabelHandle = null;
      instructions = new Instructions();
      maxStack = -1;
      maxLocals = -1;
      exceptionHandlers = new ArrayList<ExceptionHandler>();
    }

    public void visitFrame(
        int type,
        int nLocal,
        Object[] local,
        int nStack,
        Object[] stack) {
      // do nothing
    }

    public void visitInsn(int opcode) {
      try {
        addInstruction(InstructionFactory.fromInsn(opcode));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitIntInsn(int opcode, int operand) {
      try {
        addInstruction(InstructionFactory.fromIntInsn(opcode, operand));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitVarInsn(int opcode, int var) {
      try {
        addInstruction(InstructionFactory.fromVarInsn(opcode, var));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitTypeInsn(int opcode, String descriptor) {
      // The descriptor parameter either contains the internal name of a class
      // type, or else, the descriptor of an array type.
      JReferenceType type;
      if (descriptor.startsWith("[")) {
        type = (JArrayType) JType.fromDescriptor(TypeLoader.this, descriptor);
      } else {
        type = getClassType(descriptor);
      }

      try {
        addInstruction(InstructionFactory.fromTypeInsn(opcode, type));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitFieldInsn(
        int opcode,
        String owner,
        String name,
        String descriptor) {
      try {
        addInstruction(InstructionFactory.fromFieldInsn(
            opcode,
            getClassType(owner),
            name,
            JType.fromDescriptor(TypeLoader.this, descriptor)));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitMethodInsn(
        int opcode,
        String owner,
        String name,
        String descriptor) {
        
      // Methods can also be invoked on arrays. If so, the owner provided by
      // ASM is the array's descriptor.
      JReferenceType ownerType;
      if (owner.startsWith("[")) {
        ownerType = (JArrayType) JType.fromDescriptor(TypeLoader.this, owner);
      } else {
        ownerType = getClassType(owner);
      }
      JType returnType = JType.returnType(TypeLoader.this, descriptor);
      JType[] parameterTypes = JType.argumentTypes(TypeLoader.this, descriptor);

      try {
        addInstruction(InstructionFactory.fromMethodInsn(
            opcode,
            ownerType,
            name,
            returnType,
            parameterTypes));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitJumpInsn(int opcode, Label label) {
      try {
        addInstruction(InstructionFactory.fromJumpInsn(
            opcode,
            getHandleFor(label)));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[opcode]);
      }
    }

    public void visitLabel(Label label) {
      currentLabelHandle = getHandleFor(label);
    }

    public void visitLdcInsn(Object cst) {
      try {
        addInstruction(instructionFactory.fromLdcInsn(cst));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[IOpCodes.LDC]);
      }
    }

    public void visitIincInsn(int var, int increment) {
      try {
        addInstruction(InstructionFactory.fromIincInsn(var, increment));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION, IOpCodes.NAMES[IOpCodes.IINC]);
      }
    }

    public void visitTableSwitchInsn(
        int min,
        int max,
        Label dflt,
        Label[] labels) {
      InstructionHandle[] handles = new InstructionHandle[labels.length];
      for (int i = 0; i < handles.length; i++) {
        handles[i] = getHandleFor(labels[i]);
      }

      try {
        addInstruction(InstructionFactory.fromTableSwitchInsn(
            min,
            max,
            getHandleFor(dflt),
            handles));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION,
            IOpCodes.NAMES[IOpCodes.TABLESWITCH]);
      }
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      InstructionHandle[] handles = new InstructionHandle[labels.length];
      for (int i = 0; i < handles.length; i++) {
        handles[i] = getHandleFor(labels[i]);
      }

      try {
        addInstruction(InstructionFactory.fromLookupSwitchInsn(
            getHandleFor(dflt),
            keys,
            handles));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION,
            IOpCodes.NAMES[IOpCodes.LOOKUPSWITCH]);
      }
    }

    public void visitMultiANewArrayInsn(String descriptor, int dims) {
      try {
        addInstruction(InstructionFactory.fromMultiANewArrayInsn(
            (JArrayType) JType.fromDescriptor(TypeLoader.this, descriptor),
            dims));
      } catch (UnsupportedInstructionException e) {
        reportTrouble(
            B2BPLMessages.UNSUPPORTED_INSTRUCTION,
            IOpCodes.NAMES[IOpCodes.MULTIANEWARRAY]);
      }
    }

    public void visitTryCatchBlock(
        Label start,
        Label end,
        Label handler,
        String type) {
      // Setting type to null is used to implement finally-blocks which are
      // modeled as exception handlers which catch all exceptions. In order to
      // avoid handling null values later on, we set the exception to Throwable
      // which correctly reflects the above mentioned semantics.
      if (type == null) {
        type = "java/lang/Throwable";
      }

      ExceptionHandler exceptionHandler = new ExceptionHandler(
          getHandleFor(start),
          getHandleFor(end),
          getHandleFor(handler),
          getClassType(type));

      exceptionHandlers.add(exceptionHandler);
    }

    public void visitLocalVariable(
        String name,
        String descriptor,
        String signature,
        Label start,
        Label end,
        int index) {
    	LocalVariableInfo localVar = new LocalVariableInfo(index, name);
        // set the name of the local variable for all LocalVariableInstructions in the segment between start and end label
        InstructionHandle startHandle = getHandleFor(start);
        InstructionHandle endHandle = getHandleFor(end);
        InstructionHandle insnHandle;        
        if(startHandle.getPrevious() != null){
            insnHandle = startHandle.getPrevious();
        } else {
            insnHandle = startHandle;
        }
        Instruction insn;
        while(insnHandle != null && insnHandle != endHandle){
        	insnHandle.addActiveLocalVar(localVar);
            insn = insnHandle.getInstruction();
            if(insn instanceof LocalVariableInstruction){
                LocalVariableInstruction lvInsn = (LocalVariableInstruction)insn;
                if(lvInsn.getIndex() == index){
                    lvInsn.setVariableName(name);
                }
            }
            insnHandle = insnHandle.getNext();
        }
        
//        if(startHandle.getPrevious() == null){ //TODO this is no indication of the original parameters
            int parameterCount = method.getParameterNames().length;
            if(index < parameterCount){
                method.getParameterNames()[index] = name;
            }
//        }
    }

    public void visitLineNumber(int line, Label start) {
        currentSourceLine  = line;
    }

    public void visitMaxs(int maxStack, int maxLocals) {
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
    }

    public void visitEnd() {
      if (assertAttr != null) {
        Label[] labels = assertAttr.getLabels();
        BMLAssertStatement[] assertions = assertAttr.getAssertions();
        for (int i = 0; i < labels.length; i++) {
          InstructionHandle handle = getHandleFor(labels[i]);
          handle.addAssertion(assertions[i]);
        }
        assertAttr = null;
      }

      if (assumeAttr != null) {
        Label[] labels = assumeAttr.getLabels();
        BMLAssumeStatement[] assumptions = assumeAttr.getAssumptions();
        for (int i = 0; i < labels.length; i++) {
          InstructionHandle handle = getHandleFor(labels[i]);
          handle.addAssumption(assumptions[i]);
        }
        assumeAttr = null;
      }

      if (loopSpecAttr != null) {
        Label[] labels = loopSpecAttr.getLabels();
        BMLLoopSpecification[] specs = loopSpecAttr.getLoopSpecifications();
        for (int i = 0; i < labels.length; i++) {
          InstructionHandle handle = getHandleFor(labels[i]);
          handle.addLoopSpecification(specs[i]);
        }
        loopSpecAttr = null;
      }

      if (instructions != null) {
        method.setCodeInfo(
            instructions,
            maxStack,
            maxLocals,
            exceptionHandlers.toArray(
                new ExceptionHandler[exceptionHandlers.size()]));
        instructions = null;
      }
    }

    private void addInstruction(Instruction instruction) {
      InstructionHandle handle = currentLabelHandle;
      if (handle == null) {
        handle = new InstructionHandle();
      }
      handle.setInstruction(instruction);
      handle.setSourceLine(currentSourceLine);
      
      instructions.add(handle);
      currentLabelHandle = null;
    }

    private InstructionHandle getHandleFor(Label label) {
      InstructionHandle handle = (InstructionHandle) label.info;
      if (handle == null) {
        handle = new InstructionHandle();
        label.info = handle;
      }
      return handle;
    }
  }
}
