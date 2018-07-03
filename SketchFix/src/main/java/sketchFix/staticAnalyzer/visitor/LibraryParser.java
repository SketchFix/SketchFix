/**
 * @author Lisa Apr 12, 2017 LibraryParser.java 
 */
package  sketchFix.staticAnalyzer.visitor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;

public class LibraryParser {
	Map<String, List<LibraryVisitor>> library = new HashMap<String, List<LibraryVisitor>>();
	Map<String, EnumDeclaration> enumMap = new HashMap<String, EnumDeclaration>();
	String faultyClass = "";
	private Map<String, Class<?>[]> external = new HashMap<String, Class<?>[]>();
	String source = "";

	public String fieldResolveType(List<Node> fields) {
		LibraryVisitor type = library.get(faultyClass) == null ? null : library.get(faultyClass).get(0);
		String typeStr = "";
		String name ="";
		

		for (Node n : fields) {
			name = n.toString();
			if (type == null) {
				if (Character.isUpperCase(name.charAt(0)) && library.containsKey(name)) {
					for (LibraryVisitor visitor : library.get(name)) {
						type = visitor;
						typeStr = type.toString();
					}
				} else if (enumMap.containsKey(name)) {
					typeStr = name;
				}
			} else {
				if (Character.isUpperCase(name.charAt(0)) && library.containsKey(name)) {
					for (LibraryVisitor visitor : library.get(name)) {
						type = visitor;
						typeStr = type.toString();
					}
				} else if (enumMap.containsKey(name)) {
					typeStr = name;
				}
				List<VariableDeclarator> l = type.getField(name);
				if (l == null)
					continue;
				Type t = l.get(0).getType();
				typeStr = t.toString();
				if (library.containsKey(typeStr))
					type = library.get(typeStr).get(0);
			}
		}
		return typeStr;
	}

	public List<LibraryVisitor> getClassByName(String name) {
		return library.get(name);
	}

	public void parseDir(File folder) {
		if (folder.isDirectory()) {
			for (File f : folder.listFiles())
				parseDir(f);
		} else {
			if (!folder.exists()) {
				// Hacky for inner class
				String dir = folder.getPath().substring(0, folder.getPath().lastIndexOf("/"));
				if (new File(dir).exists())
					parseDir(new File(dir));
				else if (new File(dir + ".java").exists())
					parseDir(new File(dir + ".java"));
			} else {
				LibraryVisitor v = parseFile(folder);
				if (v != null) {
					for (String c : v.getClassNames()) {
						if (!library.containsKey(c))
							library.put(c, new ArrayList<LibraryVisitor>());
						library.get(c).add(v);
					}
				}
			}
		}
	}

	private LibraryVisitor parseFile(File file) {
		if (!file.getName().endsWith(".java"))
			return null;

		CompilationUnit cu;
		try {
			cu = JavaParser.parse(file);
			LibraryVisitor visitor = new LibraryVisitor();
			visitor.visit(cu, null);
			enumMap.putAll(visitor.enumerateMap());
			System.out.println("[Parse] " + file.getName());
			return visitor;
		} catch (Exception e) {
			System.out.println("[Fail to Parse] " + file.getName());
			// e.printStackTrace();
		}
		return null;
	}

	public void setFaultyClass(String next) {
		faultyClass = next;
	}

	public EnumDeclaration getEnum(String type) {
		return enumMap.get(type);
	}

	public void putExternal(String substring, Class<?>[] clazz) {
		external.put(substring, clazz);
		System.out.println("dynamic add " + substring);
	}

	@SuppressWarnings("rawtypes")
	public List<Class[]> fetchMethods(String clazz, String method) {
		Class<?>[] cls = external.get(clazz);
		List<Class[]> list = new ArrayList<Class[]>();
		if (cls == null)
			return list;

		for (Class<?> cz : cls) {
			if (clazz.equals(method)) {
				for (Constructor c : cz.getConstructors()) {
					list.add(c.getParameterTypes());
				}
			} else {
				for (Method mtd : cz.getMethods()) {
					if (mtd.getName().equals(method)) {
						list.add(mtd.getParameterTypes());
					}
				}
			}
		}
		return list;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
