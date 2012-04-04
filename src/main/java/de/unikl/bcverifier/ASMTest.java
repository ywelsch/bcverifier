package de.unikl.bcverifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unikl.bcverifier.data.JavaStack;
import de.unikl.bcverifier.data.JavaStack.LocalVariable;
import de.unikl.bcverifier.data.JavaStack.StackContent;
import de.unikl.bcverifier.data.Library;

public class ASMTest {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        
        File libraryDir = new File("libraries/cell");
        File oldLibraryDir = new File(libraryDir, "old");
        assert oldLibraryDir.exists() && oldLibraryDir.isDirectory();
        File newLibraryDir = new File(libraryDir, "new");
        assert newLibraryDir.exists() && newLibraryDir.isDirectory();
        
        Library oldLibrary = new Library();
        oldLibrary.load(oldLibraryDir);
        
        Library newLibrary = new Library();
        newLibrary.load(newLibraryDir);
        
        System.out.println("Printing information about old library");
        for(ClassNode cn : oldLibrary.getAllClasses()){
//            printInformation(cn);
            printBoogie(cn, System.out);
        }
        
//        System.out.println();
//        System.out.println("Printing infromation about new library");
//        for(ClassNode cn : newLibrary.getAllClasses()){
//            printInformation(cn);
//        }
    }
    
    private static void printBoogie(ClassNode cn, PrintStream writer){
        System.out.println("class "+cn.name);
        for(Object o : cn.methods){
            MethodNode method = (MethodNode)o;
            printBoogie(method, writer);
        }
    }
    
    private static void printBoogie(MethodNode mn, PrintStream writer){
        writer.println("Method "+mn.name);
        Type[] parameters = Type.getArgumentTypes(mn.desc);
        
        JavaStack stack = new JavaStack();
        
        InsnList instructions = mn.instructions;
        ListIterator iterator = instructions.iterator();
        while(iterator.hasNext()){
            Object insn = iterator.next();
            AbstractInsnNode node = (AbstractInsnNode)insn;
            switch (node.getType()) {
            case AbstractInsnNode.VAR_INSN:
                VarInsnNode varInsn = (VarInsnNode)node;
                switch(varInsn.getOpcode()){
                case Opcodes.ALOAD:
                    if(varInsn.var == 0){
                        stack.push(new JavaStack.This(null)); //TODO what refType does "this" have in this context?
                    } else if(varInsn.var <= parameters.length){
                        stack.push(new JavaStack.LocalVariable("arg"+(varInsn.var - 1), JavaStack.Type.fromASM(parameters[varInsn.var-1]), JavaStack.Type.getRefType(parameters[varInsn.var-1])));
                    } else {
                        stack.push(new JavaStack.LocalVariable("$"+(varInsn.var - parameters.length), JavaStack.Type.REF, null)); //TODO what refType is this?
                    }
                    break;
                case Opcodes.ILOAD:
                    stack.push(new JavaStack.LocalVariable("$"+varInsn.var, JavaStack.Type.INT));
                    break;
                case Opcodes.LLOAD:
                    stack.push(new JavaStack.LocalVariable("$"+varInsn.var, JavaStack.Type.LONG));
                    break;
                case Opcodes.FLOAD:
                    stack.push(new JavaStack.LocalVariable("$"+varInsn.var, JavaStack.Type.FLOAT));
                    break;
                case Opcodes.DLOAD:
                    stack.push(new JavaStack.LocalVariable("$"+varInsn.var, JavaStack.Type.DOUBLE));
                    break;
                    
                case Opcodes.ASTORE:
                    if(varInsn.var <= parameters.length){
                        
                    } else {
                        writer.print("$"+(varInsn.var - parameters.length)+" = ");
                    }
                    
                    StackContent topOfStack = stack.pop();
                    writer.print(topOfStack.getRepresentation());
                    writer.println(";");
                    break;
                }
                break;
            case AbstractInsnNode.METHOD_INSN:
                MethodInsnNode methodCall = (MethodInsnNode)insn;
                Type[] callParameters = Type.getArgumentTypes(methodCall.desc);
                StackContent[] parameterValues = stack.pop(callParameters.length);
                //TODO check, that parameters are not method calls
                Type returnType = Type.getReturnType(methodCall.desc);
                boolean isVoidCall = returnType.getSort()==Type.VOID;
                
                StackContent methodCallContent;
                StackContent owner;
                switch(methodCall.getOpcode()){
                case Opcodes.INVOKEVIRTUAL:
                    owner = stack.pop();
                    //TODO check that callee is not a method call
                    methodCallContent = new JavaStack.MethodCall(owner.getRepresentation(), methodCall.name, JavaStack.Type.fromASM(returnType), JavaStack.Type.getRefType(returnType), parameterValues);
                    if(isVoidCall){
                        writer.println(methodCallContent.getRepresentation());
                    } else {
                        stack.push(methodCallContent);
                    }
                    break;
                case Opcodes.INVOKESPECIAL:
                    //TODO (private methods, super calls, ...)
                    break;
                case Opcodes.INVOKESTATIC:
                    methodCallContent = new JavaStack.MethodCall(methodCall.owner, methodCall.name, JavaStack.Type.fromASM(returnType), JavaStack.Type.getRefType(returnType), parameterValues);
                    if(isVoidCall){
                        writer.println(methodCallContent.getRepresentation());
                    } else {
                        stack.push(methodCallContent);
                    }
                    break;
                case Opcodes.INVOKEINTERFACE:
                    owner = stack.pop();
                    //TODO check that callee is not a method call
                    methodCallContent = new JavaStack.MethodCall(owner.getRepresentation(), methodCall.name, JavaStack.Type.fromASM(returnType), JavaStack.Type.getRefType(returnType), parameterValues);
                    if(isVoidCall){
                        writer.println(methodCallContent.getRepresentation());
                    } else {
                        stack.push(methodCallContent);
                    }
                    break;
                case Opcodes.INVOKEDYNAMIC:
                    throw new RuntimeException("INVOKEDYNAMIC not supported."); //TODO what is invokedynamic?
                }
                break;
            case AbstractInsnNode.FIELD_INSN:
                FieldInsnNode fieldAccess = (FieldInsnNode)insn;
                Type fieldType = Type.getType(fieldAccess.desc);
                StackContent fieldValue;
                switch(fieldAccess.getOpcode()){
                case Opcodes.GETFIELD:
                    owner = stack.pop();
                    //TODO check, that owner is not a method call
                    stack.push(new JavaStack.NormalField(owner.getRepresentation(), fieldAccess.name, JavaStack.Type.fromASM(fieldType), JavaStack.Type.getRefType(fieldType)));
                    break;
                case Opcodes.GETSTATIC:
                    stack.push(new JavaStack.NormalField(fieldAccess.owner, fieldAccess.name, JavaStack.Type.fromASM(fieldType), JavaStack.Type.getRefType(fieldType)));
                    break;
                case Opcodes.PUTFIELD:
                    fieldValue = stack.pop();
                    owner = stack.pop();
                    writer.println(owner.getRepresentation()+"."+fieldAccess.name+" = "+fieldValue.getRepresentation()+";");
                    break;
                case Opcodes.PUTSTATIC:
                    fieldValue = stack.pop();
                    writer.println(fieldAccess.owner+"."+fieldAccess.name+" = "+fieldValue.getRepresentation()+";");
                    break;
                default:
                    System.out.print("Unknown field-access: "+fieldAccess.owner+"."+fieldAccess.name);
                    break;
                }
                break;
            case AbstractInsnNode.JUMP_INSN:
                JumpInsnNode jump = (JumpInsnNode)insn;
                String label = jump.label.getLabel().toString();
                
                switch (jump.getOpcode()) {
                case Opcodes.IFEQ:
                    //TODO
                    writer.println("IFEQ "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFNE:
                    //TODO
                    writer.println("IFNE "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFLT:
                    //TODO
                    writer.println("IFLT "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFGE:
                    //TODO
                    writer.println("IFGE "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFGT:
                    //TODO
                    writer.println("IFGT "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFLE:
                    //TODO
                    writer.println("IFLE "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPEQ:
                    //TODO
                    writer.println("IF_ICMPEQ "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPNE:
                    //TODO
                    writer.println("IF_ICMPNE "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPLT:
                    //TODO
                    writer.println("IF_ICMPLT "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPGE:
                    //TODO
                    writer.println("IF_ICMPGE "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPGT:
                    //TODO
                    writer.println("IF_ICMPGT "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ICMPLE:
                    //TODO
                    writer.println("IF_ICMPLE "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ACMPEQ:
                    //TODO
                    writer.println("IF_ACMPEQ "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IF_ACMPNE:
                    //TODO
                    writer.println("IF_ACMPNE "+stack.pop().getRepresentation()+" "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFNULL:
                    //TODO
                    writer.println("IFNULL "+stack.pop().getRepresentation());
                    break;
                case Opcodes.IFNONNULL:
                    //TODO
                    writer.println("IFNONNULL "+stack.pop().getRepresentation());
                    break;
                case Opcodes.GOTO:
                    //TODO
                    writer.println("GOTO "+label);
                    break;
                    
                default:
                    writer.println("Unknown jump instruction: "+jump.getOpcode()+" to label "+label);
                    break;
                }
                break;
            default:
                // other instructions, that have no explicit representation as special node type
                switch(node.getOpcode()){
                case Opcodes.IRETURN:
                    writer.println("return "+stack.pop().getRepresentation()+";");
                    break;
                case Opcodes.LRETURN:
                    writer.println("return "+stack.pop().getRepresentation()+";");
                    break;
                case Opcodes.FRETURN:
                    writer.println("return "+stack.pop().getRepresentation()+";");
                    break;
                case Opcodes.DRETURN:
                    writer.println("return "+stack.pop().getRepresentation()+";");
                    break;
                case Opcodes.ARETURN:
                    writer.println("return "+stack.pop().getRepresentation()+";");
                    break;
                case Opcodes.RETURN:
                    writer.println("return;");
                    break;
                case Opcodes.ATHROW:
                    writer.println("throw "+stack.pop().getRepresentation()+";");
                    break;
                }
                break;
            }
        }
    }

    private static void printInformation(ClassNode cn) {
        System.out.println("Messages for class "+cn.name);
        for(Object o : cn.methods){
            MethodNode method = (MethodNode)o;
            int numberOfParameters = Type.getArgumentTypes(method.desc).length;
            System.out.println("Method: " + method.name);
            System.out.println("Number of parameters: " + numberOfParameters);
            System.out.println("Instructions:");
            InsnList instructions = method.instructions;
            ListIterator iterator = instructions.iterator();
            while(iterator.hasNext()){
                Object insn = iterator.next();
                AbstractInsnNode node = (AbstractInsnNode)insn;
                switch (node.getType()) {
                case AbstractInsnNode.VAR_INSN:
                    VarInsnNode varInsn = (VarInsnNode)node;
                    System.out.println("Access to variable "+varInsn.var);
                    break;
                case AbstractInsnNode.METHOD_INSN:
                    MethodInsnNode methodCall = (MethodInsnNode)insn;
                    System.out.println("Method-call: "+methodCall.owner+"."+methodCall.name);
                    break;
                case AbstractInsnNode.FIELD_INSN:
                    FieldInsnNode fieldAccess = (FieldInsnNode)insn;
                    switch(fieldAccess.getOpcode()){
                    case Opcodes.GETFIELD:
                        System.out.print("Field-access: ");
                        break;
                    case Opcodes.GETSTATIC:
                        System.out.print("Static field-access: ");
                        break;
                    case Opcodes.PUTFIELD:
                        System.out.print("Putfield: ");
                        break;
                    case Opcodes.PUTSTATIC:
                        System.out.print("Putstatic: ");
                        break;
                    default:
                        System.out.print("Unknown field-access: ");
                        break;
                    }
                    System.out.println(fieldAccess.owner+"."+fieldAccess.name);
                    break;
                default:
                    break;
                }
            }
            System.out.println();
        }
    }
}
