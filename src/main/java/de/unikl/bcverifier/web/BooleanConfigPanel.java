package de.unikl.bcverifier.web;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class BooleanConfigPanel extends Panel {

	public BooleanConfigPanel(String id, IModel<Boolean> model) {
		super(id, model);
		CheckBox cb = new CheckBox("boolcheckbox", model);
		add(cb);
	}

}
