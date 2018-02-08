/**
 * @author Lisa Apr 12, 2017 ExpressionGenerator.java 
 */
package sketch4j.repair.staticInstrumentor.generator;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class OperatorGenerator {

	public static Expression fetchROP(BinaryExpr expr, String type) {
		NodeList<Expression> param = getParameter(expr);
		param.add(0, new FieldAccessExpr(new NameExpr(type), "class"));
		MethodCallExpr call = new MethodCallExpr(new NameExpr("Sketch4J"), new SimpleName("EXPOP"), param);
		return call;

	}

	private static NodeList<Expression> getParameter(BinaryExpr expr) {
		NodeList<Expression> paramList = new NodeList<Expression>();
		NodeList<Expression> values = new NodeList<Expression>();
		NodeList<Expression> names = new NodeList<Expression>();
		values.add(expr.getLeft());
		values.add(expr.getRight());
		names.add(new NameExpr("\"" + expr.getLeft().toString() + "\""));
		names.add(new NameExpr("\"" + expr.getRight().toString() + "\""));

		NodeList<ArrayCreationLevel> arrLevel = new NodeList<ArrayCreationLevel>();
		arrLevel.add(new ArrayCreationLevel());
		ArrayCreationExpr arrNames = new ArrayCreationExpr(new ClassOrInterfaceType("String"), arrLevel,
				new ArrayInitializerExpr(names));
		ArrayCreationExpr arrValues = new ArrayCreationExpr(new ClassOrInterfaceType("Object"), arrLevel,
				new ArrayInitializerExpr(values));
		paramList.add(arrNames);
		paramList.add(arrValues);
		paramList.add(new IntegerLiteralExpr("0"));
		paramList.add(new BooleanLiteralExpr(true));
		paramList.add(new IntegerLiteralExpr("1"));
		paramList.add(new IntegerLiteralExpr("0"));
		return paramList;
	}
}
