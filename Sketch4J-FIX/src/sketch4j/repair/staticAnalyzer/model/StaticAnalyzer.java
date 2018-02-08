/**
 * @author Lisa Apr 10, 2017 StaticAnalyzer.java 
 */
package sketch4j.repair.staticAnalyzer.model;

import java.io.File;
import java.util.List;

import sketch4j.repair.config.ConfigReader;
import sketch4j.repair.config.Defects4JConfigReader;
import sketch4j.repair.faultLocalizer.Location;
import sketch4j.repair.faultLocalizer.SimpleFaultLocalizer;
import sketch4j.repair.staticInstrumentor.StaticIntrumentor;
import sketch4j.repair.staticInstrumentor.TransformSubject;
import sketch4j.repair.testRunner.FailingTestRunner;
import sketch4j.repair.testRunner.TestSuiteValidator;

public class StaticAnalyzer {
	protected ConfigReader config = new Defects4JConfigReader();
	protected SimpleFaultLocalizer localizer = new SimpleFaultLocalizer();
	protected TestSuiteValidator suite = new TestSuiteValidator();
	protected FailingTestRunner failTest = new FailingTestRunner();
	protected StaticFileParser parser = new StaticFileParser();

	public void setConfigFile(String file) {
		config.readConfigFile(file);
		localizer.setDir(config.getSourceFolder());
	}

	public void setFaultLocation(String locFile,int id) {
		List<Location> locs = localizer.readConfig(locFile,id);
		String path = locs.get(0).getFilePath();
		
		//this can be time consuming
//		parser.parseSource(path.substring(0, path.lastIndexOf("/")));
		parser.setSource(config.getSourceFolder());
		
		for (Location loc : locs) {
			TransformSubject subject = new TransformSubject(loc, parser);
			List<File> instrumented = new StaticIntrumentor(subject).instrument();
		}
	}
}
