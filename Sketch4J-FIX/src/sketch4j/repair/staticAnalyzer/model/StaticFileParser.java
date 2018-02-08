/**
 * @author Lisa Apr 10, 2017 StaticAnalyzer.java 
 */
package sketch4j.repair.staticAnalyzer.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import sketch4j.repair.staticAnalyzer.visitor.ClassVisitor;
import sketch4j.repair.staticAnalyzer.visitor.LibraryParser;
import sketch4j.utils.ClassPrefixFinder;

public class StaticFileParser {
	private Map<String, ClassVisitor> files = new HashMap<String, ClassVisitor>();
	private LibraryParser libParser = new LibraryParser();
	private String source = "";

	public LibraryParser parseSource(String dir) {
		// libParser.parseDir(new File(dir));
		return libParser;
	}

	private ClassVisitor analyzeSingleFile(String file) {
		try {
			CompilationUnit cu = JavaParser.parse(new File(file));
			ClassVisitor visitor = new ClassVisitor();
			visitor.visit(cu, null);
			libParser.parseDir(new File(file.substring(0,file.lastIndexOf("/"))));
			for (String imp : visitor.getImports()) {
				
				String inimp = source + imp.replace(".", "/");
				if (!new File(inimp).isDirectory())
					imp += ".java";
				libParser.parseDir(new File(inimp));
			
				Class<?>[] clazz = ClassPrefixFinder.getClassesFromPrefix(imp);
				if (clazz != null && clazz.length > 0)
					libParser.putExternal(imp.substring(imp.lastIndexOf(".") + 1), clazz);
			}
			files.put(file, visitor);
			return visitor;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ClassVisitor parseFautyClass(String file) {
		if (files.containsKey(file))
			return files.get(file);
		if (!file.endsWith(".java"))
			return null;
		return analyzeSingleFile(file);
	}

	public LibraryParser getLibParser() {
		return libParser;
	}

	public void setSource(String sourceFolder) {
		source = sourceFolder;
		libParser.setSource(source);
	}

}
