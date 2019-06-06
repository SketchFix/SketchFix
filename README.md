# SketchFix
Automatic Program Repair with On-Demand Candidate Generation.

Given a *faulty program* and a test suite that has test failures, SketchFix tries to find a repair for the faulty program such that all tests pass.  

## Hello World Example
Import the **HelloWorld.zip** to Eclipse. 
* A faulty program: `SimpleExpReplace.java` and a failing test case written in JUnit test framework.

``` Java
	public int simpleExpError() {
		int a = 2; 
		int b = 1;
		//expect to have int c = a;
		int c = b;
		return c;
	}
	
	@Test
	public void test1() {
		assertEquals(2, new SimpleEXPReplace().simpleExpError());
	}	
```
 **Step 1**: Run `PatchGenerationRunner.java`. Here I assume the Fault Location is known.  
 ``` Java
 	public static void main(String[] args) {
		StaticAnalyzer analyzer = new StaticAnalyzer();
		analyzer.setConfigFile(ConfigType.SIMPLE, "SimpleConfig.txt");
		analyzer.setFaultLocation("SimpleEXPReplace:8"); 
	}
 ```
 `SimpleConfig.txt` is a config file similar to Defects4J build properties.
 ```
classes.modified=SimpleEXPReplace
dir.src.classes=src
dir.src.tests=src
tests.trigger=TestSimpleEXPReplace::test1
output.sketch=./
 ```

**Step2**: SketchFix generates a list of sketches. One of them is as below: 

``` Java
import edSketch.request.SketchFix;

public class SimpleEXPReplace {

    public int simpleExpError() {
        int a = 2;
        int b = 1;
        // expect to have int c = a;
        int c = ((Integer) SketchFix.EXP(new Object[] { b, a }, 0, new String[] { "b", "a" }, int.class).invoke());
        return c;
    }
}

```
The parameter list: visible variable list, hold id, a string array simply for the purpose of printing results, target type.

Replace this file with `SimpleEXPReplace.java` and execute the JUnit test case with `SketchFixDriver.java`:
``` Java
import edSketch.executor.SketchExecutor;
import edSketch.request.SketchFix;

public class SketchFixDriver {
	public static void main(String[] arg){
		org.junit.runner.JUnitCore core = new org.junit.runner.JUnitCore();
		org.junit.runner.Result result1 = null;
		Class target1 = Class.forName("TestSimpleEXPReplace");
		do {
			SketchFix.reset();
			try {
				result1 = core.run(target1);
				if (result1.wasSuccessful() ) {
					System.out.println("Found solution:  " + SketchFix.getString());
					break;
				} 
			} catch (Exception e) {}
		} while (SketchExecutor.incrementCounter());
	}
}
```
If SketchFix finds a solution that passes all tests, it prints the result as below:

```
Generate 3 candidates for the type int: [b, a, 0]
Found solution:
 Hole 0	a
```

