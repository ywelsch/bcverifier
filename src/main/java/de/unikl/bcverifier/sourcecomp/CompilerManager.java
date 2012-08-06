package de.unikl.bcverifier.sourcecomp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import de.unikl.bcverifier.Configuration;

public class CompilerManager {
	public static void main(String... args) throws IOException, InterruptedException {
		for (int i = 0; i < 500; i++) {
		Configuration c = new Configuration();
		c.setLibraries(new File("libraries/cell/old"), new File("libraries/cell/new"));
		new CompilerManager().run(c);
		}
	}

	public void run(Configuration c) throws IOException, InterruptedException {
		JavaCompiler compiler1 = ToolProvider.getSystemJavaCompiler();
		JavaCompiler compiler2 = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager1 = compiler1.getStandardFileManager(null, null, null);
		StandardJavaFileManager fileManager2 = compiler2.getStandardFileManager(null, null, null);
		List<String> compilationOptions = new ArrayList<String>();
		compilationOptions.add("-proc:only");

		Iterable<? extends JavaFileObject> compilationUnits1 =
				fileManager1.getJavaFileObjectsFromFiles(FileUtils.listFiles(c.library1(),
						new String[] { "java" }, true));
		final CompilationTask lib1task = compiler1.getTask(null, fileManager1, null, compilationOptions, null, compilationUnits1);

		Iterable<? extends JavaFileObject> compilationUnits2 =
				fileManager2.getJavaFileObjectsFromFiles(FileUtils.listFiles(c.library2(),
						new String[] { "java" }, true));
		final CompilationTask lib2task = compiler2.getTask(null, fileManager2, null, compilationOptions, null, compilationUnits2);

		ArrayList<Processor> processors = new ArrayList<Processor>();
		SynchronousQueue<MyProcessor1> meet = new SynchronousQueue<MyProcessor1>();
		MyProcessor1 proc1 = new MyProcessor1(meet);
		MyProcessor2 proc2 = new MyProcessor2(meet);
		processors.add(proc1);
		lib1task.setProcessors(processors);
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				lib1task.call();
			}
		});
		processors = new ArrayList<Processor>();
		processors.add(proc2);
		lib2task.setProcessors(processors);
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				lib2task.call();
			}
		});
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		fileManager1.close();
		fileManager2.close();
	}
	
	public void checks(RoundEnvironment roundEnv, ProcessingEnvironment processEnv, Trees trees) {
		for (Element e : roundEnv.getRootElements()) {
			//System.out.println("Element is " + e.getSimpleName());
			TreePath tp = trees.getPath(e);
			ClassVisitor visitor = new ClassVisitor(processEnv, roundEnv);
			visitor.scan(tp, trees);
			//processingEnv.getTypeUtils().asElement(e.asType());
			//processingEnv.getElementUtils().
		}
		TypeElement te = processEnv.getElementUtils().getTypeElement("cell.Cell");
		//ClassVisitor visitor = new ClassVisitor(processingEnv, env);
		//visitor.scan(trees.getPath(te), trees);
		//System.out.println(te);
		
	}
	
	@SupportedSourceVersion(SourceVersion.RELEASE_6)
	@SupportedAnnotationTypes("*")
	class MyProcessor1 extends AbstractProcessor {
		private volatile SynchronousQueue<MyProcessor1> meet;
		private volatile RoundEnvironment roundEnv;
		private volatile ProcessingEnvironment processEnv;
		private volatile Trees trees;
		public MyProcessor1(SynchronousQueue<MyProcessor1> meet) {
			this.meet = meet;
		}
		@Override
		public boolean process(Set<? extends TypeElement> annotations,
				RoundEnvironment roundEnv) {
			if (!roundEnv.getRootElements().isEmpty()) {
				this.roundEnv = roundEnv;
				this.processEnv = processingEnv;
				this.trees = Trees.instance(this.processingEnv);
				try {
					meet.put(this);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				meet = null;
			}
			return true;
		}
	}
	
	@SupportedSourceVersion(SourceVersion.RELEASE_6)
	@SupportedAnnotationTypes("*")
	class MyProcessor2 extends AbstractProcessor {
		private volatile SynchronousQueue<MyProcessor1> meet;
		public MyProcessor2(SynchronousQueue<MyProcessor1> meet) {
			this.meet = meet;
		}
		@Override
		public boolean process(Set<? extends TypeElement> annotations,
				RoundEnvironment roundEnv) {
			try {
				if (!roundEnv.getRootElements().isEmpty()) {
					MyProcessor1 myp = meet.take();
					Trees trees = Trees.instance(this.processingEnv);
										
					checks(myp.roundEnv, myp.processEnv, myp.trees);
					try {
						checks(roundEnv, this.processingEnv, trees);
					} catch (Throwable t) {}
					meet = null;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return true;
		}
	}
	
	class ClassVisitor extends TreePathScanner<Object, Trees> {
		final RoundEnvironment env;
		final ProcessingEnvironment processingEnv;
		public ClassVisitor(ProcessingEnvironment processingEnv, RoundEnvironment env) {
			this.env = env;
			this.processingEnv = processingEnv;
		}

		@Override
	    public Object visitClass(ClassTree classTree, Trees trees) {
			TreePath path = getCurrentPath();

	        //Get the type element corresponding to the class
	        TypeElement e = (TypeElement) trees.getElement(path);
	        System.out.println("Element is " + processingEnv.getTypeUtils().erasure(e.asType()));
	        Set<Modifier> mods = e.getModifiers();
	        if (mods.contains(Modifier.PUBLIC)) {
	        	//Set qualified class name into model
		        List<? extends Element> members = processingEnv.getElementUtils().getAllMembers(e);
		        for (Element member : members) {
		        	if (member instanceof ExecutableElement) {
		        		ExecutableElement emember = (ExecutableElement) member;
		        		TypeMirror tm = emember.getEnclosingElement().asType();
		        		if (!tm.toString().equals("java.lang.Object")) {
		        			System.out.println("Found method " + emember.getSimpleName());
		        		}
		        	}
		        }
	        }
	        return super.visitClass(classTree, trees);
	    }
	}

}
