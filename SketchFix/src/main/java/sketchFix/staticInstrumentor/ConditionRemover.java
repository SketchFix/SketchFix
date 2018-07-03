/**
 * @author Lisa Apr 10, 2017 ExpressionMutator.java 
 */
package  sketchFix.staticInstrumentor;

import java.io.File;
import java.util.List;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

public class ConditionRemover extends TransformRule {

	public ConditionRemover(TransformSubject subject) {
		super(subject);
		pre = "cr";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof IfStmt)
			visit((IfStmt) targetStmt, null);
		return list;
	}

	public void visit(BinaryExpr expr, Void arg) {
		super.visit(expr, arg);
		if (expr.getOperator() == Operator.AND || expr.getOperator() == Operator.OR) {
			Expression right = expr.getRight();
			expr.setOperator(Operator.AND);
			expr.setRight(new BooleanLiteralExpr(true));
			writeToFile();
			expr.setRight(right);
		}
	}

	public void visit(IfStmt stmt, Void arg) {
		super.visit(stmt, arg);
		Expression  cond = stmt.getCondition();
		BinaryExpr bin = new BinaryExpr(new BooleanLiteralExpr(false),cond, BinaryExpr.Operator.AND);
		stmt.setCondition(bin);
		writeToFile();
		stmt.setCondition(cond);
	}
}
