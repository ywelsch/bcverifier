package de.unikl.bcverifier.data;

import java.util.ArrayList;
import java.util.LinkedList;

import org.objectweb.asm.Type;

public class JavaStack {
    private LinkedList<StackContent> stack = new LinkedList<StackContent>();
    
    public enum Type{
        INT, FLOAT, DOUBLE, LONG, REF;

        public static Type fromASM(org.objectweb.asm.Type type) {
            switch(type.getSort()){
            case org.objectweb.asm.Type.BOOLEAN:
                return INT; //TODO
            case org.objectweb.asm.Type.BYTE:
                return INT; //TODO
            case org.objectweb.asm.Type.CHAR:
                return INT;
            case org.objectweb.asm.Type.DOUBLE:
                return DOUBLE;
            case org.objectweb.asm.Type.FLOAT:
                return FLOAT;
            case org.objectweb.asm.Type.INT:
                return INT;
            case org.objectweb.asm.Type.LONG:
                return LONG;
            case org.objectweb.asm.Type.SHORT:
                return INT; //TODO
            case org.objectweb.asm.Type.OBJECT:
                return REF;
            default:
                throw new RuntimeException("Type not supported: "+type.getDescriptor());    
            }
        }
        
        public static String getRefType(org.objectweb.asm.Type type){
            if(type.getSort() == org.objectweb.asm.Type.OBJECT){
                return type.getClassName();
            } else {
                return null;
            }
        }
    }
    
    public interface StackContent{
        Type getType();
        String getRefType();
        boolean isConstant();
        String getRepresentation();
        boolean isMethodCall();
    }
    
    public static abstract class Constant implements StackContent {
        public boolean isConstant(){
            return true;
        }

        public boolean isMethodCall() {
            return false;
        }
    }
    
    public static class IntConstant extends Constant {
        private int value;
        
        public IntConstant(int value){
            this.value = value;
        }
        
        public int getValue(){
            return this.value;
        }

        public Type getType() {
            return Type.INT;
        }

        public String getRefType() {
            return null;
        }

        public String getRepresentation() {
            return Integer.toString(value);
        }
    }
    
    public static class FloatConstant extends Constant {
        private float value;
        
        public FloatConstant(float value){
            this.value = value;
        }
        
        public float getValue(){
            return this.value;
        }

        public Type getType() {
            return Type.FLOAT;
        }

        public String getRefType() {
            return null;
        }

        public String getRepresentation() {
            return Float.toString(value);
        }
    }
    
    public static class LongConstant extends Constant {
        private long value;
        
        public LongConstant(long value){
            this.value = value;
        }
        
        public long getValue(){
            return this.value;
        }

        public Type getType() {
            return Type.LONG;
        }

        public String getRefType() {
            return null;
        }

        public String getRepresentation() {
            return Long.toString(value);
        }
    }
    
    public static class DoubleConstant extends Constant {
        private double value;
        
        public DoubleConstant(double value){
            this.value = value;
        }
        
        public double getValue(){
            return this.value;
        }

        public Type getType() {
            return Type.DOUBLE;
        }

        public String getRefType() {
            return null;
        }

        public String getRepresentation() {
            return Double.toString(value);
        }
    }
    
    public static abstract class Variable implements StackContent{
        public abstract String getName();
        public abstract boolean isLocal();
        
        public boolean isConstant(){
            return false;
        }

        public boolean isMethodCall() {
            return false;
        }
    }
    
    public static class This extends Variable{
        private String refType;
        
        public This(String refType){
            this.refType = refType;
        }
        
        public String getName() {
            return "this";
        }

        public boolean isLocal() {
            return false;
        }

        public Type getType() {
            return Type.REF;
        }

        public String getRefType() {
            return this.refType;
        }

        public String getRepresentation() {
            return "this";
        }
        
    }
    
    public static class LocalVariable extends Variable{
        private String name;
        private Type type;
        private String refType;
        
        public LocalVariable(String name, Type type){
            this(name, type, null);
        }
        
        public LocalVariable(String name, Type type, String refType){
            this.name = name;
            this.type = type;
            this.refType = refType;
        }
        
        public String getName() {
            return this.name;
        }

        public boolean isLocal() {
            return true;
        }

        public Type getType() {
            return this.type;
        }
        
        public String getRefType(){
            return this.refType;
        }

        public String getRepresentation() {
            return name;
        }
    }
    
    public static abstract class Field extends Variable {
        private String owner;
        private String name;
        private Type type;
        private String refType;

        public Field(String owner, String name, Type type, String refType){
            this.owner = owner;
            this.name = name;
            this.type = type;
            this.refType = refType;
        }
        
        public String getName() {
            return this.name;
        }

        public Type getType() {
            return this.type;
        }

        public String getRefType() {
            return this.refType;
        }
        
        public String getOwner(){
            return this.owner;
        }
        
        public boolean isLocal(){
            return false;
        }
        
        public abstract boolean isStatic();
        
        public String getRepresentation(){
            return owner.replaceAll("/", ".") + "." + name;
        }
    }
    
    public static class NormalField extends Field {
        public NormalField(String owner, String name, Type type){
            super(owner, name, type, null);
        }
        
        public NormalField(String owner, String name, Type type, String refType){
            super(owner, name, type, refType);
        }

        @Override
        public boolean isStatic() {
            return false;
        }
    }
    
    public static class StaticField extends Field {
        public StaticField(String owner, String name, Type type){
            super(owner, name, type, null);
        }
        
        public StaticField(String owner, String name, Type type, String refType){
            super(owner, name, type, refType);
        }

        @Override
        public boolean isStatic() {
            return true;
        }
    }
    
    public static class MethodCall implements StackContent{
        private StackContent[] parameters;
        private String owner;
        private String name;
        private de.unikl.bcverifier.data.JavaStack.Type type;
        private String refType;
        
        public MethodCall(String owner, String name, Type type, String refType, StackContent[] parameters){
            this.owner = owner;
            this.name = name;
            this.type = type;
            this.refType = refType;
            this.parameters = parameters;
        }
        
        public Type getType() {
            return this.type;
        }

        public String getRefType() {
            return this.refType;
        }

        public boolean isConstant() {
            return false;
        }

        public String getRepresentation() {
            return owner + "." + name + "(" + createParameterRepresentation() + ")";
        }
        
        private String createParameterRepresentation(){
            StringBuffer buffer = new StringBuffer();
            for(StackContent sc : parameters){
                buffer.append(sc.getRepresentation());
                buffer.append(", ");
            }
            buffer.delete(buffer.length()-3, buffer.length()-1);
            return buffer.toString();
        }

        public boolean isMethodCall() {
            return true;
        }
    }
    
    
    // Implementation of JavaStack

    public void push(StackContent v){
        if(v != null)
            stack.addFirst(v);
    }
    
    public StackContent pop(){
        return stack.removeFirst();
    }
    
    public StackContent[] pop(int n){
        LinkedList<StackContent> result = new LinkedList<StackContent>();
        for(int i = 0; i<n; i++){
            result.addFirst(pop());
        }
        return result.toArray(new StackContent[result.size()]);
    }
    
    public StackContent peek(){
        return stack.getFirst();
    }
    
    public void clear(){
        stack.clear();
    }
    
    public void swap(){
        StackContent v1 = pop();
        StackContent v2 = pop();
        push(v1);
        push(v2);
    }
}
