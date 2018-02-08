package sketch4j.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassPrefixFinder {
    
    private static Map<String, Class<?>[]> classesOfPrefix = new HashMap<>();
    
    public static Class<?>[] getClassesFromPrefix(final String prefix) {
        if (!classesOfPrefix.containsKey(prefix)) {
            final Set<Class<?>> classesGivenPrefix = new HashSet<>();
            ClassFinder.findClasses(new ClassFinder.Visitor<String>() {
                @Override
                public boolean visit(String className) {
                    if (className.startsWith(prefix)) {
                        try {
                            classesGivenPrefix.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
            System.out.println("Classes found with package prefix \"" + prefix + "\": " + classesGivenPrefix);
            classesOfPrefix.put(prefix, classesGivenPrefix.toArray(new Class[classesGivenPrefix.size()]));
        }
        return classesOfPrefix.get(prefix);
    }

}