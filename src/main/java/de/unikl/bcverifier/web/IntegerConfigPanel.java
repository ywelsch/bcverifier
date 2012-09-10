package de.unikl.bcverifier.web;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class IntegerConfigPanel extends Panel {
	public IntegerConfigPanel(String id, String param) {
		super(id);
		TextField<Integer> tf = new TextField<Integer>("inttextfield", new IntegerOptionModel(param));
		tf.setType(Integer.class);
		add(tf);
	}
	
	private static final class IntegerOptionModel implements IModel<Integer> {
		private final String param;

		private IntegerOptionModel(String param) {
			this.param = param;
		}

		public void detach() {}

		public Integer getObject() {
			return (Integer) ConfigSession.get().get(param);
		}

		public void setObject(Integer object) {
			ConfigSession.get().put(param, object);
		}
	}
}