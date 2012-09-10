package de.unikl.bcverifier.web;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class BooleanConfigPanel extends Panel {

	public BooleanConfigPanel(String id, String param) {
		super(id);
		add(new CheckBox("boolcheckbox", new BooleanOptionModel(param)));
	}
	
	private static final class BooleanOptionModel implements IModel<Boolean> {
		private final String param;

		private BooleanOptionModel(String param) {
			this.param = param;
		}

		public void detach() {}

		public Boolean getObject() {
			return (Boolean) ConfigSession.get().get(param);
		}

		public void setObject(Boolean object) {
			ConfigSession.get().put(param, object);
		}
	}
}