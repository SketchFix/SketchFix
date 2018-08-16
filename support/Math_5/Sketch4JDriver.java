
/**
 * @author Lisa Apr 2, 2017 Sketch4JDriver.java 
 */

import sketch4j.executor.SketchExecutor;
import sketch4j.request.Sketch4J;

public class Sketch4JDriver {

    public static void main(String[] arg) throws ClassNotFoundException {
        org.junit.runner.JUnitCore core = new org.junit.runner.JUnitCore();
        org.junit.runner.Result result = null;
Class target = Class.forName("org.apache.commons.math3.complex.ComplexTest");
        int count=0;
        do {count++;
            Sketch4J.initialize();
            try {
                result = core.run(target);
                if (result.wasSuccessful())
                    break;
            } catch (Throwable e) {
                // System.out.println( Sketch4J.getString());
            }
        } while (SketchExecutor.incrementCounter());
        if (result.wasSuccessful()) {
            System.out.println("Found solution:" + Sketch4J.getString());
        } else {
            System.out.println("No solution!");
        }
        System.out.println("Space: "+count);
    }

}
