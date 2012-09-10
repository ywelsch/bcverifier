package de.unikl.bcverifier.web.backend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class FileNameExtractor {
	public static Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("package\\s+(\\w+(\\s*\\.\\s*\\w+)*)\\s*\\;");
	public static Pattern FILE_NAME_PATTERN = Pattern.compile("(class|interface)\\s+(\\w+)\\W");
	
	public static void createFiles(final File prefix, final List<String> libcontents) throws IOException {
		for (String s : libcontents) {
			File dir = prefix;
			Matcher dirmatcher = DIRECTORY_NAME_PATTERN.matcher(s);
			Matcher filematcher = FILE_NAME_PATTERN.matcher(s);
			if (dirmatcher.find()) {
				String separator = System.getProperty("file.separator");
				String path = dirmatcher.group(1);
				path = path.replaceAll("\\s", "");
				path = path.replaceAll("\\.", separator);
				dir = new File(prefix, path);
			}
			if (filematcher.find()) {
				String filename = filematcher.group(2) + ".java";
				File file = new File(dir, filename);
				FileUtils.writeStringToFile(file, s);
			} else {
				throw new IllegalArgumentException("Can not find a meaningful class or interface definition");
			}
		}
	}
}
