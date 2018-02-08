/**
 * @author Lisa Mar 13, 2017 StaticVisitor.java 
 */
package sketch4j.repair.staticAnalyzer.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<Void> {
	// I know I cannot handle multi same vars.
	private Map<String, VariableDeclarator> vars = new HashMap<String, VariableDeclarator>();
	private Map<VariableDeclarator, Statement> varScope = new HashMap<VariableDeclarator, Statement>();
	private TreeSet<NodeWithRange<?>> stmts = new TreeSet<NodeWithRange<?>>(Node.NODE_BY_BEGIN_POSITION);
	private NodeList<Parameter> param;
	private MethodDeclaration method;
	private Stack<Statement> currentStmt = new Stack<Statement>();
	private ConstructorDeclaration constructor;
	private String name = "";

	public MethodVisitor(MethodDeclaration mtd) {
		method = mtd;
		name = method.getNameAsString();
		param = method.getParameters();
		super.visit(method, null);
	}

	public MethodVisitor(ConstructorDeclaration construct) {
		constructor = construct;
		param = constructor.getParameters();
		name = construct.getNameAsString();
		super.visit(constructor, null);
	}

	public void visit(VariableDeclarator n, Void arg) {
		vars.putIfAbsent(n.getNameAsString(), n);
		varScope.put(n, currentStmt.peek());
	}

	public void visit(BlockStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		currentStmt.push(expr);
		// stmts.add(expr);
		super.visit(expr, null);
	}

	public void visit(ExpressionStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
	}

	public void visit(ForStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
		super.visit(expr, null);
	}

	public void visit(IfStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);

	}

	public void visit(ThrowStmt expr, Void arg) {
		stmts.add(expr);

	}

	public void visit(TryStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
	}

	public void visit(WhileStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
	}

	public void visit(ReturnStmt expr, Void arg) {
		if (!currentStmt.isEmpty() && expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
	}

	public void visit(BreakStmt expr, Void arg) {
		stmts.add(expr);

	}

	public void visit(ExplicitConstructorInvocationStmt expr, Void arg) {
		if (expr.getBegin().get().line >= currentStmt.peek().getEnd().get().line)
			currentStmt.pop();
		stmts.add(expr);
		super.visit(expr, null);
	}

	public Statement fetchStatement(int lineNum) {
		Stack<Statement> stack = new Stack<Statement>();
		for (NodeWithRange<?> n : stmts) {
			if (n.getBegin().get().line > lineNum)
				continue;
			else if (n.getEnd().get().line < lineNum)
				continue;
			if (n instanceof Statement)
				stack.push((Statement) n);
		}
		return stack.isEmpty() ? null : stack.pop();
	}

	public List<VariableDeclarator> fetchVisibleVar(int lineNum) {
		List<VariableDeclarator> visible = new ArrayList<VariableDeclarator>();
		for (VariableDeclarator v : vars.values()) {
			System.out.println(v.getNameAsString() + " " + v.getBegin().get().line + " "
					+ varScope.get(v).getBegin().get().line + " " + varScope.get(v).getEnd().get().line);
			if (v.getBegin().get().line >= lineNum)
				continue;
			try {
				if (varScope.get(v).getBegin().get().line < lineNum && varScope.get(v).getEnd().get().line > lineNum)
//					 if (varScope.get(v).getBegin().get().line < lineNum)
					visible.add(v);
			} catch (Exception e) {
			}
		}
		return visible;
	}

	public boolean containsLine(int lineNum) {
		if (constructor != null)
			return constructor.getBegin().get().line < lineNum && constructor.getEnd().get().line > lineNum;
		return method.getBegin().get().line < lineNum && method.getEnd().get().line > lineNum;

	}

	public String fetchVarType(String var) {
		if (vars.containsKey(var))
			return vars.get(var).getType().toString();
		else {
			for (Parameter p : param)
				if (p.getName().toString().equals(var))
					return p.getType().toString();
		}
		return null;
	}

	public TreeSet<NodeWithRange<?>> getStmts() {
		return stmts;
	}

	public NodeList<Parameter> getParam() {
		return param;
	}

	public NodeWithRange<Node> getMethod() {
		return method == null ? constructor : method;
	}

	public int getStartPosition() {
		return method!=null? method.getRange().get().begin.line: constructor.getRange().get().begin.line;
	}

	public String getName() {
		return name;
	}
}
