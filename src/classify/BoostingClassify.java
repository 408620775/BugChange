package classify;

import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;

public class BoostingClassify extends Classify {
int choose;
	public BoostingClassify(Instances instances, String claName,int choose) {
		super(instances, claName);
		this.choose=choose;
		Classifier boost = new AdaBoostM1();
		setCla(boost);
	}

}
