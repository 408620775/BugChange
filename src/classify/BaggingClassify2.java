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


	void Evaluation() throws Exception {
		Bagging classifier=new Bagging();
		classifier.setClassifier(cla);
		
		res = new ArrayList<>();
		eval = new MyEvalution(ins,choose);
		eval.crossValidateModel(classifier, ins, 10, new Random());
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
