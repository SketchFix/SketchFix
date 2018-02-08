/**
 * @author Lisa Apr 10, 2017 ExpressionMutator.java 
 */
package sketch4j.repair.staticInstrumentor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import sketch4j.repair.staticAnalyzer.visitor.LibraryVisitor;
import sketch4j.repair.staticAnalyzer.visitor.MethodVisitor;
import sketch4j.repair.staticInstrumentor.generator.ExpressionGenerator;

public class OverloadMutator extends TransformRule {
	private Map<String, Expression> typeCandidates = new HashMap<String, Expression>();
	private Map<NameExpr, String> varType = new HashMap<NameExpr, String>();

	public OverloadMutator(TransformSubject subject) {
		super(subject);
		pre = "ov";
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

	public void visit(NameExpr expr, Void arg) {
		super.visit(expr, arg);
		// FIXME I know now I can only resolve vars in overload
		String type = subject.getOriginClass().resolveVarType(subject.getLocation().getMethod(), expr);
		if (type != null && !type.equals(""))
			varType.put(expr, type);
	}

	@SuppressWarnings("rawtypes")
	public void visit(ObjectCreationExpr expr, Void arg) {
		super.visit(expr, arg);
		String type = expr.getType().getNameAsString();
		List<LibraryVisitor> list = subject.getLibParser().getClassByName(type);
		List<String> orig = resolveTypes(expr.getArguments());
		if (list == null) {
			List<Class[]> params = subject.getLibParser().fetchMethods(type, type);
			for (Class[] p : params)
				transformOneConstructor(orig, expr.getArguments(), p);
		}
	}

	public void visit(MethodCallExpr call, Void arg) {
		super.visit(call, arg);
		
		// current class methods overload
		if (fetchInClassOverload(call))
			fetchLibOverload(call);
	}

	@SuppressWarnings("rawtypes")
	private void transformOneConstructor(List<String> origTypes, NodeList<Expression> originParam, Class[] param) {
		List<String> target = new ArrayList<String>();
		for (Class c : param) {
			String name = c.getName();
			name = name.substring(name.lastIndexOf(".")+1);
			target.add(name);
			if (!typeCandidates.containsKey(name))
				typeCandidates.put(name, ExpressionGenerator.fetchEXP(subject, name));
		}
		transformOneMethod(origTypes, target, originParam);
	}

	private boolean fetchInClassOverload(MethodCallExpr call) {
		String name = call.getNameAsString();
		List<MethodVisitor> methods = subject.getOriginClass().getOverloadMethods(name);
		if (methods == null)
			return true;
		if (methods != null && methods.size() < 2) {
			return false;
		}
		List<String> origTypes = resolveTypes(call.getArguments());
		if (origTypes == null)
			return false;
		for (MethodVisitor mtd : methods) {
			transformOneMethod(origTypes, resolveParams(mtd.getParam()), call.getArguments());
		}
		return false;
	}

	private void fetchLibOverload(MethodCallExpr call) {

		Expression exp = call.getScope().isPresent() ? call.getScope().get() : null;
		List<LibraryVisitor> libs = null;
		if (exp != null && (exp instanceof NameExpr)) {
			NameExpr n = (NameExpr) exp;
			String clazz = subject.getOriginClass().resolveVarType(subject.getLocation().getMethod(), n);
			libs = subject.getLibParser().getClassByName(clazz);
			if (libs == null)
				libs = subject.getLibParser().getClassByName(n.getNameAsString());
		}
		if (libs == null) {
			return;
		}

		List<MethodDeclaration> methods = null;
		String name = call.getNameAsString();
		for (LibraryVisitor cls : libs) {
			if (cls.getMethods().get(name) != null) {
				methods = cls.getMethods().get(name);
				break;
			}
		}
		if (methods == null || methods.size() < 2)
			return;
		List<String> origTypes = resolveTypes(call.getArguments());
		if (origTypes == null)
			return;
		for (MethodDeclaration mtd : methods) {
			transformOneMethod(origTypes, resolveParams(mtd.getParameters()), call.getArguments());
		}
	}

	private List<String> resolveTypes(NodeList<Expression> origParam) {
		List<String> types = new ArrayList<String>();
		for (int i = 0; i < origParam.size(); i++) {
			String type = varType.get(origParam.get(i));
			type = type == null ? "double" : type;
			types.add(type);
			if (!typeCandidates.containsKey(type))
				typeCandidates.put(type, ExpressionGenerator.fetchEXP(subject, type));
		}
		return types;
	}

	private List<String> resolveParams(NodeList<Parameter> origParam) {
		List<String> types = new ArrayList<String>();
		for (int i = 0; i < origParam.size(); i++) {
			String type =  origParam.get(i).getType().toString();
			types.add(type == null ? "double" : type);
			if (!typeCandidates.containsKey(type))
				typeCandidates.put(type, ExpressionGenerator.fetchEXP(subject, type));
		}
		return types;
	}

	private void transformOneMethod(List<String> origTypes, List<String> target, NodeList<Expression> origin) {
		// do not transform more than one delta
		if (target.size() - origTypes.size() > 1 || target.size() < origTypes.size())
			return;
		List<Edit> edits = editDistance(target, origTypes);
		if (edits == null)
			return;
		for (Edit e : edits) {
			switch (e.type) {
			case EDIT:
				Expression orig = origin.get(e.id);
				if (!typeCandidates.containsKey(e.target))	break;
				origin.set(e.id, typeCandidates.get(e.target));
				writeToFile();
				origin.set(e.id, orig);
				break;
			case INSERT:
				if (!typeCandidates.containsKey(e.target))	break;
				origin.add(e.id, typeCandidates.get(e.target));
				writeToFile();
				origin.remove(e.id);
				break;
			case DELETE:
				orig = origin.get(e.id);
				origin.remove(e.id);
				writeToFile();
				origin.add(e.id, orig);
				break;
			default:
				break;
			}
		}
	}

	private List<Edit> editDistance(List<String> target, List<String> orig) {
		int m = target.size(), n = orig.size();
		int[][] dp = new int[m + 1][n + 1];
		List<Edit> edits = new ArrayList<Edit>();
		int num = 0;
		for (int i = 0; i <= m; i++)
			dp[i][0] = i;
		for (int i = 0; i <= n; i++)
			dp[0][i] = i;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (target.get(i).equals(orig.get(j)))
					dp[i + 1][j + 1] = dp[i][j];
				else
					dp[i + 1][j + 1] = Math.min(dp[i][j] + 1, Math.min(dp[i][j + 1] + 1, dp[i + 1][j] + 1));
			}
		}
		int i = m, j = n;
		while (i > 0 && j > 0) {
			int min = Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j]));
			if (min==dp[i - 1][j - 1] && dp[i - 1][j - 1] == dp[i][j]) {
				edits.add(0, new Edit(EditType.NOCHANGE, target.get(i - 1), orig.get(j - 1), j - 1));
				i--;
				j--;
				continue;
			}
			else if (dp[i - 1][j - 1] == min) {
				edits.add(0, new Edit(EditType.EDIT, target.get(i - 1), orig.get(j - 1), j - 1));
				i--;
				j--;
				num++;
			} else if (min == dp[i - 1][j]) {
				edits.add(0, new Edit(EditType.INSERT, target.get(i - 1), null, j));
				i--;
				num++;
			} else if (min == dp[i][j - 1]) {
				edits.add(0, new Edit(EditType.DELETE, null, orig.get(j - 1), j - 1));
				j--;
				num++;
			}
		}
		while (i > 0) {
			edits.add(0, new Edit(EditType.INSERT, target.get(--i), null, j));
			num++;
		}
		while (j > 0) {
			edits.add(0, new Edit(EditType.DELETE, target.get(--j), null, j));
			num++;
		}
		return num > 1 ? null : edits;
	}

	class Edit {
		EditType type;
		String orig;
		String target;
		int id;

		public Edit(EditType ty, String t, String o, int id) {
			type = ty;
			orig = o;
			target = t;
			this.id = id;
		}

		public String toString() {
			return type.toString() + "  " + orig + " " + target + " " + id;
		}
	}

	enum EditType {
		EDIT, INSERT, DELETE, NOCHANGE
	}
}
