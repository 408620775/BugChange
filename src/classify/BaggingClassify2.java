package classify;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

public class BaggingClassify2 extends Classify{
int choose;
	

	public BaggingClassify2(Classifier classifier, Instances instances,
			String claName,int choose) {
		
		super(classifier, instances, claName);		
		this.choose=choose;
	}
}
