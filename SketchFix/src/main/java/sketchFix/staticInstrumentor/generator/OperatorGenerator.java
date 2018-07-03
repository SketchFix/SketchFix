/**
 * @author Lisa Apr 12, 2017 ExpressionGenerator.java 
 */
package  sketchFix.staticInstrumentor.generator;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class OperatorGenerator {
	@SuppressWarnings("serial")
	private final static Map<String, String> primToObj = new HashMap<String, String>() {
		{
			put("int", "Integer");
			put("double", "Double");
			put("long", "Long");
			put("float", "Float");
			put("boolean", "Boolean");
		}
	};
	public static Expression fetchOperator(BinaryExpr expr, String type, String query) {
		NodeList<Expression> param = getParameter(expr);
		param.add(new FieldAccessExpr(new NameExpr(type), "class"));
		MethodCallExpr call = new MethodCallExpr(new NameExpr("SketchFix"), new SimpleName(query), param);
		MethodCallExpr invokeCall = new MethodCallExpr(call, new SimpleName("invoke"), new NodeList<Expression>());
		String castType = primToObj.containsKey(type) ? primToObj.get(type) : type;
		EnclosedExpr enclose = new EnclosedExpr(new CastExpr(new ClassOrInterfaceType(castType), invokeCall));
		return enclose;
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
		paramList.add(arrValues);
		paramList.add(new IntegerLiteralExpr("0"));
		paramList.add(arrNames);
		return paramList;
	}
}
