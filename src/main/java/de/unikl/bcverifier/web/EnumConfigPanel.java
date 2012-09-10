package de.unikl.bcverifier.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.beust.jcommander.ParameterDescription;

public class EnumConfigPanel extends Panel {
	public EnumConfigPanel(String id, String param, Class<?> enumClass) {
		super(id);
		List<String> choices = new ArrayList<String>();
		List<Enum<?>> enums = EnumUtils.getEnumList((Class<Enum>)enumClass);
		for (Enum<?> e : enums) {
			choices.add(e.name());
		}
		add(new DropDownChoice<String>("enumdropdown", new EnumOptionModel(param), choices));
	}
	
	private static final class EnumOptionModel implements IModel<String> {
		private final String param;

		private EnumOptionModel(String param) {
			this.param = param;
		}

		public void detach() {}

		public String getObject() {
			return ((Enum<?>)ConfigSession.get().get(param)).name();
		}

		public void setObject(String object) {
			ParameterDescription pd = ConfigSession.get().getParam(param);
			Class<?> paramType = pd.getField().getType();
			Object value = EnumUtils.getEnum((Class<Enum>)paramType, object);
			ConfigSession.get().put(param, value);
		}
	}
}