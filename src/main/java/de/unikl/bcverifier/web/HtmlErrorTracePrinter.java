package de.unikl.bcverifier.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.string.Strings;

import de.unikl.bcverifier.exceptionhandling.ErrorTracePrinter;
import de.unikl.bcverifier.isl.ast.Version;

public class HtmlErrorTracePrinter extends ErrorTracePrinter {

	private final List<String> lib1contents;
	private final List<AcePanel> lib1panels;
	private final List<String> lib2contents;
	private final List<AcePanel> lib2panels;
	public HtmlErrorTracePrinter(List<String> lib1contents,
			List<AcePanel> lib1panels, List<String> lib2contents,
			List<AcePanel> lib2panels) {
		super();
		this.lib1contents = lib1contents;
		this.lib1panels = lib1panels;
		this.lib2contents = lib2contents;
		this.lib2panels = lib2panels;
	}


	protected void printTable(List<String> column1, List<String> column2) {
            print("<table>");
            for (int i=0; i<column1.size(); i++) {
            	String c1 = column1.get(i);
            	String c2 = column2.get(i);
            	if (c2.isEmpty()) {
            		print("<tr><td colspan=\"2\">"+c1+"</td></tr>");
            	} else {
	            	print("<tr><td>");
					print(c1);
	            	print("</td><td>");
					print(c2);
	            	print("</td><tr>");
            	}
            }
            print("</table>");
    }


	protected String esc(String msg) {
		return Strings.escapeMarkup(msg).toString();
	}


	protected String makeLink(Version currentLib, String file, int line) {
		String text = "("+file+":"+line+")";
		
		List<AcePanel> panels = currentLib == Version.OLD ? lib1panels : lib2panels;
		List<String> contents = currentLib == Version.OLD ? lib1contents : lib2contents;
		if (panels.isEmpty()) {
			return text;
		}
		AcePanel panel = panels.get(0);
		// pattern to extract packet and classname
		Pattern p = Pattern.compile("^(\\w+\\.)*(\\w+)$");
		Matcher matcher = p.matcher(file);
		if (matcher.matches()) {
			String pack = matcher.group(1);
			String clazz = matcher.group(2);
			// search the panel containing the class clazz
			for (int i=0; i<contents.size(); i++) {
				String content = contents.get(i);
				if (Pattern.compile("(class|interface)\\s+"+clazz).matcher(content).find()) {
					panel = panels.get(i);
					break;
				}
			}
		}
		return "<a href=\"#\" onclick=\"acegoto('"+panel.getAceId()+"',"+line+",0); return false;\">"+text+"</a>";
	}


	@Override
	public String getOutput() {
		flush();
        StringBuffer buffer = new StringBuffer();
        for(String line : lines) {
            buffer.append(String.format("%s%n", line));
        }
        return buffer.toString();
	}
}
