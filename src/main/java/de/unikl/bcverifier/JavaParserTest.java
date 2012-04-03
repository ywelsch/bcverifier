package de.unikl.bcverifier;


import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

public class JavaParserTest {
    public static void main(String[] args) throws ParseException, IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        
        File libraryDir = new File("libraries/cell");
        File oldLibraryDir = new File(libraryDir, "old");
        assert oldLibraryDir.exists() && oldLibraryDir.isDirectory();
        File newLibraryDir = new File(libraryDir, "new");
        assert newLibraryDir.exists() && newLibraryDir.isDirectory();
        
        Collection<File> oldJavaFiles = FileUtils.listFiles(oldLibraryDir, new String[]{"java"}, true);
        
        System.out.println();
        System.out.println("Files of old library:");
        if(oldJavaFiles != null && oldJavaFiles.size() > 0){
            for( File file : oldJavaFiles){
                CompilationUnit cu = JavaParser.parse(file);
                System.out.println("Defined type in file:");
                System.out.println(cu);
                System.out.println("Messages: ");
                for(TypeDeclaration td : cu.getTypes()){
                    for(BodyDeclaration bd : td.getMembers()){
                        if(bd instanceof MethodDeclaration){
                            MethodDeclaration md = (MethodDeclaration)bd;
                            if(ModifierSet.isPublic(md.getModifiers())){
                                System.out.println(md.getName());
                            }
                        }
                    }
                }
            }           
        } else {
            System.out.println("No Java files found.");
        }
        
        
        Collection<File>  newJavaFiles = FileUtils.listFiles(newLibraryDir, new String[]{"java"}, true);
        System.out.println();
        System.out.println("File of new library:");
        if(newJavaFiles != null && newJavaFiles.size() > 0){
            for(File file : newJavaFiles){
                CompilationUnit cu = JavaParser.parse(file);
                System.out.println("Defined type in file:");
                System.out.println(cu);
                System.out.println("Messages: ");
                for(TypeDeclaration td : cu.getTypes()){
                    for(BodyDeclaration bd : td.getMembers()){
                        if(bd instanceof MethodDeclaration){
                            MethodDeclaration md = (MethodDeclaration)bd;
                            if(ModifierSet.isPublic(md.getModifiers())){
                                System.out.println(md.getName());
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No Java files found.");
        }
    }
}
