/**
 * @author Lisa Apr 23, 2017 FailingTestCounter.java 
 */
package sketchFix.staticAnalyzer.eval;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class FailingTestCounter {
	PrintWriter writer;

	public FailingTestCounter(PrintWriter writer) {
		this.writer = writer;
	}

	public void locateTest(String source) {
		Scanner scan;
		try {
			scan = new Scanner(new File(source + "/defects4j.build.properties"));

			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.startsWith("d4j.dir.src.tests")) {
					source += line.split("=")[1] + "/";
				}
				if (line.startsWith("d4j.tests.trigger")) {
					String token = line.split("=")[1].split("::")[0].split(",")[0];
					 String file = source + token.replace(".",
					 "/") + ".java";
					 CompilationUnit cu = JavaParser.parse(new File(file));
					 TestCounterVisitor visitor = new
					 TestCounterVisitor(writer);
					 visitor.visit(cu, null);
					 writer.println(source + " " + visitor.getCount());
//					writer.println("cd "+source+"\n defects4j coverage -t "+token+" \n cd ..");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
