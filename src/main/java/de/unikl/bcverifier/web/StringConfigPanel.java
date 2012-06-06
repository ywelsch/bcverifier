package de.unikl.bcverifier.web;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class StringConfigPanel extends Panel {
	public StringConfigPanel(String id, IModel<String> model) {
		super(id, model);
		TextField<String> tf = new TextField<String>("stringtextfield", model);
		tf.setType(String.class);
		add(tf);
	}
}
