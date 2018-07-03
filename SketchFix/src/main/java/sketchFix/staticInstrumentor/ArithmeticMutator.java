/**
 * @author Lisa Apr 10, 2017 ExpressionMutator.java 
 */
package  sketchFix.staticInstrumentor;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketchFix.staticInstrumentor.generator.OperatorGenerator;

public class ArithmeticMutator extends TransformRule {
	private Set<Operator> aop = new HashSet<Operator>(Arrays.asList(Operator.PLUS, Operator.MINUS,
			Operator.MULTIPLY, Operator.DIVIDE, Operator.REMAINDER));

	public ArithmeticMutator(TransformSubject subject) {
		super(subject);
		pre = "ao";
	}

	@Override
	public List<File> transform() {
		Statement targetStmt = subject.getTargetStmt();
		if (targetStmt instanceof ExpressionStmt)
			visit((ExpressionStmt) targetStmt, null);
		else if (targetStmt instanceof ReturnStmt)
			this.visit((ReturnStmt) targetStmt, null);
		else if (targetStmt instanceof ExplicitConstructorInvocationStmt)
			this.visit((ExplicitConstructorInvocationStmt) targetStmt, null);
		else if (targetStmt instanceof IfStmt)
			this.visit((IfStmt) targetStmt, null);
		return list;
	}

	public void visit(BinaryExpr expr, Void arg) {
		// relational rop
		super.visit(expr, arg);
		String type = "int";
		if (expr.getLeft() instanceof NameExpr) {
			NameExpr n = (NameExpr) expr.getLeft();
			type = subject.getOriginClass().resolveVarType(subject.getLocation().getMethod(), n);
		}

		Expression rep = OperatorGenerator.fetchOperator(expr, type, "AOP");
		if (rep != null)
			candidates.put(expr, rep);
	
		Expression left = expr.getLeft();
		Expression right = expr.getRight();
		if (candidates.containsKey(left)) {
			expr.setLeft(candidates.get(left));
			writeToFile();
			expr.setLeft(left);
		}
		if (candidates.containsKey(right)) {
			expr.setRight(candidates.get(right));
			writeToFile();
			expr.setRight(right);
		}
	}

	public void visit(IfStmt ifStmt, Void arg) {
		super.visit(ifStmt, arg);
		Expression cond = ifStmt.getCondition();
		if (candidates.containsKey(cond)) {
			ifStmt.setCondition(candidates.get(cond));
			writeToFile();
			ifStmt.setCondition(cond);
		}
	}
	
	public void visit(VariableDeclarationExpr n, Void arg) {
		super.visit(n, arg);
		if (n.getVariable(0).getInitializer().isPresent()) {
			VariableDeclarator varDecl = n.getVariable(0);
			Expression exp = varDecl.getInitializer().get();
			if (candidates.containsKey(exp)) {
				VariableDeclarator decl = new VariableDeclarator(n.getVariable(0).getType(), n.getVariable(0).getName(),
						candidates.get(exp));
				n.setVariable(0, decl);
				writeToFile();
				n.setVariable(0, varDecl);
			}
		}
	}
}