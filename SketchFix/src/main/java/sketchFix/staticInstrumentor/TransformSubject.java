/**
 * @author Lisa Apr 12, 2017 TransformSubject.java 
 */
package  sketchFix.staticInstrumentor;

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;

import sketchFix.faultLocalizer.Location;
import sketchFix.staticAnalyzer.model.StaticFileParser;
import sketchFix.staticAnalyzer.visitor.ClassVisitor;
import sketchFix.staticAnalyzer.visitor.LibraryParser;

public class TransformSubject {
	private Location location;
	private ClassVisitor originClass;
	private List<Node> visibleVars;
	private Statement targetStmt;
	private LibraryParser libParser;

	public TransformSubject(Location loc, StaticFileParser parser) {
		location = loc;
		libParser = parser.getLibParser();
		originClass = parser.parseFautyClass(loc.getFilePath());
		visibleVars = originClass.fetchVisibleVars(loc);
		targetStmt = originClass.fetchStatementOnLineNumber(loc.getMethod(), loc.getLocation());
		libParser.setFaultyClass(originClass.getClassNames().iterator().next());
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public ClassVisitor getOriginClass() {
		return originClass;
	}

	public void setOriginClass(ClassVisitor originClass) {
		this.originClass = originClass;
	}

	public List<Node> getVisibleVars() {
		return visibleVars;
	}

	public void setVisibleVars(List<Node> visibleVars) {
		this.visibleVars = visibleVars;
	}

	public Statement getTargetStmt() {
		return targetStmt;
	}

	public void setTargetStmt(Statement targetStmt) {
		this.targetStmt = targetStmt;
	}

	public LibraryParser getLibParser() {
		return libParser;
	}

	public void setLibParser(LibraryParser libParser) {
		this.libParser = libParser;
	}

}
