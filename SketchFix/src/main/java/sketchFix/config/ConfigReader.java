/**
 * @author Lisa Apr 10, 2017 ConfigReader.java 
 */
package sketchFix.config;

/**
 * This class does nothing with the most general setting
 */
public class ConfigReader {

	protected String sourceFolder;
	protected String testFolder;
	protected String[] faultyFile;
	protected String[] failingTests;
	protected String base;
	protected String outputPath;
    protected ConfigType type;

    
    public static ConfigReader getInstance(ConfigType type) {
    	switch (type) {
    	case SIMPLE: return new SimpleConfigReader(); 
    	case DEFECTS4J: return new Defects4JConfigReader();
    	}
    	return null;
    }
	public void readConfigFile(String path) {

	}

	public String getSourceFolder() {
		return base + sourceFolder + "/";
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public String getTestFolder() {
		return base + testFolder;
	}

	public void setTestFolder(String testFolder) {
		this.testFolder = testFolder;
	}

	public String[] getFaultyFile() {
		String[] res = new String[faultyFile.length];
		for (int i = 0; i < faultyFile.length; i++)
			res[i] = base + faultyFile[i] + ".java";
		return faultyFile;
	}

	public void setFaultyFile(String[] faultyFile) {
		this.faultyFile = faultyFile;
	}

	public String[] getFailingTests() {
		String[] res = new String[failingTests.length];
		for (int i = 0; i < failingTests.length; i++)
			res[i] = base + failingTests[i] + ".java";
		return res;
	}

	public void setFailingTests(String[] failingTests) {
		this.failingTests = failingTests;

	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public enum ConfigType {
		DEFECTS4J, SIMPLE
	}
}
