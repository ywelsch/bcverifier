package de.unikl.bcverifier.web;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class AcePanel extends Panel {
	final Component ta;
	final Component ace;
	public AcePanel(final String id, final String connect, IModel<String> model) {
		super(id, model);
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
				response.renderOnDomReadyJavaScript(connect + "(\"" + ace.getMarkupId() + "\",\"" + ta.getMarkupId() + "\");");
			}
		});
	}

	public String getAceId() {
		return ace.getMarkupId();
	}

}
