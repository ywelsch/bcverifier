package de.unikl.bcverifier;

import java.io.File;
import java.util.ArrayList;

public class EclipseRunconfigHelper {
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Please specify at least the path to invariant file.");
        } else {
            File invariantFile = new File(args[0]);
            File libraryDir = invariantFile.getParentFile().getParentFile();
            File lib1Dir = new File(libraryDir, "old");
            File lib2Dir = new File(libraryDir, "new");
            ArrayList<String> params = new ArrayList<String>();
            params.add("-i"); params.add(invariantFile.getAbsolutePath());
            params.add("-l"); params.add(lib1Dir.getAbsolutePath()); params.add(lib2Dir.getAbsolutePath());
            for(int i=1; i<args.length; i++){
                params.add(args[i]);
            }
            Main.main(params.toArray(new String[params.size()]));
        }
    }
}
