/**
 * @author Lisa Apr 10, 2017 ConfigReader.java 
 */
package sketch4j.repair.config;

/**
 * This class does nothing with the most general setting
 */
public class ConfigReader {

	protected String sourceFolder;
	protected String testFolder;
	protected String[] faultyFile;
	protected String[] failingTests;
	protected String base;

	public void readConfigFile(String path) {

	}

	public String getSourceFolder() {
		return base + sourceFolder+"/";
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

}
