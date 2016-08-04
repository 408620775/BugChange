package classify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

/**
 * bagging分类器. 该分类器由10个子分类器构成.在进行模型构建时有两种方式. 第一种方式是直接调用weka的bagging方法;
 * 第二种方式是手动完成10个子分类器训练集的构建,并整合10个子分类器的分类效果,其中训练集的构建分为过采样和欠采样两种方式.
 * 
 * @author niu
 *
 */
public class BaggingClassify extends Classify {
	MyBagging bagging;
	int judge; // 0标示普通的bagging方法,1表示undersample,2表示oversample

	/**
	 * 构造函数;指定用于构建bagging分类器的训练集以及子分类器类型.
	 * 
	 * @param classifier
	 *            子分类器.
	 * @param instances
	 *            bagging分类器的训练集.
	 * @param ou
	 * @throws Exception
	 */
	public BaggingClassify(Classifier classifier, Instances instances, int ou,
			String claName) throws Exception {
		super(classifier, instances, claName);

		 bagging = new MyBagging(classifier, instances, claName, ou);
		judge = ou;
		// SimpleClassify simpleClassify = new SimpleClassify(cla);

	}

	void Evaluation() throws Exception {
		res = new ArrayList<>();
		eval = new Evaluation(ins);
		//ins.setClass(ins.attribute("bug_introducing"));
		eval.crossValidateModel(bagging, ins, 10, new Random(1));
		res.add(eval.recall(0));
		res.add(eval.recall(1));
		res.add(eval.precision(0));
		res.add(eval.precision(1));
		res.add(eval.fMeasure(0));
		res.add(eval.fMeasure(1));
		res.add(eval.areaUnderROC(1));
		res.add(Math.sqrt(res.get(0) * res.get(1)));
		return;
	/*	if (judge == 0) {
			bagging = new Bagging();
			bagging.setClassifier(cla);
			bagging.setNumIterations(10);
			res = new ArrayList<>();
			eval = new Evaluation(ins);
			eval.crossValidateModel(bagging, ins, 10, new Random(1));
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
		int loop = 0;
		Instances temp = null;
		List<Double[]> resDoubles = new ArrayList<>();
		while (loop < 10) {
			Sample sample = new Sample(className);
			if (judge == 1) {
				temp = sample.UnderSample(ins);
			} else if (judge == 2) {
				temp = sample.OverSample(ins);
			} else {
				temp = sample.smote(ins);
			}
			Double[] r = new Double[8];
			eval = new Evaluation(ins);
			cla = cla.getClass().newInstance();
			eval.crossValidateModel(cla, temp, 10, new Random());
			r[0] = eval.recall(0);
			r[1] = eval.recall(1);
			r[2] = eval.precision(0);
			r[3] = eval.precision(1);
			r[4] = eval.fMeasure(0);
			r[5] = eval.fMeasure(1);
			r[6] = eval.areaUnderROC(1);
			r[7] = Math.sqrt(r[0] * r[1]);
			resDoubles.add(r);
			loop++;
			System.out.println("loop" + loop);
		}
		res = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			double sum = 0;
			for (int j = 0; j < 10; j++) {
				sum = sum + resDoubles.get(j)[i];
			}
			sum = sum / 10;
			res.add(sum);
		}*/
	}
}
