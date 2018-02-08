package sketch4j.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ComputeVariableInfo {
	/** The path to the source folder.  This should be specified by the user. */
	public static final String SOURCE_FOLDER_PATH = "src";
	/** Currently the second last argument in METHOD_INVOCATION represents the values of variables */
	public static final int VALUES_INDEX_DELTA = -2;
	
	public static CompilationUnit findCompilationUnit(String fqn) {
		String sourceFilePath = Paths.get(SOURCE_FOLDER_PATH, fqn.replaceAll("\\.", File.separator) + ".java").toString();
		FileInputStream in = null;
		try {
			in = new FileInputStream(sourceFilePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File " + sourceFilePath + " not found.");
		}
		return JavaParser.parse(in);
	}
	@SuppressWarnings("unused")
	public static class VariableInfoCollector extends VoidVisitorAdapter<Void> {
		
		private String methodName;
		private int lineNumber;
		private List<String> parameterNames;
		
		public VariableInfoCollector(String methodName, int lineNumber) {
			this.methodName = methodName;
			this.lineNumber = lineNumber;
			this.parameterNames = new ArrayList<>();
		}
		
		/**
		 * Try to find the method invocation that introduce holes.  We assume
		 * the method invocations cannot be at the same line for now.
		 * E.g. METHOD_INVOCATION(...); METHOD_INVOCATION(...); // Same line
		 * We also assume the user pass array initialization to the
		 * METHOD_INVOCATION for variables.
		 */
		@Override
		public void visit(MethodCallExpr n, Void arg) {
//			if (methodName.equals(n.getNameAsString()) && lineNumber == n.getBegin().get().line) {
//				int value_index = n.getArguments().size() + VALUES_INDEX_DELTA;
//				Expression expr = n.getArgument(value_index);
//				String initializer = ((ArrayCreationExpr) expr).getInitializer().get().toString();
//				int len = initializer.length();
//				if (len > 2) {
//					String elems = initializer.substring(1, len - 1);
//					for (String elem : elems.split(",")) {
//						parameterNames.add(elem.trim());
//					}
//				}
//			}
			super.visit(n, arg);
		}
		
		public String[] getParameterNames() {
			return parameterNames.toArray(new String[parameterNames.size()]);
		}
	}
	
	/**
	 * For each non-null value, we convert to its class.  For null,
	 * we use Object class.  This implementation requires that we treat
	 * Object type of values assignable from anything.
	 * @param argValues
	 * @return
	 */
	public static Class<?>[] getParameterTypes(Object[] argValues) {
		Class<?>[] parameterTypes = new Class<?>[argValues.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			if (argValues[i] == null) {
				parameterTypes[i] = Object.class;
			} else {
				parameterTypes[i] = ((Object) argValues[i]).getClass();
			}
		}
		return parameterTypes;
	}
}
