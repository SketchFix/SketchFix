/**
 * @author Lisa Apr 10, 2017 ExpressionMutator.java 
 */
package sketch4j.repair.staticInstrumentor;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketch4j.repair.staticInstrumentor.generator.ExpressionGenerator;

public class ConditionMutator extends TransformRule {

	public ConditionMutator(TransformSubject subject) {
		super(subject);
		pre = "cm";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			visit((IfStmt) targetStmt, null);
		return list;
	}

	public void visit(NameExpr n, Void arg) {
		// transform
		String type = subject.getOriginClass().resolveVarType(subject.getLocation().getMethod(), n);
		Expression e = ExpressionGenerator.fetchEXP(subject, type);
		if (e != null)
			candidates.put(n, e);
	}

	public void visit(IfStmt stmt, Void arg) {
		super.visit(stmt, arg);
		fetchCOND(stmt);
	}

	public void visit(BinaryExpr expr, Void arg) {
		super.visit(expr, arg);
		if (expr.getOperator() == Operator.AND || expr.getOperator() == Operator.OR) {
			fetchCOND(expr);
		}
	}

	private void fetchCOND(Node node) {
		List<String> types = new ArrayList<String>();
		List<Node> vars = subject.getVisibleVars();
		NodeList<Expression> param = ExpressionGenerator.getParameter(vars);
		PriorityQueue<Node> q = new PriorityQueue<Node>(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o2.getBegin().get().line - o1.getBegin().get().line;
			}
		});
		q.addAll(vars);
		while (!q.isEmpty()) {
			Node var = q.poll();
			if (var instanceof VariableDeclarator) {
				VariableDeclarator v = (VariableDeclarator) var;
				String type = v.getType().toString();
				if (!types.contains(type)) {
					types.add(type);
					writeCondition(param, type, node);
				}
			}
		}
	}

	private void writeCondition(NodeList<Expression> param, String type, Node node) {
		param.add(0, new FieldAccessExpr(new NameExpr(type), "class"));
		MethodCallExpr call = new MethodCallExpr(new NameExpr("Sketch4J"), new SimpleName("COND"), param);
		if (node instanceof IfStmt) {
			IfStmt stmt = (IfStmt) node;
			Expression cond = stmt.getCondition();
			stmt.setCondition(new BinaryExpr(cond, call, Operator.OR));
			writeToFile();
			stmt.setCondition(cond);
			stmt.setCondition(new BinaryExpr(cond, call, Operator.AND));
			writeToFile();
			stmt.setCondition(cond);
		} else if (node instanceof BinaryExpr) {
			BinaryExpr expr = (BinaryExpr) node;
			Expression left = expr.getLeft();
			expr.setLeft(new BinaryExpr(left, call, Operator.OR));
			writeToFile();
			expr.setLeft(left);
			expr.setLeft(new BinaryExpr(left, call, Operator.AND));
			writeToFile();
			expr.setLeft(left);
		}
		param.remove(0);
	}

}
