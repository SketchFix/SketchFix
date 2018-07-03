/**
 * @author Lisa Apr 10, 2017 Defects4JConfigReader.java 
 */
package sketchFix.config;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import sketchFix.staticAnalyzer.visitor.ClassVisitor;

public class Defects4JConfigReader extends ConfigReader {
	ClassVisitor modifiedClass;
	
	public void readConfigFile(String file) {
		base = file.substring(0,file.lastIndexOf("/")+1);
		Scanner scan;
		try {
			scan = new Scanner(new File(file));

			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.startsWith("d4j.classes.modified")) {
					setFaultyFile(line.split("=")[1].split(","));
				} else if (line.startsWith("d4j.dir.src.classes"))
					setSourceFolder(line.split("=")[1]);
				else if (line.startsWith("d4j.dir.src.tests"))
					setTestFolder(line.split("=")[1]);
				else if (line.startsWith("d4j.tests.trigger")) {
					String[] tests = line.split("=")[1].split(",");
					Set<String> set = new HashSet<String>();
					for (String t : tests)
						set.add(t.substring(0, t.indexOf("::")));
					setFailingTests(set.toArray(new String[set.size()]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
