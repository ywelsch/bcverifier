package de.unikl.bcverifier.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

import com.beust.jcommander.ParameterDescription;
import com.google.common.io.Files;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.helpers.VerificationResult;
import de.unikl.bcverifier.sourcecomp.SourceInCompatibilityException;
import de.unikl.bcverifier.specification.GenerationException;
import de.unikl.bcverifier.web.backend.Example;
import de.unikl.bcverifier.web.backend.ExampleLoader;
import de.unikl.bcverifier.web.backend.FileNameExtractor;

public class HomePage extends WebPage {
	private String output;
	private String inv = "";
	private String boogieinput;
	private List<String> lib1contents = new ArrayList<String>();
	private List<String> lib2contents = new ArrayList<String>();
	
	public static Pattern BPL_FILE_DEBUG_PATTERN = Pattern.compile(/*"\\s*" +*/ ".*\\.bpl\\((\\d+)\\,(\\d+)\\)\\:" /*+ ".*"*/);
	
	final AcePanel bipanel = new AcePanel("boogieinput", "connectBoogieInput", new PropertyModel<String>(HomePage.this, "boogieinput"));
	final LibForm form = new LibForm("libForm");
	final MarkupContainer outputpanel = new WebMarkupContainer("outputpanel");
	final MultiLineLabel olabel = new MultiLineLabel("output", new PropertyModel(this, "output"));
	
    public HomePage(final PageParameters parameters) {
		add(new Label("version", ConfigSession.get().getConfig().getVersionString()));
		add(form);
		createDropDownSelector(form.pan1, form.pan2, form.pan3);
		createOutputPanel();
    }
    
    private void createOutputPanel() {
		outputpanel.setOutputMarkupId(true);
		outputpanel.setOutputMarkupPlaceholderTag(true);
		outputpanel.add(olabel);
		olabel.setEscapeModelStrings(false);
		olabel.setOutputMarkupId(true);
		outputpanel.add(bipanel);
		outputpanel.setVisible(false);
		bipanel.setVisible(false);
		add(outputpanel);
	}

