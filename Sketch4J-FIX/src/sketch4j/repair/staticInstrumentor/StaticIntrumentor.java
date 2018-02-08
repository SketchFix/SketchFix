/**
 * @author Lisa Apr 10, 2017 StaticIntrumentor.java 
 */
package sketch4j.repair.staticInstrumentor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaticIntrumentor {
	private List<TransformRule> transformer = new ArrayList<TransformRule>();

	// ClassVisitor faultyClassVisitor;
	public StaticIntrumentor(TransformSubject subject) {
		transformer.add(new ExpressionMutator(subject));
		transformer.add(new OperatorMutator(subject));
		transformer.add(new OverloadMutator(subject));
		transformer.add(new ConditionRemover(subject));
		transformer.add(new ConditionMutator(subject));
		transformer.add(new ConditionAdder(subject));
		transformer.add(new MultiExprMutator(subject));
	}

	public List<File> instrument() {
		List<File> files = new ArrayList<File>();
		for (TransformRule rule : transformer) {
			files.addAll(rule.transform());
		}
		return files;
	}

}
