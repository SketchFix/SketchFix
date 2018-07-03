/**
 * @author Lisa Apr 10, 2017 StaticAnalyzer.java 
 */
package sketchFix.staticAnalyzer.model;

import java.io.File;
import java.util.List;

import sketchFix.config.ConfigReader;
import sketchFix.config.ConfigReader.ConfigType;
import sketchFix.faultLocalizer.Location;
import sketchFix.faultLocalizer.SimpleFaultLocalizer;
import sketchFix.staticInstrumentor.StaticIntrumentor;
import sketchFix.staticInstrumentor.TransformSubject;



public class StaticAnalyzer {
	protected ConfigReader config = ConfigReader.getInstance(ConfigType.DEFECTS4J);
	protected SimpleFaultLocalizer localizer = new SimpleFaultLocalizer();
	//protected TestSuiteValidator suite = new TestSuiteValidator();
	//protected FailingTestRunner failTest = new FailingTestRunner();
	protected StaticFileParser parser = new StaticFileParser();

	public void setConfigFile(ConfigType type, String file) {
		config = ConfigReader.getInstance(type);
		config.readConfigFile(file);
		localizer.setDir(config.getSourceFolder());
	}

	public void setFaultLocation(String locFile) {
		List<Location> locs = localizer.readConfig(locFile,0);
		String path = locs.get(0).getFilePath();
		
		//this can be time consuming
//		parser.parseSource(path.substring(0, path.lastIndexOf("/")));
		parser.setSource(config.getSourceFolder());
		
		for (Location loc : locs) {
			TransformSubject subject = new TransformSubject(loc, parser);
			List<File> instrumented = new StaticIntrumentor(subject).instrument(config.getOutputPath());
		}
	}
	
	public void setFaultLocation(String locFile,int id) {
		List<Location> locs = localizer.readConfig(locFile,id);
		String path = locs.get(0).getFilePath();
		
		//this can be time consuming
//		parser.parseSource(path.substring(0, path.lastIndexOf("/")));
		parser.setSource(config.getSourceFolder());
		
		for (Location loc : locs) {
			TransformSubject subject = new TransformSubject(loc, parser);
			List<File> instrumented = new StaticIntrumentor(subject).instrument(config.getOutputPath());
		}
	}
}
