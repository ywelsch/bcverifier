package de.unikl.bcverifier.web;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class StringConfigPanel extends Panel {
	public StringConfigPanel(String id, String param) {
		super(id);
		TextField<String> tf = new TextField<String>("stringtextfield", new StringOptionModel(param));
		tf.setType(String.class);
		add(tf);
	}
	
	private static final class StringOptionModel implements IModel<String> {
		private final String param;

		private StringOptionModel(String param) {
			this.param = param;
		}

		public void detach() {}

		public String getObject() {
			return (String) ConfigSession.get().get(param);
		}

		public void setObject(String object) {
			ConfigSession.get().put(param, object);
		}
	}
}