package de.unikl.bcverifier.web;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

public class AceTextArea<T> extends TextArea<T> {

	public AceTextArea(final String id, final String tableId, final String connect, IModel model) {
		super(id, model);
		setOutputMarkupId(true);
		add(new Behavior() {
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.renderOnDomReadyJavaScript(connect + "(\"" + id + "\",\"" + tableId + "\");");
			}
		});
	}
}
