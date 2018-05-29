# SketchFix
Automatic Program Repair with On-Demand Candidate Generation

## Note
1. For anyone who want to try out the old version of SketchFix. I update a zip file with a toy example to fix the bug below. Import the project to Eclipse. 

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
        int c = ((Integer) Sketch4J.EXP(int.class, new String[] { "b", "a" }, new Object[] { b, a }, 0, true, 1, 0));
        return c;
    }
}

```
The parameter is in old API format of EdSketch synthesis engine: Target type, String list only for the display purpose, candidate list, hole id, and 3 more. FYI, The rest three parameters are all fixed for SketchFix, in case you are interested: "true" are for whether include default value or not, and 1 is for lagency purpose (num of assignments that only used for EdSketch), and last "0" is the number of field dereference, e.g., node.next, but in SketchFix, I ture off these features, just because defects I tested from Defects4J does not need it. I also didn't claim that in ICSE paper. 

```
Generate 3 candidates for the type int: [b, a, 0]
Found solution:
 Hole 0	a
```

2. Since I am actively preparing a new version, I may break some features when preparing this HelloWorld example. You may email me and I should be able to fix quickly. Hopefully I could have the new version ready around July. 
