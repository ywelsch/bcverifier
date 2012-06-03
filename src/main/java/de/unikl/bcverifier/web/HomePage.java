package de.unikl.bcverifier.web;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

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
	private String lib1 = "public class C {\n  public int m() {\n    return 0;\n  }\n}";
	private String lib2 = "public class C {\n  public int m() {\n    return 2 - 1;\n  }\n}";
	private String inv = "true";
	private String boogieinput;
	
	public static Pattern PATTERN = Pattern.compile(/*"\\s*" +*/ "(\\w*\\.bpl)\\((\\d*)\\,(\\d*)\\)\\:" /*+ ".*"*/);
	AcePanel bipanel = new AcePanel("boogieinput", "connectBoogieInput", new PropertyModel<String>(HomePage.this, "boogieinput"));

    public HomePage(final PageParameters parameters) {
		add(new Label("version", new Configuration().getVersionString()));
		add(new LibForm("libForm"));
		add(new MultiLineLabel("output", new PropertyModel(this, "output")).setEscapeModelStrings(false));
		add(bipanel);
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

	public String getBoogieinput() {
		return boogieinput;
	}

	public void setBoogieinput(String boogieinput) {
		this.boogieinput = boogieinput;
	}

	public class LibForm extends Form {
		public LibForm(String id) {
			super(id);
			add(new AcePanel("lib1", "connectLib", new PropertyModel(HomePage.this, "lib1")));
			add(new AcePanel("lib2", "connectLib", new PropertyModel(HomePage.this, "lib2")));
			add(new AcePanel("inv",  "connectInv", new PropertyModel(HomePage.this, "inv")));
		}
		
		@Override
		protected void onSubmit() {
			try {
				HomePage.this.setOutput("");
				HomePage.this.setBoogieinput("");
				compile();
				HomePage.this.setOutput(linkify(BoogieRunner.getLastMessage()));
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
		
		private String linkify(String lastMessage) {
			StringBuilder result = new StringBuilder(Strings.escapeMarkup(lastMessage));
			Matcher m = PATTERN.matcher(result.toString());
    		return m.replaceAll("<a href=\"#\" onclick=\"acegoto('" +  bipanel.getAceId() + "',$2,$3);\">$1($2,$3):</a>");
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
			File output = new File(bplDir, "output.bpl");
			Configuration config = new Configuration();
			config.setLibraries(oldDir, newDir);
			config.setInvariant(invFile);
			config.setOutput(output);
			Library library = new Library(config);
			LibraryCompiler.compile(config.library1());
			LibraryCompiler.compile(config.library2());
			library.translate();
			HomePage.this.setBoogieinput(FileUtils.readFileToString(output));
			library.check();
		}
    }
}
