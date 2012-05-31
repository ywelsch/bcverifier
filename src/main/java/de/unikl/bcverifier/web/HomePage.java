package de.unikl.bcverifier.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.io.Files;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.LibraryCompiler;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;
	private String output;
	private String lib1;
	private String lib2;
	private String inv;

    public HomePage(final PageParameters parameters) {
		add(new Label("version", new Configuration().getVersionString()));
		add(new LibForm("libForm"));
		add(new Label("output", new PropertyModel(this, "output")));
    }
    
    public String getOutput() {
    	return output;
    }
    
    public void setOutput(String output) {
    	this.output = output;
    }
    
    public String getLib1() {
		return lib1;
	}

	public void setLib1(String lib1) {
		this.lib1 = lib1;
	}

	public String getLib2() {
		return lib2;
	}

	public void setLib2(String lib2) {
		this.lib2 = lib2;
	}

	public String getInv() {
		return inv;
	}

	public void setInv(String inv) {
		this.inv = inv;
	}

	public class LibForm extends Form {
		public LibForm(String id) {
			super(id);
			add(new TextArea<String>("lib1", new PropertyModel(HomePage.this, "lib1")).setType(String.class));
			add(new TextArea<String>("lib2", new PropertyModel(HomePage.this, "lib2")).setType(String.class));
			add(new TextArea<String>("inv", new PropertyModel(HomePage.this, "inv")).setType(String.class));
		}
		
		@Override
		protected void onSubmit() {
			try {
				compile();
				HomePage.this.setOutput(BoogieRunner.getLastMessage());
			} catch (IOException e) {
				HomePage.this.setOutput(e.getMessage());
				e.printStackTrace();
			} catch (TranslationException e) {
				HomePage.this.setOutput(e.getMessage());
				e.printStackTrace();
			} catch (CompileException e) {
				HomePage.this.setOutput(e.getMessage());
				e.printStackTrace();
			}
		}
		
		private void compile() throws IOException, TranslationException, CompileException {
			File dir = Files.createTempDir();
			File oldDir = new File(dir, "old");
			File oldFile = new File(oldDir, "C.java");
			FileUtils.writeStringToFile(oldFile, getLib1());
			File newDir = new File(dir, "new");
			File newFile = new File(newDir, "C.java");
			FileUtils.writeStringToFile(newFile, getLib2());
			File bplDir = new File(dir, "bpl");
			File invFile = new File(bplDir, "inv.bpl");
			FileUtils.writeStringToFile(invFile, getInv());
			Configuration config = new Configuration();
			config.setLibraries(oldDir, newDir);
			config.setInvariant(invFile);
			Library library = new Library(config);
			LibraryCompiler.compile(config.library1());
			LibraryCompiler.compile(config.library2());
			library.translate();
			library.check();
		}
    }
}
