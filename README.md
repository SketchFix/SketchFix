# SketchFix
Automatic Program Repair with On-Demand Candidate Generation.

Given a faulty program and a test suite that has test failures, SketchFix tries to find a repair for the faulty program such that all tests pass.  

## Hello World Example
1. Import the project to Eclipse. A faulty program: SimpleExpReplace.java

```
	public int simpleExpError() {
		int a = 0; 
		int b = 1;
		//expect to have int c = a;
		int c = b;
		return c;
	}
```
 Step 1: Run PatchGenerationRunner. Here I assume the Fault Location is known.  
 ```
 	public static void main(String[] args) {
		StaticAnalyzer analyzer = new StaticAnalyzer();
		analyzer.setConfigFile(ConfigType.SIMPLE, "SimpleConfig.txt");
		analyzer.setFaultLocation("SimpleEXPReplace:8"); 
	}
 ```
 SimpleConfig.txt is a config file similar to Defects4J build properties.
 ```
classes.modified=SimpleEXPReplace
dir.src.classes=src
dir.src.tests=src
tests.trigger=TestSimpleEXPReplace::test1
output.sketch=./
 ```

Step2: SketchFix generates a list of sketches. One of them is as below: 

```
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

Replace this file with SimpleEXPReplace.java and execute SketchFixDriver.java. 
```
Generate 3 candidates for the type int: [b, a, 0]
Found solution:
 Hole 0	a
```

