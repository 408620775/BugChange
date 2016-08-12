package classify;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Range;

public class MyEvaluation extends Evaluation {
	int choose;

	public MyEvaluation(Instances data, int choose) throws Exception {
		super(data);
		this.choose = choose;
	}

	public void crossValidateModel(Classifier classifier, Instances data,
			int numFolds, Random random, Object... forPredictionsPrinting)
			throws Exception {
		// Make a copy of the data we can reorder
		data = new Instances(data);
		data.randomize(random);
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		/*if (choose==0) {
			System.out.println("MyEvluation common");
		}else if (choose==1) {
			System.out.println("MyEvluation under");
		}else if (choose==2) {
			System.out.println("MyEvluation over");
		}*/
		
		// We assume that the first element is a StringBuffer, the second a
		// Range
		// (attributes
		// to output) and the third a Boolean (whether or not to output a
		// distribution instead
		// of just a classification)
		if (forPredictionsPrinting.length > 0) {
			// print the header first
			StringBuffer buff = (StringBuffer) forPredictionsPrinting[0];
			Range attsToOutput = (Range) forPredictionsPrinting[1];
			boolean printDist = ((Boolean) forPredictionsPrinting[2])
					.booleanValue();
			printClassificationsHeader(data, attsToOutput, printDist, buff);
		}
		Sample sample = new Sample();
		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			Instances trainOrigin = data.trainCV(numFolds, i, random);
			System.out.println("trainOrigin's size is "+trainOrigin.numInstances());
			Instances train = null;
			if (choose == 0) {
				train = trainOrigin;
				System.out.println("common num of instances is "+train.numInstances());
			} else if (choose == 1) {
				train = sample.UnderSample(trainOrigin);
				System.out.println("underSample num of instances is "+train.numInstances());
			} else {
				train = sample.OverSample(trainOrigin);
				System.out.println("overSample num of instances is "+train.numInstances());
			}
			setPriors(train);
			Classifier copiedClassifier = Classifier.makeCopy(classifier);
			copiedClassifier.buildClassifier(train);
			Instances test = data.testCV(numFolds, i);
			evaluateModel(copiedClassifier, test, forPredictionsPrinting);
		}
		m_NumFolds = numFolds;
	}

}
