/**
 * @author Lisa Apr 23, 2017 TestCounter.java 
 */
package sketchFix.staticAnalyzer.eval;

import java.io.PrintWriter;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class TestCounterVisitor extends VoidVisitorAdapter<Void> {
	private String clazzS = "";
	private PrintWriter writer;
	private int count = 0;

	public TestCounterVisitor(PrintWriter writer) {
		this.writer = writer;
	}

	public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
		clazzS = clazz.getNameAsString();
		super.visit(clazz, arg);
	}

	public void visit(MethodDeclaration mtd, Void arg) {
		super.visit(mtd, arg);
		String name = mtd.getDeclarationAsString();
		if (name.toLowerCase().contains("test")) {
//			writer.println(clazzS + "::" + name);
			count++;
		}
	}

	public int getCount() {
		return count;
	}
}
