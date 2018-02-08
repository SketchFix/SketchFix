/**
 * @author Lisa Apr 23, 2017 FileParser.java 
 */
package sketch4j.repair.staticAnalyzer.eval;

import java.io.File;
import java.io.PrintWriter;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class FileParser {
	PrintWriter writer;
	int count = 0;

	public FileParser(PrintWriter writer) {
		this.writer = writer;
	}

	public void parseFile(File file) {
		try {
			CompilationUnit cu = JavaParser.parse(file);
			TestCounterVisitor visitor = new TestCounterVisitor(writer);
			visitor.visit(cu, null);
			count += visitor.getCount();
		} catch (Exception e) {
		}
	}

	public int getCount() {
		return count;
	}
}
