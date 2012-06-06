package de.unikl.bcverifier.web;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class EnumConfigPanel extends Panel {
	public EnumConfigPanel(String id, IModel<String> model, List<String> choices) {
		super(id, model);
		DropDownChoice<String> choice = new DropDownChoice<String>("enumdropdown", model, choices);
		add(choice);
	}
}
