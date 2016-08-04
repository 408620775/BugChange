package classify;

import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;

public class BoostingClassify extends Classify {

	public BoostingClassify(Instances instances, String claName) {
		super(instances, claName);
		Classifier boost = new AdaBoostM1();
		setCla(boost);
	}

	@Override
	void Evaluation() throws Exception {
		res = new ArrayList<>();
		eval = new Evaluation(ins);
		eval.crossValidateModel(cla, ins, 10, new Random());
		res.add(eval.recall(0));
		res.add(eval.recall(1));
		res.add(eval.precision(0));
		res.add(eval.precision(1));
		res.add(eval.fMeasure(0));
		res.add(eval.fMeasure(1));
		res.add(eval.areaUnderROC(1));
		res.add(Math.sqrt(res.get(0) * res.get(1)));
		return;
	}

}
