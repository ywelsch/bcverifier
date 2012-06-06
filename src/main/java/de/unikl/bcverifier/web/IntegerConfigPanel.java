package de.unikl.bcverifier.web;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class IntegerConfigPanel extends Panel {
	public IntegerConfigPanel(String id, IModel<Integer> model) {
		super(id, model);
		TextField<Integer> tf = new TextField<Integer>("inttextfield", model);
		tf.setType(Integer.class);
		add(tf);
	}
}
