package de.unikl.bcverifier.web;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.settings.def.JavaScriptLibrarySettings;

public class AcePanel extends Panel {
	final Component ta;
	final Component ace;
	public AcePanel(final String id, final String connect, IModel<String> model) {
		super(id, model);
		this.setOutputMarkupId(true);
		this.setOutputMarkupPlaceholderTag(true);
		ta = new TextArea<String>("textarea", model);
		ta.setOutputMarkupId(true);
		add(ta);
		ace = new Label("acetextarea") {
			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.put("class", id);
				super.onComponentTag(tag);
			}
		};
		ace.setOutputMarkupId(true);
		add(ace);
		
		add(new Behavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.render(JavaScriptHeaderItem.forUrl("ace/ace.js", "ace-main", false, "utf-8"));
				response.render(JavaScriptHeaderItem.forUrl("ace/theme-cobalt.js", "ace-cobalt-theme", false, "utf-8"));
				response.render(JavaScriptHeaderItem.forUrl("ace/mode-java.js", "ace-mode-java", false, "utf-8"));
				response.render(JavaScriptHeaderItem.forUrl("ace/mode-boogie.js", "ace-mode-boogie", false, "utf-8"));
				response.render(JavaScriptHeaderItem.forUrl("ace/mode-isl.js", "ace-mode-isl", false, "utf-8"));
				response.render(JavaScriptHeaderItem.forUrl("customaceheader.js", "customaceheader", false, "utf-8"));
			}
		});
		add(new Behavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				Class<?> c = JavaScriptLibrarySettings.class;
				response.render(JavaScriptHeaderItem.forReference(new JavaScriptLibrarySettings().getJQueryReference()));
				response.render(OnLoadHeaderItem.forScript(connect + "(\"" + ace.getMarkupId() + "\",\"" + ta.getMarkupId() + "\",\"" + AcePanel.this.getMarkupId() + "\");"));
			}
		});
	}

	public String getAceId() {
		return ace.getMarkupId();
	}

}
