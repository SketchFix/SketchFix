/**
 * @author Lisa Apr 10, 2017 ExpressionMutator.java 
 */
package sketch4j.repair.staticInstrumentor;

import java.io.File;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketch4j.repair.staticInstrumentor.generator.ExpressionGenerator;

public class ExpressionMutator extends TransformRule {

	public ExpressionMutator(TransformSubject subject) {
		super(subject);
		pre = "am";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			this.visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			this.visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			this.visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			this.visit((IfStmt) targetStmt, null);

		return list;
	}

	public void visit(FieldAccessExpr n, Void arg) {
		// transform
		List<Node> nodes = n.getChildNodes();
		String type = subject.getLibParser().fieldResolveType(nodes);
		EnumDeclaration en = subject.getLibParser().getEnum(type);
		Expression exp = null;
		if (en == null)
			exp = ExpressionGenerator.fetchEXP(subject, type);
		else
			exp = ExpressionGenerator.fetchENUM(en, type);
		if (exp != null)
			candidates.put(n, exp);
	}

	public void visit(MethodCallExpr call, Void arg) {
		super.visit(call, arg);

		Expression scope = call.getScope().isPresent() ? call.getScope().get() : null;

		if (scope != null && candidates.containsKey(scope)) {
			call.setScope(candidates.get(scope));
			writeToFile();
			call.setScope(scope);
		}
		NodeList<Expression> param = call.getArguments();
		for (int i = 0; i < param.size(); i++) {
			Expression p = param.get(i);
			if (candidates.containsKey(p)) {
				call.setArgument(i, candidates.get(p));
				writeToFile();
				call.setArgument(i, p);
			}
		}

	}

	public void visit(BinaryExpr expr, Void arg) {
		super.visit(expr, arg);
		Expression left = expr.getLeft();
		if (candidates.containsKey(left)) {
			expr.setLeft(candidates.get(left));
			writeToFile();
			expr.setLeft(left);
		}
	}

	public void visit(ExplicitConstructorInvocationStmt thiz, Void arg) {
		super.visit(thiz, arg);
		NodeList<Expression> params = thiz.getArguments();
		for (int i = 0; i < params.size(); i++) {
			Expression p = params.get(i);
			if (candidates.containsKey(p)) {
				thiz.setArgument(i, candidates.get(p));
				writeToFile();
				thiz.setArgument(i, p);
			}
		}
	}

	public void visit(NameExpr n, Void arg) {
		// transform
		String type = subject.getOriginClass().resolveVarType(subject.getLocation().getMethod(), n);
		Expression e = ExpressionGenerator.fetchEXP(subject, type);
		if (e != null)
			candidates.put(n, e);
	}

	public void visit(ReturnStmt n, Void arg) {
		super.visit(n, arg);
		try {
			Expression rtnExpr = n.getExpression().get();
			if (candidates.containsKey(rtnExpr)) {
				n.setExpression(candidates.get(rtnExpr));
				writeToFile();
				n.setExpression(rtnExpr);
			}
		} catch (Exception e) {
		}
	}

	public void visit(BooleanLiteralExpr expr, Void arg) {
		Expression e = ExpressionGenerator.fetchEXP(subject, "boolean");
		if (e != null)
			candidates.put(expr, e);
	}

	public void visit(ConditionalExpr n, Void arg) {
		// transform
		super.visit(n, arg);
		Expression exp = n.getCondition();
		if (candidates.containsKey(exp)) {
			n.setCondition(candidates.get(exp));
			writeToFile();
			n.setCondition(exp);
		}
		exp = n.getElseExpr();
		if (candidates.containsKey(exp)) {
			n.setElseExpr(candidates.get(exp));
			writeToFile();
			n.setElseExpr(exp);
		}
		exp = n.getThenExpr();
		if (candidates.containsKey(exp)) {
			n.setThenExpr(candidates.get(exp));
			writeToFile();
			n.setThenExpr(exp);
		}

	}

}
