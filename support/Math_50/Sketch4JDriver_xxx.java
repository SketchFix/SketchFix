
import sketch4j.executor.SketchExecutor;
import sketch4j.request.Sketch4J;

public class Sketch4JDriver {

	// @SuppressWarnings("rawtypes")
	public static void main(String[] arg) throws ClassNotFoundException {
		org.junit.runner.JUnitCore core = new org.junit.runner.JUnitCore();
		org.junit.runner.Result result1 = null;
		// org.junit.runner.Result result2 = null;
		Class target1 = Class.forName("org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest");
		// Class target2 =
		// Class.forName("org.apache.commons.lang3.time.FastDateParserTest");
		// try {

		do {
			Sketch4J.initialize();
			try {
				result1 = core.run(target1);
				if (result1.wasSuccessful()) {
					System.out.println("Found solution:" + Sketch4J.getString());
					break;
				} else {
//					System.out.println(
//							"No solution:" + Sketch4J.getString() + "\n" + result1.getFailures().get(0).getTrace());
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
		} while (SketchExecutor.incrementCounter());
		// }

	}

}
