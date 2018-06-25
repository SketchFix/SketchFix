# SketchFix
Automatic Program Repair with On-Demand Candidate Generation.

## Hello World Example
1. Import the project to Eclipse. 

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
		analyzer.setFaultLocation("SimpleEXPReplace:11", 0); // ignore the second parameter 0 for now. It is used to fit D4J.
	}
 ```

Step2: You should be able to see a list of sketches generated. Replace the file named am-0-0 with SimpleEXPReplace.java and execute Sketch4JDriver.java. 

```
import sketch4j.request.Sketch4J;

public class SimpleEXPReplace {

    public int simpleExpError() {
        int a = 2;
        int b = 1;
        // expect to have int c = a;
        int c = ((Integer) Sketch4J.EXP(new Object[] { b, a }, 0, new String[] { "b", "a" },  int.class));
        return c;
    }
}

```
The parameter list: visible variable list, hold id, a string array simply for the purpose of printing results, target type.

```
Generate 3 candidates for the type int: [b, a, 0]
Found solution:
 Hole 0	a
```

