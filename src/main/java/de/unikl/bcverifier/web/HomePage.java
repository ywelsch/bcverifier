package de.unikl.bcverifier.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
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
	private String inv = "true";
	private String boogieinput;
	private List<String> lib1contents = new ArrayList<String>();
	private List<String> lib2contents = new ArrayList<String>();
	private List<Example> examples = new ArrayList<Example>();
	private Model<Example> selectedExample;
	
	public static class Example implements Serializable {
		private String id;
		private List<String> lib1files;
		private List<String> lib2files;
		private String invariant;
		
		@Override
		public String toString() {
			return getId();
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public List<String> getLib1files() {
			return lib1files;
		}
		public void setLib1files(List<String> lib1files) {
			this.lib1files = lib1files;
		}
		public List<String> getLib2files() {
			return lib2files;
		}
		public void setLib2files(List<String> lib2files) {
			this.lib2files = lib2files;
		}
		public String getInvariant() {
			return invariant;
		}
		public void setInvariant(String invariant) {
			this.invariant = invariant;
		}
	}
	
	public static Pattern BPL_FILE_DEBUG_PATTERN = Pattern.compile(/*"\\s*" +*/ "(\\w+\\.bpl)\\((\\d+)\\,(\\d+)\\)\\:" /*+ ".*"*/);
	public static Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("package\\s+(\\w+(\\s*\\.\\s*\\w+)*)\\s*\\;");
	public static Pattern FILE_NAME_PATTERN = Pattern.compile("(class|interface)\\s+(\\w+)\\W");
	
	final AcePanel bipanel = new AcePanel("boogieinput", "connectBoogieInput", new PropertyModel<String>(HomePage.this, "boogieinput"));
	final LibForm form = new LibForm("libForm");
	
    public HomePage(final PageParameters parameters) {
		add(new Label("version", new Configuration().getVersionString()));
		add(form);
		createDropDownSelector(form.pan1, form.pan2, form.pan3);
		add(new MultiLineLabel("output", new PropertyModel(this, "output")).setEscapeModelStrings(false));
		add(bipanel);
		lib1contents.add("public class C {\n  public int m() {\n    return 0;\n  }\n}");
		lib1contents.add("public interface I {}");
		lib2contents.add("public class C {\n  public int m() {\n    return 2 - 1;\n  }\n}");
		populateExamples();
    }
    
    private void createDropDownSelector(final MarkupContainer pan1, final MarkupContainer pan2, final AcePanel pan3) {
		selectedExample = new Model<Example>();
		DropDownChoice choice = new DropDownChoice("examples", selectedExample, examples);
		choice.setOutputMarkupId(true);
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                Example ex = selectedExample.getObject();
                if (ex != null) {
                	lib1contents.clear();
                	lib1contents.addAll(ex.getLib1files());
                	lib2contents.clear();
                	lib2contents.addAll(ex.getLib2files());
                	setInv(ex.getInvariant());
                	if (target != null) {
                		target.add(pan1, pan2, pan3);
                	}
                }
            }
        });
		add(choice);
	}
    
    private void populateExamples() {
    	addExample("cell", "Cell example");
    	addExample("cell2", "Cell example (Alternative)");
    	addExample("cb", "Callback example");
    	addExample("obool", "OBool example");
	}

	private void addExample(String dir, String description) {
		ClassLoader loader = HomePage.class.getClassLoader();
		File topDir;
		try {
			topDir = new File(loader.getResource(dir).toURI());
			File oldDir = new File(topDir, "old");
			File newDir = new File(topDir, "new");
			File invFile = new File(topDir, "bpl/inv.bpl");
	        Collection<File> oldJavaFiles = FileUtils.listFiles(oldDir, new String[] { "java" }, true);
	        Collection<File> newJavaFiles = FileUtils.listFiles(newDir, new String[] { "java" }, true);
			Example ex = new Example();
			ex.setId(description);
			ex.setInvariant(FileUtils.readFileToString(invFile));
			List<String> lib1files = new ArrayList<String>();
			for (File f : oldJavaFiles) {
				lib1files.add(FileUtils.readFileToString(f));
			}
			ex.setLib1files(lib1files);
			List<String> lib2files = new ArrayList<String>();
			for (File f : newJavaFiles) {
				lib2files.add(FileUtils.readFileToString(f));
			}
			ex.setLib2files(lib2files);
			examples.add(ex);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getOutput() {
    	return output;
    }
    
    public void setOutput(String output) {
    	this.output = output;
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
		final MarkupContainer pan1;
		final MarkupContainer pan2;
		final AcePanel pan3;
		
		public LibForm(String id) {
			super(id);
			pan1 = createLibPanel("lib1panel", "lib1contents", "add1Row", "remove1Row", lib1contents);
			pan2 = createLibPanel("lib2panel", "lib2contents", "add2Row", "remove2Row", lib2contents);
			pan3 = createInvPanel();
		}

		private AcePanel createInvPanel() {
			AcePanel pan = new AcePanel("inv",  "connectInv", new PropertyModel<String>(HomePage.this, "inv"));
			add(pan);
			return pan;
		}

		private MarkupContainer createLibPanel(String panel, String contentsId, String addRow, String removeRow, final List<String> contents) {
			final MarkupContainer libpanel = new WebMarkupContainer(panel);
			libpanel.setOutputMarkupId(true);
			add(libpanel);
			final ListView<String> liblv = new ListView<String>(contentsId, contents) {
				@Override
				protected void populateItem(ListItem<String> item) {
					//createAcePanel("lib1", "connectLib", 1);
					int index = item.getIndex();
					item.add(new AcePanel("libclass", "connectLib", item.getModel()));
				}
			};
			//liblv.setReuseItems(true);
			libpanel.add(liblv);
			AjaxSubmitLink addLink = new AjaxSubmitLink(addRow, LibForm.this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					liblv.getModelObject().add(new String("class X {}"));
					if (target != null)
						target.add(libpanel);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
				}
			};
			addLink.setDefaultFormProcessing(false);
			libpanel.add(addLink);
			AjaxSubmitLink removeLink = new AjaxSubmitLink(removeRow, LibForm.this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					if (contents.size() > 1) {
						contents.remove(contents.size() - 1);
						//liblv.detach();
						if (target != null)
							target.add(libpanel);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
				}
			};
			removeLink.setDefaultFormProcessing(false);
			libpanel.add(removeLink);
			return libpanel;
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
			Matcher m = BPL_FILE_DEBUG_PATTERN.matcher(result.toString());
    		return m.replaceAll("<a href=\"#boogieinputbegin\" onclick=\"acegoto('" +  bipanel.getAceId() + "',$2,$3);\">$1($2,$3):</a>");
		}

		private void compile() throws IOException, TranslationException, CompileException {
			File dir = Files.createTempDir();
			System.out.println("Creating test in " + dir);
			File oldDir = new File(dir, "old");
			File newDir = new File(dir, "new");
			createFiles(oldDir, lib1contents);
			createFiles(newDir, lib2contents);
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

		private void createFiles(final File prefix, final List<String> libcontents) throws IOException {
			for (String s : libcontents) {
				File dir = prefix;
				Matcher dirmatcher = DIRECTORY_NAME_PATTERN.matcher(s);
				Matcher filematcher = FILE_NAME_PATTERN.matcher(s);
				if (dirmatcher.find()) {
					String separator = System.getProperty("file.separator");
					String path = dirmatcher.group(1);
					path = path.replaceAll("\\s", "");
					path = path.replaceAll("\\.", separator);
					dir = new File(prefix, path);
				}
				if (filematcher.find()) {
					String filename = filematcher.group(2) + ".java";
					File file = new File(dir, filename);
					FileUtils.writeStringToFile(file, s);
				} else {
					throw new IllegalArgumentException("Can not find a meaningful class or interface definition");
				}
			}
		}
    }
}