/**
 * @author Lisa Apr 12, 2017 LibraryVisitor.java 
 */
package  sketchFix.staticAnalyzer.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class LibraryVisitor extends VoidVisitorAdapter<Void> {
	protected Map<String, List<VariableDeclarator>> fields = new HashMap<String, List<VariableDeclarator>>();
	private Map<String, List<MethodDeclaration>> methods = new HashMap<String, List<MethodDeclaration>>();
	private Set<String> className = new HashSet<String>();
	private Map<String, EnumDeclaration> enMap = new HashMap<String, EnumDeclaration>();

	public void visit(FieldDeclaration field, Void arg) {
		NodeList<VariableDeclarator> vars = field.getVariables();
		for (int i = 0; i < vars.size(); i++) {
			String name = vars.get(i).getName().toString();
			if (!fields.containsKey(name)) fields.put(name, new ArrayList<VariableDeclarator>());
			fields.get(name).add(vars.get(i));
		}
	}

	public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
		className.add(clazz.getNameAsString());
		super.visit(clazz, arg);
	}

	public void visit(EnumDeclaration enDecl, Void arg) {
		super.visit(enDecl, arg);
		enMap.put(enDecl.getNameAsString(), enDecl);
	}

	public Map<String, EnumDeclaration> enumerateMap() {
		return enMap;
	}

	public void visit(MethodDeclaration mtd, Void arg) {
		String name = mtd.getNameAsString();
		if (!methods.containsKey(name))
			methods.put(name, new ArrayList<MethodDeclaration>());
		methods.get(name).add(mtd);
	}

	public Map<String, List<MethodDeclaration>> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, List<MethodDeclaration>> methods) {
		this.methods = methods;
	}

	public List<VariableDeclarator> getField(String name) {
		return fields.get(name);
	}

	public Set<String> getClassNames() {
		return className;
	}
}
