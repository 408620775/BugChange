package classify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Randomizable;
import weka.core.SelectedTag;
import weka.core.Utils;

public class MyBagging extends Bagging {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Classifier base;
	Instances instances;
	String className;
	int choose;
	Classifier[] list;
	List<Double> res;

	public MyBagging(Classifier classifier, Instances ins, String className,
			int judge) throws Exception {
		super();
		super.setClassifier(classifier);
		this.base = classifier;
		this.instances = ins;
		this.className = className;
		this.choose = judge;
		instances.setClass(instances.attribute(className));

	}

	public void buildClassifier(Instances data) throws Exception {

		// can classifier handle the data?
		getCapabilities().testWithFail(data);

		// remove instances with missing class
		data = new Instances(data);
		data.deleteWithMissingClass();

		super.buildClassifier(data);

		if (m_CalcOutOfBag && (m_BagSizePercent != 100)) {
			throw new IllegalArgumentException("Bag size needs to be 100% if "
					+ "out-of-bag error is to be calculated!");
		}

		int bagSize = (int) (data.numInstances() * (m_BagSizePercent / 100.0));
		Random random = new Random(m_Seed);

		boolean[][] inBag = null;
		if (m_CalcOutOfBag)
			inBag = new boolean[m_Classifiers.length][];
		
		Sample sample = new Sample(className);////
		
		for (int j = 0; j < m_Classifiers.length; j++) {
			Instances bagData = null;
		// create the in-bag dataset
			if (m_CalcOutOfBag) {
				inBag[j] = new boolean[data.numInstances()];
				// bagData = resampleWithWeights(data, random, inBag[j]);
				bagData = data.resampleWithWeights(random, inBag[j]);
			} else {
				/*bagData = data.resampleWithWeights(random);
				if (bagSize < data.numInstances()) {
					bagData.randomize(random);
					Instances newBagData = new Instances(bagData, 0, bagSize);
					bagData = newBagData;
				}*/
				 if (choose == 0) {
						bagData=sample.RandomSample(data, 1);
					}else if (choose==1) {
						bagData=sample.UnderSample(data);
					}else {
						bagData=sample.OverSample(data);
					}
			}

			if (m_Classifier instanceof Randomizable) {
				((Randomizable) m_Classifiers[j]).setSeed(random.nextInt());
			}

			// build the classifier
			m_Classifiers[j].buildClassifier(bagData);
		}

		// calc OOB error?
		if (getCalcOutOfBag()) {
			double outOfBagCount = 0.0;
			double errorSum = 0.0;
			boolean numeric = data.classAttribute().isNumeric();

			for (int i = 0; i < data.numInstances(); i++) {
				double vote;
				double[] votes;
				if (numeric)
					votes = new double[1];
				else
					votes = new double[data.numClasses()];

				// determine predictions for instance
				int voteCount = 0;
				for (int j = 0; j < m_Classifiers.length; j++) {
					if (inBag[j][i])
						continue;

					voteCount++;
					// double pred =
					// m_Classifiers[j].classifyInstance(data.instance(i));
					if (numeric) {
						// votes[0] += pred;
						votes[0] += m_Classifiers[j].classifyInstance(data
								.instance(i));
					} else {
						// votes[(int) pred]++;
						double[] newProbs = m_Classifiers[j]
								.distributionForInstance(data.instance(i));
						// average the probability estimates
						for (int k = 0; k < newProbs.length; k++) {
							votes[k] += newProbs[k];
						}
					}
				}

				// "vote"
				if (numeric) {
					vote = votes[0];
					if (voteCount > 0) {
						vote /= voteCount; // average
					}
				} else {
					if (Utils.eq(Utils.sum(votes), 0)) {
					} else {
						Utils.normalize(votes);
					}
					vote = Utils.maxIndex(votes); // predicted class
				}

				// error for instance
				outOfBagCount += data.instance(i).weight();
				if (numeric) {
					errorSum += StrictMath.abs(vote
							- data.instance(i).classValue())
							* data.instance(i).weight();
				} else {
					if (vote != data.instance(i).classValue())
						errorSum += data.instance(i).weight();
				}
			}

			m_OutOfBagError = errorSum / outOfBagCount;
		} else {
			m_OutOfBagError = 0;
		}
	}
}
