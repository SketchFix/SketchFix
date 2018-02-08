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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketch4j.repair.staticInstrumentor.generator.ExpressionGenerator;

public class ConditionAdder extends TransformRule {

	public ConditionAdder(TransformSubject subject) {
		super(subject);
		pre = "ca";
	}

	@Override
	public List<File> transform() {
		try {
		Node targetStmt = subject.getTargetStmt().getParentNode().get();
		if (targetStmt instanceof ExpressionStmt)
			visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			visit((IfStmt) targetStmt, null);
		else if (targetStmt instanceof BlockStmt)
			visit((BlockStmt) targetStmt, null);
		}catch (Exception e) {}
		return list;
	}

	public void visit(BlockStmt stmt, Void arg) {
		super.visit(stmt, arg);
		int id = 0;
		NodeList<Statement> list = stmt.getStatements();
		boolean flag = false;
		for (; id < list.size(); id++)
			if (list.get(id).equals(subject.getTargetStmt())) {
				flag = true;
				break;
			}
		if (!flag)
			return;
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
					System.out.println(type);
					writeCondition(param, type, subject.getTargetStmt(), stmt, id);
					writeIfReturn(param, type, stmt, id);
				}
			}
		}
	}

	private void writeCondition(NodeList<Expression> param, String type, Statement thenStmt, BlockStmt parent, int id) {
		param.add(0, new FieldAccessExpr(new NameExpr(type), "class"));
		MethodCallExpr call = new MethodCallExpr(new NameExpr("Sketch4J"), new SimpleName("COND"), param);
		IfStmt stmt = new IfStmt();
		stmt.setCondition(call);
		stmt.setThenStmt(thenStmt);
		parent.setStatement(id, stmt);
		writeToFile();
		parent.setStatement(id, thenStmt);
		param.remove(0);
	}

	private void writeIfReturn(NodeList<Expression> param, String type, BlockStmt parent, int id) {
		param.add(0, new FieldAccessExpr(new NameExpr(type), "class"));
		MethodCallExpr call = new MethodCallExpr(new NameExpr("Sketch4J"), new SimpleName("COND"), param);
		IfStmt stmt = new IfStmt();
		stmt.setCondition(call);
		String returnType = subject.getOriginClass().getCurrMethodReturnType();
		ReturnStmt rtnStmt = new ReturnStmt();
		if (!returnType.equals("void"))
			rtnStmt = new ReturnStmt(ExpressionGenerator.fetchEXP(subject, returnType));
		stmt.setThenStmt(rtnStmt);
		parent.addStatement(id, stmt);
		writeToFile();
		parent.remove(stmt);
		param.remove(0);
	}

}
