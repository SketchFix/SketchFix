/**
 * @author Lisa Apr 10, 2017 SimpleFaultLocalizer.java 
 */
package sketch4j.repair.faultLocalizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Assume the fault location has been given
 *
 */
public class SimpleFaultLocalizer {
	private String dir;

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public List<Location> readConfig(String file, int id) {
		List<Location> locList = new ArrayList<Location>();
		File f = new File(file);
		if (!f.exists()) {
			String[] tk = file.split(":");
			locList.add(new Location(dir + tk[0].replace(".", "/") + ".java", "", Integer.parseInt(tk[1]), id));
			return locList;
		}
		Scanner scan;
		try {
			scan = new Scanner(new File(file));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.startsWith("sketchFix.classes.modified")) {
					String[] tests = line.split(",");
					for (String t : tests) {
						String[] tk = t.substring(t.indexOf("=") + 1).split("::");
						locList.add(new Location(dir + tk[0].replace(".", "/") + ".java", tk[1],
								Integer.parseInt(tk[2]), 1));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[SketchFix]: fault location file does not exist or wrong location");
		}
		return locList;
	}
}
