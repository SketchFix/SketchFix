/**
 * @author Lisa Apr 10, 2017 TransformRule.java 
 */
package sketch4j.repair.staticInstrumentor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class TransformRule extends VoidVisitorAdapter<Void> {
	protected TransformSubject subject;
	protected List<File> list = new ArrayList<File>();
	protected Map<Expression, Expression> candidates = new HashMap<Expression, Expression>();
	protected int index = 0;
	protected String pre = "";
	private String source = "";
	// private static PrintWriter writer;
	private int id = 0;

	static {
		try {
			// writer = new
			// PrintWriter("/Users/lisahua/projects/demo/visibleVars.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public TransformRule(TransformSubject subject) {
		this.subject = subject;
//		source = subject.getLibParser().getSource()+"tmp3";
		String[] tkne =  subject.getLibParser().getSource().split("/");
		
		source = "/Users/lisahua/projects/sbfl/input/"+tkne[5];
		id = subject.getLocation().getId();
		// writer.println(subject.getLibParser().getSource()+","+subject.getVisibleVars().size());
		// writer.flush();
		File file = new File(source);
		if (!file.exists())
			file.mkdirs();
		String f = subject.getLocation().getFilePath();
		source += f.substring(f.lastIndexOf("/")) + "-";
	}

	/**
	 * <p>
	 * Given targetStmt, first find all expressions that fits this template.
	 * </p>
	 * Then for each fitted expression, transform the original class based on
	 * the template
	 * 
	 * @return
	 */
	public abstract List<File> transform();

	protected void writeToFile() {
		String path = source + pre +"-"+ id+"-"+ index++;
		try {
			PrintWriter writer = new PrintWriter(path);
//			System.out.println(subject.getOriginClass().getCompilationUnit());
			writer.println(subject.getOriginClass().getCompilationUnit());
			System.out.println(path);
		
			writer.close();
		} catch (Throwable e) {
//			e.printStackTrace();
			new File(path).delete();
		}

	}
}
