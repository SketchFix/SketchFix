/**
 * @author Lisa Mar 13, 2017 StaticVisitor.java 
 */
package sketch4j.repair.staticAnalyzer.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.ast.stmt.Statement;

import sketch4j.repair.faultLocalizer.Location;

public class ClassVisitor extends LibraryVisitor {
	private Map<String, List<MethodVisitor>> methods = new HashMap<String, List<MethodVisitor>>();
	private TreeMap<Integer, MethodVisitor> locMethods = new TreeMap<Integer, MethodVisitor>();
	private CompilationUnit cu;
	private Set<String> imps = new HashSet<String>();
	private NodeWithRange<Node> currMethod = null;

	public void visit(MethodDeclaration mtd, Void arg) {
		String name = mtd.getNameAsString();
		if (!methods.containsKey(name))
			methods.put(name, new ArrayList<MethodVisitor>());
		MethodVisitor mtdv = new MethodVisitor(mtd);
		methods.get(name).add(mtdv);
		locMethods.put(mtdv.getStartPosition(), mtdv);
	}

	public void visit(ConstructorDeclaration n, Void arg) {
		String name = n.getNameAsString();
		if (!methods.containsKey(name))
			methods.put(name, new ArrayList<MethodVisitor>());
		MethodVisitor mtdv = new MethodVisitor(n);
		methods.get(name).add(mtdv);
		locMethods.put(mtdv.getStartPosition(), mtdv);
	}

	public void visit(ImportDeclaration imp, Void arg) {
		String path = imp.getNameAsString();
		if (path.endsWith("*"))
			imps.add(path.substring(0, path.lastIndexOf(".")));
		else
			imps.add(path);
	}

	public Set<String> getImports() {
		return imps;
	}

	public void visit(CompilationUnit cu, Void arg) {
		this.cu = cu;
		cu.addImport("sketch4j.request.Sketch4J");
		super.visit(cu, arg);
	}

	/**
	 * Given a location, return a list of visible variables sorted by the
	 * declared distance. Does not consider inherited fields.
	 * 
	 * @return
	 */
	public List<Node> fetchVisibleVars(Location loc) {
		List<Node> visibleNodes = new ArrayList<Node>();
		MethodVisitor mtdv;
		String mtdName = loc.getMethod();
		int lineNum = loc.getLocation();
		if (mtdName.equals("") || !methods.containsKey(mtdName)) {
			mtdv = locMethods.floorEntry(lineNum).getValue();
			return fetchNode(mtdv, loc);
		} else {
			for (MethodVisitor mtd : methods.get(mtdName)) {
				if (mtd.containsLine(lineNum)) {
					return fetchNode(mtd, loc);
				}
			}
		}
		return visibleNodes;
	}

	private List<Node> fetchNode(MethodVisitor mtdv, Location loc) {
		List<Node> visibleNodes = new ArrayList<Node>();
		loc.setMethod(mtdv.getName());
		 NodeWithRange<Node> clazz = null;
		int lineNum = loc.getLocation();
		TreeMap<Integer, Node> map = new TreeMap<Integer, Node>();
		for (VariableDeclarator var : mtdv.fetchVisibleVar(lineNum)) {
			map.put(lineNum - var.getBegin().get().line, var);
		}
		visibleNodes.addAll(map.values());
		visibleNodes.addAll(mtdv.getParam());
		 clazz = mtdv.getMethod();
		for (List<VariableDeclarator> l : fields.values()) {
			for (VariableDeclarator v : l) {
				try {
					 Node n = v.getParentNode().get().getParentNode().get();
					 if (n.equals(clazz))
					visibleNodes.add(v);
					// else
					// System.out.println(v.getNameAsString()+" Not
					// add"+n.getBegin().get().line);
				} catch (Exception e) {
					continue;
				}
			}
		}

		return visibleNodes;

	}

	public Statement fetchStatementOnLineNumber(String mtdName, int lineNum) {
		if (mtdName.equals("")) {
			MethodVisitor mtdv = locMethods.floorEntry(lineNum).getValue();
			return (mtdv == null) ? null : mtdv.fetchStatement(lineNum);
		}
		for (MethodVisitor mtd : methods.get(mtdName)) {
			if (mtd.containsLine(lineNum)) {
				currMethod = mtd.getMethod();
				return mtd.fetchStatement(lineNum);
			}
		}
		return null;
	}

	public String resolveVarType(String mtdName, NameExpr string) {
		String name = string.getNameAsString();
		int num = string.getBegin().get().line;
		for (MethodVisitor mtd : methods.get(mtdName)) {
			if (mtd.containsLine(num)) {
				String type = mtd.fetchVarType(name);
				if (type != null)
					return type;
				else if (fields.containsKey(name))
					return fields.get(name).get(0).getType().toString();
			}
		}
		return null;
	}
	public String resolveVarType(String mtdName, Parameter string) {
		String name = string.getNameAsString();
		int num = string.getBegin().get().line;
		for (MethodVisitor mtd : methods.get(mtdName)) {
			if (mtd.containsLine(num)) {
				String type = mtd.fetchVarType(name);
				if (type != null)
					return type;
				else if (fields.containsKey(name))
					return fields.get(name).get(0).getType().toString();
			}
		}
		return null;
	}
	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	public List<MethodVisitor> getOverloadMethods(String name) {
		return methods.get(name);
	}

	public String getCurrMethodReturnType() {
		if (currMethod instanceof MethodDeclaration)
			return ((MethodDeclaration) currMethod).getType().toString();
		return "";
	}
}
