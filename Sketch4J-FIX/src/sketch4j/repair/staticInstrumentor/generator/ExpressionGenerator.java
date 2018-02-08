/**
 * @author Lisa Apr 12, 2017 ExpressionGenerator.java 
 */
package sketch4j.repair.staticInstrumentor.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import sketch4j.repair.staticInstrumentor.TransformSubject;

public class ExpressionGenerator {
	private final static Set<String> primitive = new HashSet<String>(Arrays.asList("int", "Integer", "String", "double",
			"Double", "byte", "Byte", "short", "Short", "long", "Long", "boolean", "Boolean","char"));
	@SuppressWarnings("serial")
	private final static Map<String, String> primToObj = new HashMap<String, String>() {
		{
			put("int", "Integer");
			put("double", "Double");
			put("long", "Long");
			put("float", "Float");
			put("boolean", "Boolean");
//			put("char", "Character");
		}
	};

	private static boolean isPrimitive = false;

	public static Expression fetchEXP(TransformSubject subject, String type) {
		isPrimitive = primitive.contains(type);
		return fetchExpressionOnType(subject.getVisibleVars(), type, "EXP");
	}

	public static Expression fetchENUM(EnumDeclaration en, String type) {
		List<Node> nodes = new ArrayList<Node>(en.getEntries());
		return fetchExpressionOnType(nodes, type, "EXP");
	}

	private static Expression fetchExpressionOnType(List<Node> nodes, String type, String query) {
		if (type == null || type.equals(""))
			return null;
		NameExpr clazz = new NameExpr(type);
		FieldAccessExpr field = new FieldAccessExpr(clazz, "class");
		NodeList<Expression> param = getParameter(nodes);
		param.add(0, field);
		MethodCallExpr call = new MethodCallExpr(new NameExpr("Sketch4J"), new SimpleName(query), param);
		String castType = primToObj.containsKey(type) ? primToObj.get(type) : type;
		EnclosedExpr enclose = new EnclosedExpr(new CastExpr(new ClassOrInterfaceType(castType), call));
		return enclose;
	}

	public static NodeList<Expression> getParameter(List<Node> vars) {
		
		NodeList<Expression> paramList = new NodeList<Expression>();
		NodeList<Expression> values = new NodeList<Expression>();
		NodeList<Expression> names = new NodeList<Expression>();
		String enumType = null;
		for (Node var : vars) {
			if (var instanceof VariableDeclarator) {
				VariableDeclarator v = (VariableDeclarator) var;
				// if (v.getType().toString().contains("<") ||
				// v.getType().toString().contains("["))
				// continue;
//				if (!isPrimitive && primitive.contains(v.getType().toString()))
//					continue;
				names.add(new NameExpr("\"" + v.getNameAsString() + "\""));
				values.add(new NameExpr(v.getName()));
			} else if (var instanceof Parameter) {
				Parameter p = (Parameter) var;
				if (p.getType().toString().contains("<"))
					continue;
				if (!isPrimitive && primitive.contains(p.getType().toString()))
					continue;
				values.add(new NameExpr(p.getName()));
				names.add(new NameExpr("\"" + p.getNameAsString() + "\""));
			} else if (var instanceof EnumConstantDeclaration) {
				EnumConstantDeclaration en = (EnumConstantDeclaration) var;
				if (enumType == null) {
					EnumDeclaration enDecl = (EnumDeclaration) en.getParentNode().get();
					enumType = enDecl.getNameAsString();
				}
				FieldAccessExpr expr = new FieldAccessExpr(new NameExpr(enumType), en.getNameAsString());
				values.add(expr);
				names.add(new NameExpr("\"" + enumType + "." + expr.getNameAsString() + "\""));

			}
		}
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