	private void createDropDownSelector(final MarkupContainer pan1, final MarkupContainer pan2, final MarkupContainer pan3) {
    	final Model<Example> selectedExample = new Model<Example>();
    	List<Example> examples = new ExampleLoader().loadExamples();
		DropDownChoice<Example> choice = new DropDownChoice<Example>("examples", selectedExample, examples);
		choice.setOutputMarkupId(true);
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
                Example ex = selectedExample.getObject();
                if (ex != null) {
                	selectExample(ex);
                	if (target != null) {
                		target.add(pan1, pan2, pan3, outputpanel);
                	}
                }
            }
        });
		selectExample(examples.get(0));
		add(choice);
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
		if (boogieinput.equals("")) {
			bipanel.setVisible(false);
		} else {
			bipanel.setVisible(true);
		}
		this.boogieinput = boogieinput;
	}

	private void selectExample(Example ex) {
		lib1contents.clear();
		lib1contents.addAll(ex.getLib1files());
		lib2contents.clear();
		lib2contents.addAll(ex.getLib2files());
		setInv(ex.getInvariant());
		setOutput("");
		setBoogieinput("");
		outputpanel.setVisible(false);
	}

	public class LibForm extends Form<Object> {
		private final class IndicatingAjaxButtonExtension extends
				AjaxButton implements IAjaxIndicatorAware {
			private IndicatingAjaxButtonExtension(String id, Form<?> form) {
				super(id, form);
			}

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				checkLibrary();
				if (target != null) {
					target.add(LibForm.this, outputpanel);
				}
			}

			@Override
			public String getAjaxIndicatorMarkupId() {
				return "ajax_indicator";
			}
			
			@Override
		    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		        super.updateAjaxAttributes(attributes);
		 
		        AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
		 
		            @Override
		            public CharSequence getPrecondition(Component component) {
		                return "document.getElementById(\"" + outputpanel.getMarkupId() + "\").style.display = \"none\"; " + super.getPrecondition(component);
		            }
		        };
		        attributes.getAjaxCallListeners().add(myAjaxCallListener);
		    }
		}

		final MarkupContainer pan1;
		final MarkupContainer pan2;
		final MarkupContainer pan3;
		final MarkupContainer opanel;
		
		public LibForm(String id) {
			super(id);
			this.setOutputMarkupId(true);
			add(new AttributeAppender("action", new Model<String>("#outputlink"), "")); 
			pan1 = createLibPanel("lib1panel", "lib1contents", "add1Row", "remove1Row", lib1contents);
			pan2 = createLibPanel("lib2panel", "lib2contents", "add2Row", "remove2Row", lib2contents);
			pan3 = createInvPanel();
			opanel = createOptionPanel();
			add(new IndicatingAjaxButtonExtension("checkButton", this));
		}

		private WebMarkupContainer createOptionPanel() {
			final WebMarkupContainer pan = new WebMarkupContainer("optionpanel");
			pan.setOutputMarkupId(true);
			add(pan);
			
			//parser.parseWithoutValidation("");
	        //System.out.println("INITIALIZED OPTIONPANEL" + parser.getParameters().size());
			final ListView<String> liblv = new ListView<String>("optioncontents", ConfigSession.get().getParams()) {
				@Override
				protected void populateItem(ListItem<String> item) {
					final String param = item.getModelObject();
					ParameterDescription pd = ConfigSession.get().getParam(param);
					Class<?> paramType = pd.getField().getType();
					item.add(new Label("description", pd.getDescription()));
					if (paramType.equals(Boolean.TYPE)) {
		        		item.add(new BooleanConfigPanel("option", param));
		        	} else if (paramType.equals(Integer.TYPE)) {
		        		item.add(new IntegerConfigPanel("option", param));
		        	} else if (paramType.isEnum()) {
		        		item.add(new EnumConfigPanel("option", param, paramType));	
		        	} else {
		        		item.add(new StringConfigPanel("option", param));
		        	}
				}
			};
			//liblv.setReuseItems(true);
			pan.add(liblv);
			return pan;
		}

		private MarkupContainer createInvPanel() {
			final MarkupContainer invpanel = new WebMarkupContainer("invpanel");
			invpanel.setOutputMarkupId(true);
			add(invpanel);
			invpanel.add(new AcePanel("inv", "connectInv", new PropertyModel<String>(HomePage.this, "inv")));
			return invpanel;
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
			addLink.setOutputMarkupId(true);
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
			removeLink.setOutputMarkupId(true);
			removeLink.setDefaultFormProcessing(false);
			libpanel.add(removeLink);
			return libpanel;
		}
		
		private void checkLibrary() {
			HomePage.this.setOutput("");
			HomePage.this.setBoogieinput("");
			File dir = Files.createTempDir();
			try {
				//System.out.println("Creating test in " + dir);
				File oldDir = new File(dir, "old");
				File newDir = new File(dir, "new");
				FileNameExtractor.createFiles(oldDir, lib1contents);
				FileNameExtractor.createFiles(newDir, lib2contents);
				File bplDir = new File(dir, "bpl");
				File invFile = new File(bplDir, "spec.isl");
				FileUtils.writeStringToFile(invFile, getInv());
				File output = new File(bplDir, "output.bpl");
				Configuration config = ConfigSession.get().getConfig();
				config.setLibraries(oldDir, newDir);
				config.setSpecification(invFile);
				config.setOutput(output);
				config.setCompileFirst(true);
				Library library = new Library(config);
				VerificationResult verificationResult = library.runLifecycle();
				HomePage.this.setBoogieinput(FileUtils.readFileToString(output));
				HomePage.this.setOutput(linkify(verificationResult.getLastMessage()));
			} catch (IOException e) {
				HomePage.this.setOutput(filterPath(dir, e.getMessage()));
				e.printStackTrace();
			} catch (TranslationException e) {
				HomePage.this.setOutput(filterPath(dir, e.getMessage()));
				e.printStackTrace();
			} catch (CompileException e) {
				HomePage.this.setOutput(filterPath(dir, e.getMessage()));
				e.printStackTrace();
			} catch (GenerationException e) {
				HomePage.this.setOutput(filterPath(dir, e.getMessage()));
                e.printStackTrace();
            } catch (SourceInCompatibilityException e) {
            	HomePage.this.setOutput(filterPath(dir, e.getMessage()));
                e.printStackTrace();
			}
			outputpanel.setVisible(true);
		}
		
		private String filterPath(File dir, String message) {
			return message.replaceAll(Pattern.quote(dir.getAbsolutePath()), "");
		}

		private String linkify(String lastMessage) {
			StringBuilder result = new StringBuilder(Strings.escapeMarkup(lastMessage));
			Matcher m = BPL_FILE_DEBUG_PATTERN.matcher(result.toString());
    		return m.replaceAll("<a href=\"#boogieinputbegin\" onclick=\"acegoto('" +  bipanel.getAceId() + "',$1,$2);\">($1,$2):</a>");
		}
    }
}
