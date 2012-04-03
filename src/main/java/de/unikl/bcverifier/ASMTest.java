package de.unikl.bcverifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMTest {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        
        File libraryDir = new File("libraries/cell");
        File oldLibraryDir = new File(libraryDir, "old");
        assert oldLibraryDir.exists() && oldLibraryDir.isDirectory();
        File newLibraryDir = new File(libraryDir, "new");
        assert newLibraryDir.exists() && newLibraryDir.isDirectory();
        
        Collection<File> oldJavaFiles = FileUtils.listFiles(oldLibraryDir, new String[]{"class"}, true);
        
        for(File file : oldJavaFiles){
            ClassReader creader = new ClassReader(new FileInputStream(file));
            ClassNode cn = new ClassNode();
            creader.accept(cn, 0);
            System.out.println("Messages for class "+cn.name);
            for(Object o : cn.methods){
                MethodNode method = (MethodNode)o;
                System.out.println(method.name);
                
                System.out.println("Instructions:");
                InsnList instructions = method.instructions;
                ListIterator iterator = instructions.iterator();
                while(iterator.hasNext()){
                    Object insn = iterator.next();
                    if(insn instanceof MethodInsnNode){
                        MethodInsnNode methodCall = (MethodInsnNode)insn;
                        System.out.println("Method-call: "+methodCall.name);
                    } else if(insn instanceof FieldInsnNode){
                        FieldInsnNode fieldAccess = (FieldInsnNode)insn;
                        System.out.println("Field-access: "+fieldAccess.name);
                    }
                }
            }
        }
    }
}
